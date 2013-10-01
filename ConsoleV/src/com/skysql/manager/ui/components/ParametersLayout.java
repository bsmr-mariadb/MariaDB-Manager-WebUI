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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.skysql.manager.BackupRecord;
import com.skysql.manager.Commands;
import com.skysql.manager.Commands.Command;
import com.skysql.manager.api.BackupStates;
import com.skysql.manager.api.Backups;
import com.skysql.manager.api.NodeInfo;
import com.skysql.manager.api.SystemInfo;
import com.skysql.manager.ui.RunningTask;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class ParametersLayout extends HorizontalLayout {

	private static final String NOT_AVAILABLE = "n/a";

	private OptionGroup backupLevel;
	private HorizontalLayout prevBackupsLayout;
	private ListSelect selectPrevBackup;
	private String firstItem;
	private GridLayout backupInfoGrid;
	private Link backupLogLink;
	private TextField connectPassword;
	private TextField connectKey;
	private String connectParameters;
	private RunningTask runningTask;

	public ParametersLayout(final RunningTask runningTask, NodeInfo nodeInfo, Commands.Command commandEnum) {
		this.runningTask = runningTask;

		addStyleName("parametersLayout");
		setSizeFull();
		setSpacing(true);
		setMargin(true);

		switch (commandEnum) {
		case backup:
			backupLevel = new OptionGroup("Backup Level");
			backupLevel.setImmediate(true);
			backupLevel.addItem("Full");
			backupLevel.addItem("Incremental");
			addComponent(backupLevel);
			setComponentAlignment(backupLevel, Alignment.MIDDLE_LEFT);
			backupLevel.addValueChangeListener(new ValueChangeListener() {
				private static final long serialVersionUID = 0x4C656F6E6172646FL;

				public void valueChange(ValueChangeEvent event) {
					String level = (String) event.getProperty().getValue();
					runningTask.selectParameter(level);
					if (level.equalsIgnoreCase("Incremental")) {
						addComponent(prevBackupsLayout);
						selectPrevBackup.select(firstItem);

					} else {
						if (getComponentIndex(prevBackupsLayout) != -1) {
							removeComponent(prevBackupsLayout);
						}
					}
				}
			});

			backupLevel.setValue("Full");

		case restore:
			prevBackupsLayout = new HorizontalLayout();

			selectPrevBackup = new ListSelect("Backups");
			selectPrevBackup.setImmediate(true);
			final Backups backups = new Backups(nodeInfo.getParentID(), null);
			final LinkedHashMap<String, BackupRecord> backupsList = backups.getBackupsForNode(nodeInfo.getID());
			if (backupsList != null && backupsList.size() > 0) {
				Collection<BackupRecord> set = backupsList.values();
				Iterator<BackupRecord> iter = set.iterator();
				while (iter.hasNext()) {
					BackupRecord backupRecord = iter.next();
					String started = backupRecord.getStarted();
					selectPrevBackup.addItem(started);
					if (firstItem == null) {
						firstItem = started;
					}
				}
				selectPrevBackup.setNullSelectionAllowed(false);
				selectPrevBackup.setRows(8); // Show a few items and a scrollbar if there are more
				prevBackupsLayout.addComponent(selectPrevBackup);

				final VerticalLayout backupInfoLayout = new VerticalLayout();
				backupInfoLayout.setMargin(true);
				prevBackupsLayout.addComponent(backupInfoLayout);

				selectPrevBackup.addValueChangeListener(new ValueChangeListener() {
					private static final long serialVersionUID = 0x4C656F6E6172646FL;

					public void valueChange(ValueChangeEvent event) {
						String date = (String) event.getProperty().getValue();
						Collection<BackupRecord> set = backupsList.values();
						Iterator<BackupRecord> iter = set.iterator();
						while (iter.hasNext()) {
							BackupRecord backupRecord = iter.next();
							String started = backupRecord.getStarted();
							if (date.equalsIgnoreCase(started)) {
								displayBackupInfo(backupInfoLayout, backupRecord);
								if (backupLevel != null) {
									runningTask.selectParameter("Incremental " + backupRecord.getID());
								} else {
									runningTask.selectParameter(backupRecord.getID());
								}
								break;
							}
						}

					}
				});

				// final DisplayBackupRecord displayRecord = new
				// DisplayBackupRecord(parameterLayout);

				if (commandEnum == Command.restore) {
					addComponent(prevBackupsLayout);
					selectPrevBackup.select(firstItem);

				}

			} else {
				// no previous backups
				if (commandEnum == Command.backup) {
					backupLevel.setEnabled(false);

				} else if (commandEnum == Command.restore) {

					Label placeholderLabel = new Label("No Backups are available for Restore");
					placeholderLabel.addStyleName("instructions");
					placeholderLabel.setSizeUndefined();
					runningTask.getLayout().replaceComponent(runningTask.getScriptingLayout(), placeholderLabel);
					runningTask.getLayout().setComponentAlignment(placeholderLabel, Alignment.MIDDLE_CENTER);
				}
			}
			break;

		case connect:
			VerticalLayout connectParamsLayout = new VerticalLayout();
			addComponent(connectParamsLayout);
			setComponentAlignment(connectParamsLayout, Alignment.MIDDLE_LEFT);

			connectPassword = new TextField("Root Password");
			connectPassword.setImmediate(true);
			connectParamsLayout.addComponent(connectPassword);
			connectParamsLayout.setComponentAlignment(connectPassword, Alignment.MIDDLE_LEFT);
			connectPassword.addValueChangeListener(connectParamsListener);

			connectKey = new TextField("SSH Key");
			connectKey.setImmediate(true);
			connectParamsLayout.addComponent(connectKey);
			connectParamsLayout.setComponentAlignment(connectKey, Alignment.MIDDLE_LEFT);
			connectKey.addValueChangeListener(connectParamsListener);
			break;

		}

	}

	private ValueChangeListener connectParamsListener = new ValueChangeListener() {
		private static final long serialVersionUID = 0x4C656F6E6172646FL;

		public void valueChange(ValueChangeEvent event) {
			String params = "rootpassword=" + connectPassword.getValue() + "&sshkey=" + connectKey.getValue();
			runningTask.selectParameter(params);
		}
	};

	private String backupLabels[] = { "Node", "Level", "State", "Size", "Restored" };

	final public void displayBackupInfo(VerticalLayout layout, BackupRecord record) {
		String value;
		String values[] = { (value = record.getID()) != null ? value : NOT_AVAILABLE, (value = record.getLevel()) != null ? value : NOT_AVAILABLE,
				((value = record.getState()) != null) && (value = BackupStates.getDescriptions().get(value)) != null ? value : "Invalid",
				(value = record.getSize()) != null ? value : NOT_AVAILABLE, (value = record.getRestored()) != null ? value : NOT_AVAILABLE };

		GridLayout newBackupInfoGrid = new GridLayout(2, backupLabels.length);
		newBackupInfoGrid.setSpacing(true);

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
		LinkedHashMap<String, String> sysProperties = systemInfo.getCurrentSystem().getProperties();
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