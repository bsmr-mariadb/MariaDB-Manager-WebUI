package com.skysql.consolev.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

import com.skysql.consolev.BackupRecord;
import com.skysql.consolev.SessionData;
import com.skysql.consolev.TaskRecord;
import com.skysql.consolev.api.BackupCommands;
import com.skysql.consolev.api.BackupStates;
import com.skysql.consolev.api.Backups;
import com.skysql.consolev.api.CommandStates;
import com.skysql.consolev.api.NodeInfo;
import com.skysql.consolev.api.TaskInfo;
import com.skysql.consolev.api.UserInfo;
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
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class PanelBackup {

	private static final String NOT_AVAILABLE = "n/a";
	private static final String SYSTEM_NODEID = "0";

	private NodeInfo nodeInfo;
	private VerticalLayout commandsLayout, runningContainerLayout, placeholderLayout;
	private HorizontalLayout newLayout, logsLayout, backupsLayout;
	private ListSelect commandSelect;
	private String[] oldcommands;
	private UserInfo userInfo;
	private Table logsTable, backupsTable;
	private int oldLogsCount, oldBackupsCount;
	private ArrayList<TaskRecord> tasksList;
	private LinkedHashMap<String, BackupRecord> backupsList;
	final LinkedHashMap<String, String> names = BackupCommands.getNames();

	PanelBackup(HorizontalLayout thisTab) {
		thisTab.setSizeFull();
		thisTab.addStyleName("backupTab");
		thisTab.setSpacing(true);

		TabSheet tabsheet = new TabSheet();
		tabsheet.setSizeFull();

		newLayout = new HorizontalLayout();

		logsLayout = new HorizontalLayout();
		logsLayout.setSpacing(true);
		logsLayout.setMargin(true);

		backupsLayout = new HorizontalLayout();
		backupsLayout.setSpacing(true);
		backupsLayout.setMargin(true);

		// Add the components as tabs in the Accordion.
		tabsheet.addTab(newLayout).setCaption("New");
		tabsheet.addTab(logsLayout).setCaption("Logs");
		tabsheet.addTab(backupsLayout).setCaption("Backups");
		thisTab.addComponent(tabsheet);

		/*** NEW **********************************************/

		// COMMANDS
		commandsLayout = new VerticalLayout();
		commandsLayout.addStyleName("instructions");
		commandsLayout.setSizeUndefined();
		commandsLayout.setSpacing(true);
		commandsLayout.setMargin(true);
		newLayout.addComponent(commandsLayout);

		commandSelect = new ListSelect("Commands");
		commandSelect.setImmediate(true);
		commandSelect.setNullSelectionAllowed(false);
		commandSelect.setWidth("12em");
		commandSelect.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {
				String name = (String) event.getProperty().getValue();
				for (final String id : names.keySet()) {
					if (names.get(id).equalsIgnoreCase(name)) {
						selectCommand(id);
					}
				}
			}
		});

		commandsLayout.addComponent(commandSelect);
		commandsLayout.setComponentAlignment(commandSelect, Alignment.MIDDLE_CENTER);

		// Scripting layout placeholder
		placeholderLayout = new VerticalLayout();
		placeholderLayout.addStyleName("placeholderLayout");
		placeholderLayout.setSizeUndefined();

		Label placeholderLabel = new Label("No Backup is currently running for this node");
		placeholderLabel.addStyleName("placeholder");
		placeholderLayout.addComponent(placeholderLabel);

		newLayout.addComponent(placeholderLayout);
		newLayout.setComponentAlignment(placeholderLayout, Alignment.MIDDLE_CENTER);
		runningContainerLayout = placeholderLayout;

		/*** LOGS **********************************************/

		logsTable = new Table(null);
		logsTable.setImmediate(true);
		// logsTable.setSelectable(true);

		logsTable.addContainerProperty("Started", String.class, null);
		logsTable.addContainerProperty("Completed", String.class, null);
		logsTable.addContainerProperty("Command", String.class, null);
		logsTable.addContainerProperty("Parameters", String.class, null);
		logsTable.addContainerProperty("User", String.class, null);
		logsTable.addContainerProperty("Status", String.class, null);

		final VerticalLayout backupInfoLayout = new VerticalLayout();
		backupInfoLayout.setMargin(true);

		/***
		 * logsTable.addListener(new Property.ValueChangeListener() { private
		 * static final long serialVersionUID = 0x4C656F6E6172646FL; public void
		 * valueChange(ValueChangeEvent event) { String backupID =
		 * (String)logsTable.getValue(); log("backupID: " + backupID);
		 * displayBackupInfo(backupInfoLayout, backupsList.get(backupID)); } });
		 ***/

		logsLayout.addComponent(logsTable);
		logsLayout.setComponentAlignment(logsTable, Alignment.MIDDLE_CENTER);

		logsLayout.addComponent(backupInfoLayout);

		userInfo = new UserInfo("dummy");

		/*** BACKUPS **********************************************/

		backupsTable = new Table(null);
		backupsTable.addContainerProperty("Started", String.class, null);
		backupsTable.addContainerProperty("Completed", String.class, null);
		backupsTable.addContainerProperty("Level", String.class, null);
		backupsTable.addContainerProperty("Storage", String.class, null);
		backupsTable.addContainerProperty("Restored", String.class, null);
		backupsTable.addContainerProperty("Status", String.class, null);
		backupsTable.addContainerProperty("Log", Link.class, null);

		backupsLayout.addComponent(backupsTable);
		backupsLayout.setComponentAlignment(backupsTable, Alignment.MIDDLE_CENTER);

	}

	public void refresh(NodeInfo nodeInfo) {
		this.nodeInfo = nodeInfo;

		String taskID = nodeInfo.getTask();
		// String taskCommand = nodeInfo.getCommand();
		RunningTask runningTask = nodeInfo.getBackupTask();
		String commands[] = nodeInfo.getCommands();

		if (nodeInfo.getNodeID().equalsIgnoreCase(SYSTEM_NODEID))
			return; // we shouldn't get here, since we are supposed to
					// only do Nodes, not the System

		TaskInfo taskInfo = new TaskInfo(null, null, "backup", nodeInfo.getNodeID());
		tasksList = taskInfo.getTasksList();
		if (tasksList != null) {
			int size = tasksList.size();
			if (oldLogsCount != size) {
				oldLogsCount = size;

				logsTable.removeAllItems();
				for (TaskRecord taskRecord : tasksList) {
					logsTable.addItem(new Object[] { taskRecord.getStart(), taskRecord.getEnd(), names.get(taskRecord.getCommand()), taskRecord.getParams(),
							userInfo.findNameByID(taskRecord.getUser()), CommandStates.getDescriptions().get(taskRecord.getStatus()) }, taskRecord.getID());
				}
			}
		} else {
			logsTable.removeAllItems();
		}

		SessionData userData = VaadinSession.getCurrent().getAttribute(SessionData.class);
		LinkedHashMap<String, String> sysProperties = userData.getSystemProperties().getProperties();
		String EIP = sysProperties.get("EIP");
		Link backupLogLink;

		Backups backups = new Backups(nodeInfo.getSystemID(), null);
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

		if (taskID != null) {
			commandSelect.setEnabled(false);

			/***
			 * if (runningTask == null &&
			 * (nodeInfo.getCommand().equalsIgnoreCase(CMD_BACKUP) ||
			 * nodeInfo.getCommand().equalsIgnoreCase(CMD_RESTORE))) {
			 * runningTask = new RunningTask(null, nodeInfo);
			 * runningTask.activateTimer();
			 ***/

		} else {
			if (!Arrays.equals(commands, oldcommands)) {
				oldcommands = commands;
				// rebuild list of commands with what node is accepting
				String selected = (runningTask != null) ? runningTask.getCommand() : null;
				commandSelect.removeAllItems();
				if ((commands != null) && (commands.length != 0)) {
					for (String commandID : commands) {
						String name = names.get(commandID);
						if (name != null)
							commandSelect.addItem(name);
					}
					if (selected != null) {
						commandSelect.select(names.get(selected));
					}
					commandSelect.setVisible(true);
				} else {
					commandSelect.setVisible(false);
				}
			}

		}

		if (runningTask != null) {
			VerticalLayout newScriptingLayout = runningTask.getLayout();
			newLayout.replaceComponent(runningContainerLayout, newScriptingLayout);
			runningContainerLayout = newScriptingLayout;
		} else if (runningContainerLayout != placeholderLayout) {
			newLayout.replaceComponent(runningContainerLayout, placeholderLayout);
			newLayout.setComponentAlignment(placeholderLayout, Alignment.MIDDLE_CENTER);
			runningContainerLayout = placeholderLayout;
		}

	}

	public void selectCommand(String command) {
		RunningTask runningTask = nodeInfo.getBackupTask();

		if (runningTask != null) {
			runningTask.close();
		}

		// SessionData userData =
		// VaadinSession.getCurrent().getAttribute(SessionData.class);
		// runningTask = new RunningTask(command, nodeInfo, userData,
		// commandSelect);
		runningTask = new RunningTask(command, nodeInfo, commandSelect);

		// add SCRIPTING layout
		VerticalLayout newScriptingLayout = runningTask.getLayout();
		newLayout.replaceComponent(runningContainerLayout, newScriptingLayout);
		newLayout.setComponentAlignment(newScriptingLayout, Alignment.MIDDLE_LEFT);
		runningContainerLayout = newScriptingLayout;

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

		SessionData userData = VaadinSession.getCurrent().getAttribute(SessionData.class);
		LinkedHashMap<String, String> sysProperties = userData.getSystemProperties().getProperties();
		String EIP = sysProperties.get("EIP");
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

	private static void log(String aMsg) {
		System.out.println(aMsg);
	}

}
