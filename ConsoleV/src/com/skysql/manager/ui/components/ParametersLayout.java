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
import com.skysql.manager.SystemRecord;
import com.skysql.manager.api.BackupStates;
import com.skysql.manager.api.Backups;
import com.skysql.manager.api.NodeInfo;
import com.skysql.manager.api.SystemInfo;
import com.skysql.manager.ui.RunningTask;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.NativeSelect;
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
	private RunningTask runningTask;
	private LinkedHashMap<String, BackupRecord> backupsList;
	private VerticalLayout backupInfoLayout;
	private boolean isParameterReady = false;

	public ParametersLayout(final RunningTask runningTask, final NodeInfo nodeInfo, Commands.Command commandEnum) {
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
			isParameterReady = true;

		case restore:
			VerticalLayout restoreLayout = new VerticalLayout();
			restoreLayout.setSpacing(true);

			if (commandEnum == Command.restore) {
				addComponent(restoreLayout);
				FormLayout formLayout = new FormLayout();
				//formLayout.setMargin(new MarginInfo(false, false, true, false));
				formLayout.setMargin(false);
				restoreLayout.addComponent(formLayout);

				final NativeSelect selectSystem;
				selectSystem = new NativeSelect("Backups from");
				selectSystem.setImmediate(true);
				selectSystem.setNullSelectionAllowed(false);
				SystemInfo systemInfo = VaadinSession.getCurrent().getAttribute(SystemInfo.class);
				for (SystemRecord systemRecord : systemInfo.getSystemsMap().values()) {
					if (!systemRecord.getID().equals(SystemInfo.SYSTEM_ROOT)) {
						selectSystem.addItem(systemRecord.getID());
						selectSystem.setItemCaption(systemRecord.getID(), systemRecord.getName());
					}
				}
				selectSystem.select(systemInfo.getCurrentID());
				formLayout.addComponent(selectSystem);
				selectSystem.addValueChangeListener(new ValueChangeListener() {
					private static final long serialVersionUID = 0x4C656F6E6172646FL;

					public void valueChange(ValueChangeEvent event) {
						String systemID = (String) event.getProperty().getValue();
						Backups backups = new Backups(systemID, null);
						backupsList = backups.getBackupsList();
						selectPrevBackup.removeAllItems();
						firstItem = null;
						if (backupsList != null && backupsList.size() > 0) {
							Collection<BackupRecord> set = backupsList.values();
							Iterator<BackupRecord> iter = set.iterator();
							while (iter.hasNext()) {
								BackupRecord backupRecord = iter.next();
								String backupID = backupRecord.getID();
								selectPrevBackup.addItem(backupID);
								selectPrevBackup.setItemCaption(backupID, backupRecord.getStarted());
								if (firstItem == null) {
									firstItem = backupID;
									selectPrevBackup.select(firstItem);
								}
							}
							runningTask.getControlsLayout().setEnabled(true);
						} else {
							displayBackupInfo(backupInfoLayout, new BackupRecord());
							runningTask.getControlsLayout().setEnabled(false);
						}
					}
				});
			}
			prevBackupsLayout = new HorizontalLayout();
			restoreLayout.addComponent(prevBackupsLayout);

			selectPrevBackup = (commandEnum == Command.backup) ? new ListSelect("Backups") : new ListSelect();
			selectPrevBackup.setImmediate(true);
			selectPrevBackup.setNullSelectionAllowed(false);
			selectPrevBackup.setRows(8); // Show a few items and a scrollbar if there are more
			selectPrevBackup.setWidth("16em");
			prevBackupsLayout.addComponent(selectPrevBackup);
			final Backups backups = new Backups(nodeInfo.getParentID(), null);
			backupsList = backups.getBackupsForNode(nodeInfo.getID());
			if (backupsList != null && backupsList.size() > 0) {
				Collection<BackupRecord> set = backupsList.values();
				Iterator<BackupRecord> iter = set.iterator();
				while (iter.hasNext()) {
					BackupRecord backupRecord = iter.next();
					selectPrevBackup.addItem(backupRecord.getID());
					selectPrevBackup.setItemCaption(backupRecord.getID(), backupRecord.getStarted());
					if (firstItem == null) {
						firstItem = backupRecord.getID();
					}
				}

				backupInfoLayout = new VerticalLayout();
				backupInfoLayout.setMargin(new MarginInfo(false, true, false, true));
				prevBackupsLayout.addComponent(backupInfoLayout);
				prevBackupsLayout.setComponentAlignment(backupInfoLayout, Alignment.MIDDLE_CENTER);

				selectPrevBackup.addValueChangeListener(new ValueChangeListener() {
					private static final long serialVersionUID = 0x4C656F6E6172646FL;

					public void valueChange(ValueChangeEvent event) {
						String backupID = (String) event.getProperty().getValue();
						if (backupID == null) {
							isParameterReady = false;
							runningTask.getControlsLayout().setEnabled(isParameterReady);
							return;
						}
						BackupRecord backupRecord = backupsList.get(backupID);
						displayBackupInfo(backupInfoLayout, backupRecord);
						if (backupLevel != null) {
							runningTask.selectParameter("Incremental " + backupRecord.getID());
						} else {
							runningTask.selectParameter(backupRecord.getID());
						}
						isParameterReady = true;
						VerticalLayout controlsLayout = runningTask.getControlsLayout();
						if (controlsLayout != null) {
							controlsLayout.setEnabled(isParameterReady);
						}
					}
				});

				// final DisplayBackupRecord displayRecord = new
				// DisplayBackupRecord(parameterLayout);

				if (commandEnum == Command.restore) {
					restoreLayout.addComponent(prevBackupsLayout);
					selectPrevBackup.select(firstItem);
				}

			} else {
				// no previous backups
				if (commandEnum == Command.backup) {
					backupLevel.setEnabled(false);
					isParameterReady = true;
				} else if (commandEnum == Command.restore) {
					//runningTask.getControlsLayout().setEnabled(false);
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

		default:
			isParameterReady = true;
			break;

		}

	}

	public boolean isParameterReady() {
		return isParameterReady;
	}

	private ValueChangeListener connectParamsListener = new ValueChangeListener() {
		private static final long serialVersionUID = 0x4C656F6E6172646FL;

		public void valueChange(ValueChangeEvent event) {
			String password = connectPassword.getValue();
			String sshkey = connectKey.getValue();
			if (password != null || sshkey != null) {
				String params = "rootpassword=" + password + "&sshkey=" + sshkey;
				runningTask.selectParameter(params);
				isParameterReady = true;
			} else {
				isParameterReady = false;
			}
			runningTask.getControlsLayout().setEnabled(isParameterReady);
		}
	};

	private String backupLabels[] = { "Node", "Level", "State", "Size", "Restored" };

	final public void displayBackupInfo(VerticalLayout layout, BackupRecord record) {
		String value;
		String values[] = { (value = record.getNode()) != null ? value : NOT_AVAILABLE, (value = record.getLevel()) != null ? value : NOT_AVAILABLE,
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

		//		SystemInfo systemInfo = VaadinSession.getCurrent().getAttribute(SystemInfo.class);
		//		LinkedHashMap<String, String> sysProperties = systemInfo.getCurrentSystem().getProperties();
		//		String EIP = sysProperties.get(SystemInfo.PROPERTY_EIP);
		//		if (EIP != null) {
		//			String url = "http://" + EIP + "/consoleAPI/" + record.getLog();
		//			Link newBackupLogLink = new Link("Backup Log", new ExternalResource(url));
		//			newBackupLogLink.setTargetName("_blank");
		//			newBackupLogLink.setDescription("Open backup log in a new window");
		//			newBackupLogLink.setIcon(new ThemeResource("img/externalLink.png"));
		//			newBackupLogLink.addStyleName("icon-after-caption");
		//			if (backupLogLink == null) {
		//				layout.addComponent(newBackupLogLink);
		//				backupLogLink = newBackupLogLink;
		//			} else {
		//				layout.replaceComponent(backupLogLink, newBackupLogLink);
		//				backupLogLink = newBackupLogLink;
		//			}
		//		}
	}
}