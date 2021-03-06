/*
 * This file is distributed as part of the MariaDB Manager.  It is free
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
 * Copyright 2012-2014 SkySQL Corporation Ab
 */

package com.skysql.manager.ui.components;

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
import com.vaadin.ui.Link;
import com.vaadin.ui.Table;

/**
 * The Class BackupSetsLayout.
 */
@SuppressWarnings("serial")
public class BackupSetsLayout extends HorizontalLayout {

	private Table backupsTable;
	private int oldBackupsCount;
	private UpdaterThread updaterThread;

	/**
	 * Instantiates a new backup sets layout.
	 */
	public BackupSetsLayout() {

		addStyleName("backupsLayout");
		setSpacing(true);
		setMargin(true);

		backupsTable = new Table("Existing Backup Sets");
		backupsTable.setPageLength(10);
		backupsTable.addContainerProperty("ID", String.class, null);
		backupsTable.addContainerProperty("Started", String.class, null);
		backupsTable.addContainerProperty("Completed", String.class, null);
		backupsTable.addContainerProperty("Restored", String.class, null);
		backupsTable.addContainerProperty("Level", String.class, null);
		backupsTable.addContainerProperty("Parent", String.class, null);
		backupsTable.addContainerProperty("Node", String.class, null);
		backupsTable.addContainerProperty("Size", String.class, null);
		backupsTable.addContainerProperty("Storage", String.class, null);
		backupsTable.addContainerProperty("State", String.class, null);
		backupsTable.addContainerProperty("Log", Link.class, null);

		addComponent(backupsTable);
		setComponentAlignment(backupsTable, Alignment.MIDDLE_CENTER);

	}

	/**
	 * Refresh.
	 */
	public void refresh() {

		ManagerUI.log("BackupsLayout refresh()");
		updaterThread = new UpdaterThread(updaterThread);
		updaterThread.start();

	}

	/**
	 * The Class UpdaterThread.
	 */
	class UpdaterThread extends Thread {

		UpdaterThread oldUpdaterThread;
		volatile boolean flagged = false;
		volatile boolean adjust;
		volatile String format;

		/**
		 * Instantiates a new updater thread.
		 *
		 * @param oldUpdaterThread the old updater thread
		 */
		UpdaterThread(UpdaterThread oldUpdaterThread) {
			this.oldUpdaterThread = oldUpdaterThread;
		}

		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
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

	/**
	 * Asynch refresh.
	 *
	 * @param updaterThread the updater thread
	 */
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
		final LinkedHashMap<String, BackupRecord> backupsList = backups.getBackupsList();

		managerUI.access(new Runnable() {
			@Override
			public void run() {
				// Here the UI is locked and can be updated

				ManagerUI.log(this.getClass().getName() + " access run(): ");

				DateConversion dateConversion = getSession().getAttribute(DateConversion.class);
				boolean adjust = dateConversion.isAdjustedToLocal();
				String format = dateConversion.getFormat();

				if (backupsList != null) {
					int size = backupsList.size();
					if (oldBackupsCount != size || adjust != updaterThread.adjust || !format.equals(updaterThread.format)) {
						oldBackupsCount = size;
						updaterThread.adjust = adjust;
						updaterThread.format = format;

						backupsTable.removeAllItems();
						ListIterator<Map.Entry<String, BackupRecord>> iter = new ArrayList<Entry<String, BackupRecord>>(backupsList.entrySet()).listIterator(0);

						while (iter.hasNext()) {
							if (updaterThread.flagged) {
								ManagerUI.log("PanelBackup - flagged is set during table population");
								return;
							}

							Map.Entry<String, BackupRecord> entry = iter.next();
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
									new Object[] { backupRecord.getID(), dateConversion.adjust(backupRecord.getStarted()),
											dateConversion.adjust(backupRecord.getCompleted()), dateConversion.adjust(backupRecord.getRestored()),
											backupRecord.getLevelAsString(), backupRecord.getParent(), backupRecord.getNode(), backupRecord.getSize(),
											backupRecord.getStorage(), BackupStates.getDescriptions().get(backupRecord.getState()), backupLogLink },
									backupRecord.getID());
						}
					}
				} else {
					backupsTable.removeAllItems();
				}

			}
		});

	}

}
