/*
 * This file is distributed as part of the SkySQL Cloud Data Suite.  It is free
 * software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * version 2.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Copyright 2012-2013 SkySQL Ab
 */

package com.skysql.manager.ui.components;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import com.skysql.manager.ClusterComponent;
import com.skysql.manager.ManagerUI;
import com.skysql.manager.ScheduledRecord;
import com.skysql.manager.api.Scheduled;
import com.skysql.manager.api.SystemInfo;
import com.skysql.manager.ui.CalendarDialog;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Link;
import com.vaadin.ui.Table;

@SuppressWarnings("serial")
public class BackupScheduledLayout extends HorizontalLayout {

	private Table scheduledTable;
	private int oldScheduledCount;
	private UpdaterThread updaterThread;

	public BackupScheduledLayout() {

		addStyleName("scheduledLayout");
		setSpacing(true);
		setMargin(true);

		scheduledTable = new Table("Next Scheduled Backups");
		scheduledTable.setPageLength(5);
		// Start time, Level, Node, Alternate Node
		scheduledTable.addContainerProperty("Start", String.class, null);
		scheduledTable.addContainerProperty("Level", String.class, null);
		scheduledTable.addContainerProperty("Node", String.class, null);
		scheduledTable.addContainerProperty("Alternate Node", String.class, null);

		addComponent(scheduledTable);
		setComponentAlignment(scheduledTable, Alignment.MIDDLE_LEFT);

		//Calendar button 
		CalendarDialog calendarDialog = new CalendarDialog("Calendar");
		Button calendarButton = calendarDialog.getButton();
		addComponent(calendarButton);
		setComponentAlignment(calendarButton, Alignment.MIDDLE_CENTER);

	}

	public void refresh() {

		ManagerUI.log("ScheduledLayout refresh()");
		updaterThread = new UpdaterThread(updaterThread);
		updaterThread.start();

	}

	class UpdaterThread extends Thread {
		UpdaterThread oldUpdaterThread;
		volatile boolean flagged = false;

		UpdaterThread(UpdaterThread oldUpdaterThread) {
			this.oldUpdaterThread = oldUpdaterThread;
		}

		@Override
		public void run() {
			if (oldUpdaterThread != null && oldUpdaterThread.isAlive()) {
				ManagerUI.log("ScheduledLayout - Old thread is alive: " + oldUpdaterThread);
				oldUpdaterThread.flagged = true;
				oldUpdaterThread.interrupt();
				try {
					ManagerUI.log("ScheduledLayout - Before Join");
					oldUpdaterThread.join();
					ManagerUI.log("ScheduledLayout - After Join");
				} catch (InterruptedException iex) {
					ManagerUI.log("ScheduledLayout - Interrupted Exception");
					return;
				}

			}

			ManagerUI.log("ScheduledLayout - UpdaterThread.this: " + this);
			asynchRefresh(this);
		}
	}

	private void asynchRefresh(final UpdaterThread updaterThread) {

		ManagerUI managerUI = getSession().getAttribute(ManagerUI.class);

		SystemInfo systemInfo = VaadinSession.getCurrent().getAttribute(SystemInfo.class);

		String systemID = systemInfo.getCurrentID();
		if (SystemInfo.SYSTEM_ROOT.equals(systemID)) {
			ClusterComponent clusterComponent = VaadinSession.getCurrent().getAttribute(ClusterComponent.class);
			systemID = clusterComponent.getID();
		}

		Scheduled scheduled = new Scheduled(systemID, null);
		final LinkedHashMap<String, ScheduledRecord> scheduledList = scheduled.getScheduledList();

		managerUI.access(new Runnable() {
			@Override
			public void run() {
				// Here the UI is locked and can be updated

				ManagerUI.log("ScheduledLayout access run(): ");

				if (scheduledList != null) {
					int size = scheduledList.size();
					if (oldScheduledCount != size) {
						oldScheduledCount = size;

						scheduledTable.removeAllItems();
						ListIterator<Map.Entry<String, ScheduledRecord>> iter = new ArrayList<Entry<String, ScheduledRecord>>(scheduledList.entrySet())
								.listIterator();

						while (iter.hasNext()) {
							if (updaterThread.flagged) {
								ManagerUI.log("ScheduledLayout - flagged is set during table population");
								return;
							}

							Map.Entry<String, ScheduledRecord> entry = iter.next();
							ScheduledRecord scheduledRecord = entry.getValue();
							Link backupLogLink = null;

							/**
							if (EIP != null) {
								String url = "http://" + EIP + "/consoleAPI/" + backupRecord.getLog();
								backupLogLink = new Link("Backup Log", new ExternalResource(url));
								backupLogLink.setTargetName("_blank");
								backupLogLink.setDescription("Open backup log in a new window");
								backupLogLink.setIcon(new ThemeResource("img/externalLink.png"));
								backupLogLink.addStyleName("icon-after-caption");
							}
							***/

							scheduledTable.addItem(new Object[] { scheduledRecord.getID(), scheduledRecord.getICal(), scheduledRecord.getNodeID(),
									scheduledRecord.getTaskID() }, scheduledRecord.getID());
						}
					}
				} else {
					scheduledTable.removeAllItems();
				}

			}
		});

	}
}
