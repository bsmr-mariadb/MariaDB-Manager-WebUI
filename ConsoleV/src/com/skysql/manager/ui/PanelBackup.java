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

package com.skysql.manager.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import com.skysql.manager.BackupRecord;
import com.skysql.manager.ClusterComponent;
import com.skysql.manager.DateConversion;
import com.skysql.manager.ManagerUI;
import com.skysql.manager.api.BackupStates;
import com.skysql.manager.api.Backups;
import com.skysql.manager.api.SystemInfo;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class PanelBackup extends VerticalLayout {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private HorizontalLayout newLayout, backupsLayout;
	private Table backupsTable;
	private int oldBackupsCount;
	private LinkedHashMap<String, BackupRecord> backupsList;
	private UpdaterThread updaterThread;

	PanelBackup() {

		setSizeFull();
		addStyleName("backupTab");

		createNewLayout();
		createLogsLayout();

	}

	private void createNewLayout() {

		newLayout = new HorizontalLayout();
		newLayout.addStyleName("newLayout");
		//newLayout.setHeight("210px");
		newLayout.setMargin(true);
		newLayout.setSpacing(true);
		addComponent(newLayout);

		final Label placeholderLabel = new Label("To run an immediate backup, select a node first then switch to the Control panel.");
		placeholderLabel.addStyleName("instructions");
		placeholderLabel.setSizeUndefined();
		newLayout.addComponent(placeholderLabel);
		newLayout.setComponentAlignment(placeholderLabel, Alignment.MIDDLE_CENTER);

		// Calendar button
		//		CalendarDialog calendarDialog = new CalendarDialog("Calendar");
		//		Button calendarButton = calendarDialog.getButton();
		//		newLayout.addComponent(calendarButton);
		//		newLayout.setComponentAlignment(calendarButton, Alignment.MIDDLE_CENTER);

	}

	private void createLogsLayout() {

		backupsLayout = new HorizontalLayout();
		backupsLayout.addStyleName("logsLayout");
		backupsLayout.setSpacing(true);
		backupsLayout.setMargin(true);
		addComponent(backupsLayout);
		setExpandRatio(backupsLayout, 1.0f);

		/*** BACKUPS **********************************************/

		backupsTable = new Table("Existing Backup Sets");
		backupsTable.addContainerProperty("Started", String.class, null);
		backupsTable.addContainerProperty("Completed", String.class, null);
		backupsTable.addContainerProperty("Restored", String.class, null);
		backupsTable.addContainerProperty("Level", String.class, null);
		backupsTable.addContainerProperty("Node", String.class, null);
		backupsTable.addContainerProperty("Size", String.class, null);
		backupsTable.addContainerProperty("Storage", String.class, null);
		backupsTable.addContainerProperty("State", String.class, null);
		backupsTable.addContainerProperty("Log", Link.class, null);

		backupsLayout.addComponent(backupsTable);
		backupsLayout.setComponentAlignment(backupsTable, Alignment.MIDDLE_CENTER);

	}

	public void refresh() {

		ManagerUI.log("PanelBackup refresh()");
		updaterThread = new UpdaterThread(updaterThread);
		updaterThread.start();
		ManagerUI.log("PanelBackup after refresh()");

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
				ManagerUI.log("PanelBackup - Old thread is alive: " + oldUpdaterThread);
				oldUpdaterThread.flagged = true;
				oldUpdaterThread.interrupt();
				try {
					ManagerUI.log("PanelBackup - Before Join");
					oldUpdaterThread.join();
					ManagerUI.log("PanelBackup - After Join");
				} catch (InterruptedException iex) {
					ManagerUI.log("PanelBackup - Interrupted Exception");
					return;
				}

			}

			ManagerUI.log("PanelBackup - UpdaterThread.this: " + this);
			asynchRefresh(this);
		}
	}

	private void asynchRefresh(final UpdaterThread updaterThread) {

		ManagerUI managerUI = getSession().getAttribute(ManagerUI.class);

		SystemInfo systemInfo = VaadinSession.getCurrent().getAttribute(SystemInfo.class);

		/***
		LinkedHashMap<String, String> sysProperties = systemInfo.getCurrentSystem().getProperties();
		final String EIP = sysProperties.get(SystemInfo.PROPERTY_EIP);
		***/

		String systemID = systemInfo.getCurrentID();
		if (SystemInfo.SYSTEM_ROOT.equals(systemID)) {
			ClusterComponent clusterComponent = VaadinSession.getCurrent().getAttribute(ClusterComponent.class);
			systemID = clusterComponent.getID();
		}

		Backups backups = new Backups(systemID, null);
		backupsList = backups.getBackupsList();

		managerUI.access(new Runnable() {
			@Override
			public void run() {
				// Here the UI is locked and can be updated

				ManagerUI.log("PanelBackup access run(): ");

				if (backupsList != null) {
					int size = backupsList.size();
					if (oldBackupsCount != size) {
						oldBackupsCount = size;

						backupsTable.removeAllItems();
						ListIterator<Map.Entry<String, BackupRecord>> iter = new ArrayList<Entry<String, BackupRecord>>(backupsList.entrySet())
								.listIterator(backupsList.size());

						while (iter.hasPrevious()) {
							if (updaterThread.flagged) {
								ManagerUI.log("PanelBackup - flagged is set during table population");
								return;
							}

							Map.Entry<String, BackupRecord> entry = iter.previous();
							BackupRecord backupRecord = entry.getValue();
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

							backupsTable.addItem(
									new Object[] { DateConversion.adjust(backupRecord.getStarted()), DateConversion.adjust(backupRecord.getUpdated()),
											DateConversion.adjust(backupRecord.getRestored()), backupRecord.getLevel(), backupRecord.getNode(),
											backupRecord.getSize(), backupRecord.getStorage(), BackupStates.getDescriptions().get(backupRecord.getState()),
											backupLogLink }, backupRecord.getID());
						}
					}
				} else {
					backupsTable.removeAllItems();
				}

			}
		});

	}

}
