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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.ScheduledFuture;

import com.skysql.manager.BackupRecord;
import com.skysql.manager.ExecutorFactory;
import com.skysql.manager.ManagerUI;
import com.skysql.manager.TaskRecord;
import com.skysql.manager.api.BackupStates;
import com.skysql.manager.api.Backups;
import com.skysql.manager.api.NodeInfo;
import com.skysql.manager.api.SystemInfo;
import com.skysql.manager.api.TaskInfo;
import com.skysql.manager.api.TaskRun;
import com.skysql.manager.api.UserObject;
import com.skysql.manager.ui.components.ScriptingControlsLayout;
import com.skysql.manager.ui.components.ScriptingControlsLayout.Controls;
import com.skysql.manager.ui.components.ScriptingProgressLayout;
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
import com.vaadin.ui.VerticalLayout;

public final class RunningTask {

	private static final int SHORT_REFRESH_DELAY = 3;
	private static final String NOT_AVAILABLE = "n/a";
	private static final String CMD_BACKUP = "backup";
	private static final String CMD_RESTORE = "restore";
	private VerticalLayout containerLayout;
	private ScriptingControlsLayout scriptingControlsLayout;
	private ScriptingProgressLayout scriptingProgressLayout;
	private HorizontalLayout scriptingLayout;
	private ScheduledFuture<?> runTimerFuture;
	private String command, params;
	private NodeInfo nodeInfo;
	private TaskRecord taskRecord;
	private boolean observerMode;
	private boolean paramsReady;
	private GridLayout backupInfoGrid;
	private Link backupLogLink;
	private ListSelect commandSelect;
	private OptionGroup backupLevel;
	private HorizontalLayout prevBackupsLayout;
	private HorizontalLayout parameterLayout = new HorizontalLayout();
	private ListSelect selectPrevBackup;
	private String firstItem;
	private ValueChangeListener listener;

	RunningTask(String command, NodeInfo nodeInfo, ListSelect commandSelect) {
		this.command = command;
		this.nodeInfo = nodeInfo;
		this.commandSelect = commandSelect;

		ManagerUI.log("RunningTask - command: " + command + ", node: " + nodeInfo.getName());

		if (command == null) {
			observerMode = true;
			TaskInfo taskInfo = new TaskInfo(nodeInfo.getTask(), null, null);
			taskRecord = taskInfo.getTasksList().get(0);
			command = taskRecord.getCommand();
			this.command = command;
		}

		nodeInfo.setCommandTask(this);

		containerLayout = new VerticalLayout();
		containerLayout.addStyleName("containerLayout");
		containerLayout.setSizeFull();

		scriptingLayout = new HorizontalLayout();
		scriptingLayout.setSpacing(true);
		scriptingLayout.setSizeFull();
		containerLayout.addComponent(scriptingLayout);

		if (command.equals(CMD_BACKUP) || command.equals(CMD_RESTORE)) {

			// add PARAMETER layout
			parameterLayout.setSizeFull();
			parameterLayout.setSpacing(true);
			parameterLayout.setMargin(true);
			scriptingLayout.addComponent(parameterLayout);
			scriptingLayout.setComponentAlignment(parameterLayout, Alignment.MIDDLE_LEFT);

			// COLUMN 1. PARAMETERS
			if (command.equalsIgnoreCase(CMD_BACKUP)) {
				backupLevel = new OptionGroup("Backup Level");
				backupLevel.setImmediate(true);
				backupLevel.addItem("Full");
				backupLevel.addItem("Incremental");
				parameterLayout.addComponent(backupLevel);
				parameterLayout.setComponentAlignment(backupLevel, Alignment.MIDDLE_LEFT);
				backupLevel.addValueChangeListener(new ValueChangeListener() {
					private static final long serialVersionUID = 0x4C656F6E6172646FL;

					public void valueChange(ValueChangeEvent event) {
						String level = (String) event.getProperty().getValue();
						selectParameter(level);
						if (level.equalsIgnoreCase("Incremental")) {
							parameterLayout.addComponent(prevBackupsLayout);
							selectPrevBackup.select(firstItem);

						} else {
							if (parameterLayout.getComponentIndex(prevBackupsLayout) != -1) {
								parameterLayout.removeComponent(prevBackupsLayout);
							}
						}
					}
				});

				backupLevel.setValue("Full");

			}

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
									selectParameter("Incremental " + backupRecord.getID());
								} else {
									selectParameter(backupRecord.getID());
								}
								scriptingControlsLayout.enableControls(true, Controls.run);
								break;
							}
						}

					}
				});

				// final DisplayBackupRecord displayRecord = new
				// DisplayBackupRecord(parameterLayout);

				if (command.equalsIgnoreCase(CMD_BACKUP)) {

				} else if (command.equalsIgnoreCase(CMD_RESTORE)) {
					parameterLayout.addComponent(prevBackupsLayout);
					selectPrevBackup.select(firstItem);

				}

			} else {
				// no previous backups
				if (command.equalsIgnoreCase(CMD_BACKUP)) {
					backupLevel.setEnabled(false);

				} else if (command.equalsIgnoreCase(CMD_RESTORE)) {

					Label placeholderLabel = new Label("No Backups are available for Restore");
					placeholderLabel.addStyleName("instructions");
					placeholderLabel.setSizeUndefined();
					containerLayout.replaceComponent(scriptingLayout, placeholderLabel);
					containerLayout.setComponentAlignment(placeholderLabel, Alignment.MIDDLE_CENTER);
				}
			}

		}

		// COLUMN 2. CONTROLS
		// controls = taskRun.getControls(); this is for when they are server-side driven
		scriptingControlsLayout = new ScriptingControlsLayout(this, new Controls[] { Controls.run, Controls.schedule, Controls.stop });
		scriptingLayout.addComponent(scriptingControlsLayout);
		scriptingLayout.setComponentAlignment(scriptingControlsLayout, Alignment.MIDDLE_LEFT);

		// this needs to be done properly
		if (!command.equals(CMD_RESTORE) && !observerMode) {
			scriptingControlsLayout.enableControls(true, Controls.run);
		}

		// COLUMN 3. PROGRESS & RESULT
		scriptingProgressLayout = new ScriptingProgressLayout(this, observerMode);
		scriptingLayout.addComponent(scriptingProgressLayout);
		scriptingLayout.setComponentAlignment(scriptingProgressLayout, Alignment.MIDDLE_LEFT);

		scriptingProgressLayout.buildProgress(taskRecord, command, nodeInfo.getCommands().getSteps(command));

		if (observerMode) {
			activateTimer();
		}
	}

	public VerticalLayout getLayout() {
		return containerLayout;
	}

	public String getCommand() {
		return command;
	}

	private String backupLabels[] = { "Node", "Level", "State", "Size", "Completed", "Restored" };

	final public void displayBackupInfo(VerticalLayout layout, BackupRecord record) {
		String value;
		String values[] = { (value = record.getID()) != null ? value : NOT_AVAILABLE, (value = record.getLevel()) != null ? value : NOT_AVAILABLE,
				((value = record.getState()) != null) && (value = BackupStates.getDescriptions().get(value)) != null ? value : "Invalid",
				(value = record.getSize()) != null ? value : NOT_AVAILABLE, (value = record.getUpdated()) != null ? value : NOT_AVAILABLE,
				(value = record.getRestored()) != null ? value : NOT_AVAILABLE };

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

	public void selectParameter(String parameter) {
		params = parameter;

		if (!paramsReady) {
			paramsReady = true;

			//scriptingControlsLayout.enableControls(true, Controls.run);
		}
	}

	public void controlClicked(Controls control) {
		switch (control) {
		case run:
			start();
			break;

		case schedule:
			schedule();
			break;

		case pause:
			pause();
			break;

		case stop:
			stop();
			break;

		}
	}

	void start() {
		// disable further command selection immediately
		commandSelect.setEnabled(false);
		parameterLayout.setEnabled(false);

		scriptingProgressLayout.start();

		UserObject userObject = VaadinSession.getCurrent().getAttribute(UserObject.class);
		String userID = userObject.getUserID();
		String looseExecution = userObject.getProperty(UserObject.PROPERTY_COMMAND_EXECUTION);
		String state = (looseExecution != null && Boolean.valueOf(looseExecution)) ? null : nodeInfo.getState();

		TaskRun taskRun = new TaskRun(nodeInfo.getParentID(), nodeInfo.getID(), userID, command, params, state);
		if (taskRun.getTaskRecord() == null) {
			commandSelect.select(null);
			commandSelect.setEnabled(true);
			parameterLayout.setEnabled(true);
			scriptingProgressLayout.setResult("Failed to launch: " + taskRun.getError());
			OverviewPanel overviewPanel = VaadinSession.getCurrent().getAttribute(OverviewPanel.class);
			overviewPanel.refresh();
			return;
		}

		String taskSteps = taskRun.getTaskRecord().getSteps();
		String nodeSteps = nodeInfo.getCommands().getSteps(command);
		if (!taskSteps.equals(nodeSteps)) {
			scriptingProgressLayout.buildProgress(taskRecord, command, taskSteps);
			scriptingProgressLayout.setTitle(command + " (Updated Steps)");
		}

		nodeInfo.setTask(taskRun.getTaskRecord().getID());
		taskRecord = taskRun.getTaskRecord();

		activateTimer();

	}

	void schedule() {
	}

	void stop() {
	}

	void pause() {

	}

	public void close() {
		// make sure timers get stopped
		if (runTimerFuture != null) {
			ManagerUI.log(nodeInfo.getTask() + " - removeTimer");
			ExecutorFactory.removeTimer(runTimerFuture);
			runTimerFuture = null;
		}

		//		if (cancelTimerFuture != null)
		//			cancelTimerFuture.cancel(DONT_INTERRUPT_IF_RUNNING);

		command = null;
		commandSelect.select(null);
		commandSelect.setEnabled(true);

		if (listener != null) {
			listener.valueChange(null);
		}

	}

	public void addRefreshListener(ValueChangeListener listener) {
		this.listener = listener;
	}

	public void activateTimer() {
		// if timer not running yet
		if (runTimerFuture == null) {
			ManagerUI.log(nodeInfo.getTask() + " - activateTimer");

			final long fDelayBetweenRuns = SHORT_REFRESH_DELAY;
			Runnable runTimerTask = new RunTimerTask();
			runTimerFuture = ExecutorFactory.addTimer(runTimerTask, fDelayBetweenRuns);

			//			final long fShutdownAfter = 60 * 10;
			//			Runnable stopTimer = new StopTimerTask(runTimerFuture);
			//			cancelTimerFuture = fScheduler.schedule(stopTimer, fShutdownAfter, TimeUnit.SECONDS);
		} else {
			// cancel StopTimerTask and restart with full timeout

		}

	}

	private final class RunTimerTask implements Runnable {
		private int fCount;

		public void run() {
			++fCount;
			ManagerUI.log("timer - task:" + nodeInfo.getTask() + " - " + fCount);

			TaskInfo taskInfo = new TaskInfo(nodeInfo.getTask(), null, null);
			TaskRecord taskRecord = taskInfo.getTasksList().get(0);

			scriptingProgressLayout.refresh(taskInfo, taskRecord);

		}
	}

	//	private final class StopTimerTask implements Runnable {
	//		StopTimerTask(ScheduledFuture<?> aSchedFuture) {
	//			fSchedFuture = aSchedFuture;
	//		}
	//
	//		public void run() {
	//			ManagerUI.log(nodeInfo.getTask() + " - Stopping Timer.");
	//			fSchedFuture.cancel(true);
	//
	//			lastIndex = -1;
	//
	//		}
	//
	//		private ScheduledFuture<?> fSchedFuture;
	//	}

}
