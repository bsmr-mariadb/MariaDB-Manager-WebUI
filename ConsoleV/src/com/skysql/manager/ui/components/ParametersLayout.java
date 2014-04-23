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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.skysql.java.Encryption;
import com.skysql.java.Logging;
import com.skysql.manager.BackupRecord;
import com.skysql.manager.Commands;
import com.skysql.manager.Commands.Command;
import com.skysql.manager.SystemRecord;
import com.skysql.manager.api.APIrestful;
import com.skysql.manager.api.BackupStates;
import com.skysql.manager.api.Backups;
import com.skysql.manager.api.NodeInfo;
import com.skysql.manager.api.SystemInfo;
import com.skysql.manager.ui.RunningTask;
import com.skysql.manager.ui.components.ScriptingControlsLayout.Controls;
import com.skysql.manager.validators.Password2Validator;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.EmptyValueException;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Form;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

/**
 * The Class ParametersLayout.
 */
@SuppressWarnings("serial")
public class ParametersLayout extends HorizontalLayout {

	private static final String NOT_AVAILABLE = "n/a";

	public static final String PARAM_CONNECT_ROOTPASSWORD = "xparam-rootpassword";
	public static final String PARAM_CONNECT_SSHKEY = "xparam-sshkey";
	private static final String PARAM_BACKUP_TYPE = "param-type";
	private static final String PARAM_BACKUP_TYPE_FULL = "1";
	private static final String PARAM_BACKUP_TYPE_INCREMENTAL = "2";
	private static final String PARAM_BACKUP_PARENT = "param-parent";
	private static final String PARAM_BACKUP_ID = "param-id";

	private OptionGroup backupLevel;
	private HorizontalLayout prevBackupsLayout;
	private ListSelect selectPrevBackup;
	private String firstItem;
	private GridLayout backupInfoGrid;
	//private Link backupLogLink;
	private RunningTask runningTask;
	private LinkedHashMap<String, BackupRecord> backupsList;
	private Map<String, String> params;
	private VerticalLayout backupInfoLayout;
	private boolean isParameterReady = false;
	private boolean usePassword = false;

	private final Form form = new Form();
	private final PasswordField connectPassword = new PasswordField("Root Password");
	private final PasswordField connectPassword2 = new PasswordField("Confirm Password");
	private final TextArea connectKey = new TextArea("SSH Key");
	//private final Validator connectValidator = new PasswordOrKeyValidator(connectPassword);
	private final OptionGroup passwordOption = new OptionGroup();

	/**
	 * Instantiates a new parameters layout.
	 *
	 * @param runningTask the running task
	 * @param nodeInfo the node info
	 * @param commandEnum the command enum
	 */
	public ParametersLayout(final RunningTask runningTask, final NodeInfo nodeInfo, Commands.Command commandEnum) {
		this.runningTask = runningTask;

		addStyleName("parametersLayout");
		setSizeFull();
		setSpacing(true);
		setMargin(true);

		params = new HashMap<String, String>();
		runningTask.selectParameter(params);

		switch (commandEnum) {
		case backup:
			backupLevel = new OptionGroup("Backup Level");
			backupLevel.setImmediate(true);
			backupLevel.addItem(PARAM_BACKUP_TYPE_FULL);
			backupLevel.setItemCaption(PARAM_BACKUP_TYPE_FULL, "Full");
			backupLevel.addItem(PARAM_BACKUP_TYPE_INCREMENTAL);
			backupLevel.setItemCaption(PARAM_BACKUP_TYPE_INCREMENTAL, "Incremental");
			addComponent(backupLevel);
			setComponentAlignment(backupLevel, Alignment.MIDDLE_LEFT);
			backupLevel.addValueChangeListener(new ValueChangeListener() {
				private static final long serialVersionUID = 0x4C656F6E6172646FL;

				public void valueChange(ValueChangeEvent event) {
					String level = (String) event.getProperty().getValue();
					params.put(PARAM_BACKUP_TYPE, level);
					if (level.equals(PARAM_BACKUP_TYPE_INCREMENTAL)) {
						addComponent(prevBackupsLayout);
						selectPrevBackup.select(firstItem);
					} else {
						if (getComponentIndex(prevBackupsLayout) != -1) {
							removeComponent(prevBackupsLayout);
						}
					}
				}
			});

			backupLevel.setValue(PARAM_BACKUP_TYPE_FULL);
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
							runningTask.getControlsLayout().enableControls(true, Controls.Run);
						} else {
							if (backupInfoLayout != null) {
								displayBackupInfo(backupInfoLayout, new BackupRecord());
							}
							runningTask.getControlsLayout().enableControls(false, Controls.Run);
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
							runningTask.getControlsLayout().enableControls(isParameterReady, Controls.Run);
							return;
						}
						BackupRecord backupRecord = backupsList.get(backupID);
						displayBackupInfo(backupInfoLayout, backupRecord);
						if (backupLevel != null) {
							// we're doing a backup
							params.put(PARAM_BACKUP_PARENT, backupRecord.getID());
						} else {
							// we're doing a restore
							params.put(PARAM_BACKUP_ID, backupRecord.getID());
						}
						isParameterReady = true;
						ScriptingControlsLayout controlsLayout = runningTask.getControlsLayout();
						if (controlsLayout != null) {
							controlsLayout.enableControls(isParameterReady, Controls.Run);
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
					//runningTask.getControlsLayout().enableControls(false, Controls.run);
				}
			}
			break;

		case connect:
			VerticalLayout connectLayout = new VerticalLayout();
			addComponent(connectLayout);

			final Validator validator = new Password2Validator(connectPassword);

			passwordOption.addItem(true);
			passwordOption.setItemCaption(true, "Authenticate with root user");
			passwordOption.addItem(false);
			passwordOption.setItemCaption(false, "Authenticate with SSH Key");
			passwordOption.setImmediate(true);
			passwordOption.addValueChangeListener(new Property.ValueChangeListener() {
				private static final long serialVersionUID = 0x4C656F6E6172646FL;

				@Override
				public void valueChange(ValueChangeEvent event) {
					usePassword = (Boolean) event.getProperty().getValue();
					if (usePassword) {
						connectPassword2.addValidator(validator);
					} else {
						connectPassword2.removeValidator(validator);
					}
					connectPassword.setVisible(usePassword);
					connectPassword.setRequired(usePassword);
					connectPassword2.setVisible(usePassword);
					connectPassword2.setRequired(usePassword);
					connectKey.setVisible(!usePassword);
					connectKey.setRequired(!usePassword);
					boolean isValid;
					if (runningTask.getControlsLayout() != null) {
						if (usePassword) {
							isValid = connectPassword.isValid();
						} else {
							isValid = connectKey.isValid();
						}
						if (isValid) {
							connectParamsListener.valueChange(null);
						} else {
							form.setComponentError(null);
							form.setValidationVisible(false);
							runningTask.getControlsLayout().enableControls(false, Controls.Run);
						}
					}
				}
			});
			connectLayout.addComponent(passwordOption);
			passwordOption.select(false);

			connectLayout.addComponent(form);
			form.setImmediate(true);
			form.setFooter(null);
			Layout layout = form.getLayout();

			form.addField("connectPassword", connectPassword);
			connectPassword.setImmediate(true);
			connectPassword.setRequiredError("Root Password is a required field");
			connectPassword.addValueChangeListener(connectParamsListener);

			form.addField("connectPassword2", connectPassword2);
			connectPassword2.setImmediate(true);
			connectPassword2.setRequiredError("Confirm Password is a required field");
			connectPassword2.addValueChangeListener(connectParamsListener);

			form.addField("connectKey", connectKey);
			connectKey.setStyleName("sshkey");
			connectKey.setColumns(41);
			connectKey.setImmediate(true);
			connectKey.setRequiredError("SSH Key is a required field");
			connectKey.addValueChangeListener(connectParamsListener);
			break;

		default:
			isParameterReady = true;
			break;

		}

	}

	/**
	 * Checks if is parameter ready.
	 *
	 * @return true, if is parameter ready
	 */
	public boolean isParameterReady() {
		return isParameterReady;
	}

	/** The connect params listener. */
	private ValueChangeListener connectParamsListener = new ValueChangeListener() {
		private static final long serialVersionUID = 0x4C656F6E6172646FL;

		public void valueChange(ValueChangeEvent event) {
			isParameterReady = false;
			if (validateConnectParams()) {
				String password = connectPassword.getValue();
				String sshkey = connectKey.getValue();
				if (!password.isEmpty() || !sshkey.isEmpty()) {
					Encryption encryption = new Encryption();
					if (usePassword) {
						params.put(PARAM_CONNECT_ROOTPASSWORD, encryption.encrypt(password, APIrestful.getKey()));
					} else {
						params.put(PARAM_CONNECT_SSHKEY, encryption.encrypt(sshkey, APIrestful.getKey()));
					}
					isParameterReady = true;
				}
			}
			runningTask.getControlsLayout().enableControls(isParameterReady, Controls.Run);
		}
	};

	/**
	 * Validate connect params.
	 *
	 * @return true, if successful
	 */
	public boolean validateConnectParams() {

		try {
			form.setComponentError(null);
			form.commit();

			return true;

		} catch (EmptyValueException e) {
			return false;
		} catch (InvalidValueException e) {
			return false;
		} catch (Exception e) {
			//			e.printStackTrace();
			Logging.error(e.getMessage());
			return false;
		}

	}

	/** The backup labels. */
	private String backupLabels[] = { "Node", "Level", "State", "Size", "Restored" };

	/**
	 * Display backup info.
	 *
	 * @param layout the layout
	 * @param record the record
	 */
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