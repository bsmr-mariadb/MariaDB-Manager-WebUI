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
 * Copyright SkySQL Ab
 */

package com.skysql.consolev.ui;

import java.util.LinkedHashMap;

import com.skysql.consolev.BackupRecord;
import com.skysql.consolev.api.BackupCommands;
import com.skysql.consolev.api.BackupStates;
import com.skysql.consolev.api.Backups;
import com.skysql.consolev.api.SystemInfo;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class PanelBackup extends VerticalLayout {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private static final String NOT_AVAILABLE = "n/a";

	private HorizontalLayout newLayout, backupsLayout;
	private Table backupsTable;
	private int oldBackupsCount;
	private LinkedHashMap<String, BackupRecord> backupsList;
	final LinkedHashMap<String, String> names = BackupCommands.getNames();

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

		Label placeholderLabel = new Label(
				"Scheduled backups are currently not available. To run an interactive backup, select a node and use the Control panel.");
		placeholderLabel.addStyleName("instructions");
		placeholderLabel.setSizeUndefined();
		newLayout.addComponent(placeholderLabel);
		newLayout.setComponentAlignment(placeholderLabel, Alignment.MIDDLE_CENTER);

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
		backupsTable.addContainerProperty("Level", String.class, null);
		backupsTable.addContainerProperty("Storage", String.class, null);
		backupsTable.addContainerProperty("Restored", String.class, null);
		backupsTable.addContainerProperty("Status", String.class, null);
		backupsTable.addContainerProperty("Log", Link.class, null);

		backupsLayout.addComponent(backupsTable);
		backupsLayout.setComponentAlignment(backupsTable, Alignment.MIDDLE_CENTER);

		SystemInfo systemInfo = VaadinSession.getCurrent().getAttribute(SystemInfo.class);
		LinkedHashMap<String, String> sysProperties = systemInfo.getProperties();
		String EIP = sysProperties.get(SystemInfo.PROPERTY_EIP);
		Link backupLogLink;

		Backups backups = new Backups(systemInfo.getID(), null);
		backupsList = backups.getBackupsList();
		if (backupsList != null) {
			int size = backupsList.size();
			if (oldBackupsCount != size) {
				oldBackupsCount = size;

				backupsTable.removeAllItems();
				for (BackupRecord backupRecord : backupsList.values()) {
					if (EIP != null) {
						String url = "http://" + EIP + "/consoleAPI/" + backupRecord.getLog();
						backupLogLink = new Link("Backup Log", new ExternalResource(url));
						backupLogLink.setTargetName("_blank");
						backupLogLink.setDescription("Open backup log in a new window");
						backupLogLink.setIcon(new ThemeResource("img/externalLink.png"));
						backupLogLink.addStyleName("icon-after-caption");
					} else {
						backupLogLink = null;
					}

					backupsTable.addItem(
							new Object[] { backupRecord.getStarted(), backupRecord.getUpdated(), backupRecord.getLevel(), backupRecord.getStorage(),
									backupRecord.getRestored(), BackupStates.getDescriptions().get(backupRecord.getStatus()),
									(backupLogLink != null) ? backupLogLink : NOT_AVAILABLE }, backupRecord.getID());
				}
			}
		} else {
			backupsTable.removeAllItems();
		}

	}

	public void refresh() {

	}

	private String backupLabels[] = { "Node", "Level", "State", "Size", "Restored" };
	private GridLayout backupInfoGrid;
	private Link backupLogLink;

	final public void displayBackupInfo(VerticalLayout layout, BackupRecord record) {
		String value;
		String values[] = { (value = record.getID()) != null ? value : NOT_AVAILABLE, (value = record.getLevel()) != null ? value : NOT_AVAILABLE,
				((value = record.getStatus()) != null) && (value = BackupStates.getDescriptions().get(value)) != null ? value : "Invalid",
				(value = record.getSize()) != null ? value : NOT_AVAILABLE, (value = record.getRestored()) != null ? value : "" };

		GridLayout newBackupInfoGrid = new GridLayout(2, backupLabels.length);
		for (int i = 0; i < backupLabels.length; i++) {
			newBackupInfoGrid.addComponent(new Label(backupLabels[i]), 0, i);
			newBackupInfoGrid.addComponent(new Label(values[i]), 1, i);
		}

		if (backupInfoGrid == null) {
			layout.addComponent(newBackupInfoGrid);
			backupInfoGrid = newBackupInfoGrid;
		} else {
			layout.replaceComponent(backupInfoGrid, newBackupInfoGrid);
			backupInfoGrid = newBackupInfoGrid;
		}

		SystemInfo systemInfo = VaadinSession.getCurrent().getAttribute(SystemInfo.class);
		LinkedHashMap<String, String> sysProperties = systemInfo.getProperties();
		String EIP = sysProperties.get(SystemInfo.PROPERTY_EIP);
		if (EIP != null) {
			String url = "http://" + EIP + "/consoleAPI/" + record.getLog();
			Link newBackupLogLink = new Link("Backup Log", new ExternalResource(url));
			newBackupLogLink.setTargetName("_blank");
			newBackupLogLink.setDescription("Open backup log in a new window");
			newBackupLogLink.setIcon(new ThemeResource("img/externalLink.png"));
			newBackupLogLink.addStyleName("icon-after-caption");

			if (backupLogLink == null) {
				layout.addComponent(newBackupLogLink);
				backupLogLink = newBackupLogLink;
			} else {
				layout.replaceComponent(backupLogLink, newBackupLogLink);
				backupLogLink = newBackupLogLink;
			}
		}
	}

}
