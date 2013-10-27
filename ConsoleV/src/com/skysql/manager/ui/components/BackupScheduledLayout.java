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
import com.skysql.manager.DateConversion;
import com.skysql.manager.ManagerUI;
import com.skysql.manager.ScheduleRecord;
import com.skysql.manager.SystemRecord;
import com.skysql.manager.api.Schedule;
import com.skysql.manager.api.SystemInfo;
import com.skysql.manager.ui.CalendarDialog;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class BackupScheduledLayout extends VerticalLayout {

	private Table scheduledTable;
	private UpdaterThread updaterThread;
	public Schedule schedule;
	private BackupScheduledLayout thisLayout = this;
	private Button calendarButton;

	public BackupScheduledLayout() {

		addStyleName("scheduledLayout");
		setSpacing(true);
		setMargin(true);

		HorizontalLayout scheduleRow = new HorizontalLayout();
		scheduleRow.setSpacing(true);
		addComponent(scheduleRow);

		final Label scheduleLabel = new Label("Schedule backups using the");
		scheduleLabel.setSizeUndefined();
		scheduleRow.addComponent(scheduleLabel);
		scheduleRow.setComponentAlignment(scheduleLabel, Alignment.MIDDLE_LEFT);

		calendarButton = new Button("Calendar");
		calendarButton.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(Button.ClickEvent event) {
				if (schedule == null) {
					schedule = getSchedule();
				}
				new CalendarDialog(schedule, thisLayout);
			}
		});
		scheduleRow.addComponent(calendarButton);
		scheduleRow.setComponentAlignment(calendarButton, Alignment.MIDDLE_LEFT);

		final Label immediateLabel = new Label("(To run an immediate backup, select a node first then switch to the Control panel)");
		immediateLabel.setSizeUndefined();
		scheduleRow.addComponent(immediateLabel);
		scheduleRow.setComponentAlignment(immediateLabel, Alignment.MIDDLE_CENTER);

		scheduledTable = new Table("Next Scheduled Backups");
		scheduledTable.setPageLength(5);
		// Start time, node
		scheduledTable.addContainerProperty("Start", String.class, null);
		scheduledTable.addContainerProperty("Node", String.class, null);
		scheduledTable.addContainerProperty("Level", String.class, null);
		scheduledTable.addContainerProperty("User", String.class, null);
		addComponent(scheduledTable);
		setComponentAlignment(scheduledTable, Alignment.MIDDLE_LEFT);

	}

	private Schedule getSchedule() {
		SystemInfo systemInfo = VaadinSession.getCurrent().getAttribute(SystemInfo.class);
		String systemID = systemInfo.getCurrentID();
		if (SystemInfo.SYSTEM_ROOT.equals(systemID)) {
			ClusterComponent clusterComponent = VaadinSession.getCurrent().getAttribute(ClusterComponent.class);
			systemID = clusterComponent.getID();
		}

		return new Schedule(systemID, null);

	}

	public void refresh() {

		ManagerUI.log("ScheduleLayout refresh()");
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
				ManagerUI.log(this.getClass().getName() + " - Old thread is alive: " + oldUpdaterThread);
				oldUpdaterThread.flagged = true;
				oldUpdaterThread.interrupt();
				try {
					ManagerUI.log(this.getClass().getName() + " - Before Join");
					oldUpdaterThread.join();
					ManagerUI.log(this.getClass().getName() + " - After Join");
				} catch (InterruptedException iex) {
					ManagerUI.log(this.getClass().getName() + " - Interrupted Exception");
					return;
				}

			}

			ManagerUI.log(this.getClass().getName() + " - UpdaterThread.this: " + this);
			asynchRefresh(this);
		}
	}

	private void asynchRefresh(final UpdaterThread updaterThread) {

		ManagerUI managerUI = getSession().getAttribute(ManagerUI.class);

		schedule = getSchedule();
		final LinkedHashMap<String, ScheduleRecord> scheduleList = schedule.getScheduleList();

		managerUI.access(new Runnable() {
			@Override
			public void run() {
				// Here the UI is locked and can be updated

				ManagerUI.log(this.getClass().getName() + " access run(): ");

				SystemInfo systemInfo = VaadinSession.getCurrent().getAttribute(SystemInfo.class);
				String systemID = systemInfo.getCurrentID();
				if (systemID.equals(SystemInfo.SYSTEM_ROOT)) {
					ClusterComponent clusterComponent = VaadinSession.getCurrent().getAttribute(ClusterComponent.class);
					systemID = clusterComponent.getID();
				}
				SystemRecord systemRecord = systemInfo.getSystemRecord(systemID);
				if (systemRecord.getNodes().length == 0) {
					calendarButton.setEnabled(false);
					calendarButton.setDescription("There are no nodes in this system: no backups scheduling is possible.");
				} else {
					calendarButton.setEnabled(true);
					calendarButton.setDescription(null);
				}

				scheduledTable.removeAllItems();
				if (scheduleList != null) {
					ListIterator<Map.Entry<String, ScheduleRecord>> iter = new ArrayList<Entry<String, ScheduleRecord>>(scheduleList.entrySet()).listIterator();

					while (iter.hasNext()) {
						if (updaterThread.flagged) {
							ManagerUI.log("ScheduleLayout - flagged is set during table population");
							return;
						}

						Map.Entry<String, ScheduleRecord> entry = iter.next();
						ScheduleRecord scheduleRecord = entry.getValue();

						scheduledTable.addItem(
								new Object[] { DateConversion.adjust(scheduleRecord.getNextStart()), scheduleRecord.getNodeID(), scheduleRecord.getParams(),
										scheduleRecord.getUserID() }, scheduleRecord.getID());
					}

				}

			}
		});

	}
}
