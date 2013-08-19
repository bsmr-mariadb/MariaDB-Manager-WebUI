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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.skysql.manager.BackupRecord;
import com.skysql.manager.ExecutorFactory;
import com.skysql.manager.ManagerUI;
import com.skysql.manager.StepRecord;
import com.skysql.manager.TaskRecord;
import com.skysql.manager.api.BackupStates;
import com.skysql.manager.api.Backups;
import com.skysql.manager.api.CommandStates;
import com.skysql.manager.api.Commands;
import com.skysql.manager.api.NodeInfo;
import com.skysql.manager.api.Steps;
import com.skysql.manager.api.SystemInfo;
import com.skysql.manager.api.TaskInfo;
import com.skysql.manager.api.TaskRun;
import com.skysql.manager.api.UserInfo;
import com.skysql.manager.api.UserObject;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;

public final class RunningTask {

	private static final String NOT_AVAILABLE = "n/a";
	private static final String CMD_BACKUP = "7";
	private static final String CMD_RESTORE = "8";
	private static final String CMD_BACKUP2 = "9";
	private static final String CMD_RESTORE2 = "10";
	private VerticalLayout containerLayout, scriptingProgressLayout, scriptingControlsLayout, scriptingResultLayout;
	private HorizontalLayout scriptingLayout, progressIconsLayout;
	private Label scriptLabel, progressLabel, resultLabel;
	private ScheduledFuture<?> runTimerFuture, cancelTimerFuture;
	private long startTime, runningTime;
	private Embedded[] taskImages;
	private LinkedHashMap<String, NativeButton> ctrlButtons = new LinkedHashMap<String, NativeButton>();
	private String controls[], primitives[];
	private int lastIndex = -1, lastProgressIndex = 0;
	private String command, params;
	private NodeInfo nodeInfo;
	private TaskRecord taskRecord;
	private boolean observerMode;
	private boolean paramSelected;
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
			TaskInfo taskInfo = new TaskInfo(nodeInfo.getTask(), null);
			taskRecord = taskInfo.getTasksList().get(0);
			command = taskRecord.getCommand();
		}

		nodeInfo.setCommandTask(this);

		containerLayout = new VerticalLayout();
		containerLayout.addStyleName("containerLayout");
		containerLayout.setSizeFull();

		scriptingLayout = new HorizontalLayout();
		scriptingLayout.setSpacing(true);
		scriptingLayout.setSizeFull();
		containerLayout.addComponent(scriptingLayout);

		if (command.equalsIgnoreCase(CMD_BACKUP) || command.equalsIgnoreCase(CMD_RESTORE) || command.equalsIgnoreCase(CMD_BACKUP2)
				|| command.equalsIgnoreCase(CMD_RESTORE2)) {

			// add PARAMETER layout
			parameterLayout.setSizeFull();
			parameterLayout.setSpacing(true);
			parameterLayout.setMargin(true);
			scriptingLayout.addComponent(parameterLayout);
			scriptingLayout.setComponentAlignment(parameterLayout, Alignment.MIDDLE_LEFT);

			// COLUMN 1. PARAMETERS
			if (command.equalsIgnoreCase(CMD_BACKUP) || command.equalsIgnoreCase(CMD_BACKUP2)) {
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
			final Backups backups = new Backups(nodeInfo.getSystemID(), null);
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
								break;
							}
						}

					}
				});

				// final DisplayBackupRecord displayRecord = new
				// DisplayBackupRecord(parameterLayout);

				if (command.equalsIgnoreCase(CMD_BACKUP) || command.equalsIgnoreCase(CMD_BACKUP2)) {

				} else if (command.equalsIgnoreCase(CMD_RESTORE) || command.equalsIgnoreCase(CMD_RESTORE2)) {
					parameterLayout.addComponent(prevBackupsLayout);
					selectPrevBackup.select(firstItem);

				}

			} else { // no previous backups
				if (command.equalsIgnoreCase(CMD_BACKUP) || command.equalsIgnoreCase(CMD_BACKUP2)) {
					backupLevel.setEnabled(false);

				} else if (command.equalsIgnoreCase(CMD_RESTORE) || command.equalsIgnoreCase(CMD_RESTORE2)) {

					Label placeholderLabel = new Label("No Backups are available for Restore");
					placeholderLabel.addStyleName("instructions");
					placeholderLabel.setSizeUndefined();
					containerLayout.replaceComponent(scriptingLayout, placeholderLabel);
					containerLayout.setComponentAlignment(placeholderLabel, Alignment.MIDDLE_CENTER);
				}
			}

		}

		// COLUMN 2. CONTROLS
		scriptingControlsLayout = new VerticalLayout();
		scriptingControlsLayout.addStyleName("scriptingControlsLayout");
		scriptingControlsLayout.setSizeFull();
		scriptingControlsLayout.setSpacing(true);
		scriptingControlsLayout.setMargin(true);
		scriptingLayout.addComponent(scriptingControlsLayout);
		scriptingLayout.setComponentAlignment(scriptingControlsLayout, Alignment.MIDDLE_LEFT);

		// COLUMN 3. PROGRESS
		scriptingProgressLayout = new VerticalLayout();
		scriptingProgressLayout.addStyleName("scriptingProgressLayout");
		//scriptingProgressLayout.setSizeFull();
		scriptingProgressLayout.setSpacing(true);
		scriptingProgressLayout.setMargin(true);
		scriptingLayout.addComponent(scriptingProgressLayout);
		scriptingLayout.setComponentAlignment(scriptingProgressLayout, Alignment.MIDDLE_LEFT);

		scriptLabel = new Label("");
		scriptLabel.addStyleName("instructions");
		scriptingProgressLayout.addComponent(scriptLabel);
		scriptingProgressLayout.setComponentAlignment(scriptLabel, Alignment.TOP_CENTER);

		progressIconsLayout = new HorizontalLayout();
		progressIconsLayout.addStyleName("progressIconsLayout");
		scriptingProgressLayout.addComponent(progressIconsLayout);
		scriptingProgressLayout.setComponentAlignment(progressIconsLayout, Alignment.MIDDLE_CENTER);

		progressLabel = new Label("");
		progressLabel.setImmediate(true);
		scriptingProgressLayout.addComponent(progressLabel);
		scriptingProgressLayout.setComponentAlignment(progressLabel, Alignment.BOTTOM_CENTER);

		// COLUMN 4. RESULT
		scriptingResultLayout = new VerticalLayout();
		scriptingResultLayout.addStyleName("scriptingResultsLayout");
		scriptingResultLayout.setSizeFull();
		scriptingResultLayout.setSpacing(true);
		scriptingResultLayout.setMargin(true);
		scriptingLayout.addComponent(scriptingResultLayout);
		scriptingLayout.setComponentAlignment(scriptingResultLayout, Alignment.MIDDLE_LEFT);

		resultLabel = new Label("Has not run yet", Label.CONTENT_RAW);
		resultLabel.addStyleName("instructions");
		resultLabel.setImmediate(true);
		scriptingResultLayout.addComponent(resultLabel);
		scriptingResultLayout.setComponentAlignment(resultLabel, Alignment.MIDDLE_CENTER);

		// ******* BUILD COLUMN 2 - CONTROL
		String commandName = Commands.getNames().get(command);

		// observer mode
		if (observerMode) {
			// String userName = Users.getUserNames().get(taskRecord.getUser());
			String userID = taskRecord.getUserID();
			UserInfo userInfo = (UserInfo) VaadinSession.getCurrent().getAttribute(UserInfo.class);
			String userName = userInfo.findNameByID(userID);
			String started = taskRecord.getStart();

			final Label label = new Label("The " + commandName + " command<br>was started at " + started + "<br>by " + userName, Label.CONTENT_RAW);
			label.addStyleName("instructions");
			label.setImmediate(true);
			scriptingControlsLayout.addComponent(label);
			scriptingControlsLayout.setComponentAlignment(label, Alignment.TOP_CENTER);

		} else {
			// add task controls
			// controls = taskRun.getControls(); this is for when they are
			// server-side driven
			controls = new String[] { "run" };

			for (String control : controls) {
				final NativeButton button = new NativeButton();
				button.addStyleName(control);
				button.setImmediate(true);
				button.setDescription(control); // this should be a proper
												// description
				button.setData(control);
				scriptingControlsLayout.addComponent(button);
				scriptingControlsLayout.setComponentAlignment(button, Alignment.MIDDLE_CENTER);
				ctrlButtons.put(control, button);
				button.addClickListener(new Button.ClickListener() {
					private static final long serialVersionUID = 0x4C656F6E6172646FL;

					public void buttonClick(ClickEvent event) {
						event.getButton().setEnabled(false);
						scriptCommand((String) event.getButton().getData());
					}
				});
			}
		}

		// ********* BUILD COLUMN 3 - PROGRESS

		scriptLabel.setValue(commandName);
		/***
		 * { Embedded image = new Embedded(commandName, new
		 * ThemeResource("img/scripting/script_small.png"));
		 * image.addStyleName("stepIcons"); image.setImmediate(true);
		 * image.setAlternateText(commandName);
		 * image.setDescription(Commands.getDescriptions().get(command));
		 * progressIconsLayout.addComponent(image);
		 * progressIconsLayout.setComponentAlignment(image,
		 * Alignment.TOP_CENTER); }
		 ***/

		LinkedHashMap<String, StepRecord> stepRecords = Steps.getStepsList();

		String[] stepsIDs = Commands.getSteps(command);

		primitives = new String[stepsIDs.length];
		taskImages = new Embedded[stepsIDs.length + 1]; // allow for one more
														// for the "done" icon
		// add steps icons
		for (int index = 0; index < stepsIDs.length; index++) {
			String stepID = stepsIDs[index];
			StepRecord stepRecord = stepRecords.get(stepID);
			String iconName = stepRecord.getIcon();
			String description = stepRecord.getDescription();

			Embedded image = new Embedded(null, new ThemeResource("img/scripting/pending/" + iconName + ".png"));
			image.addStyleName("stepIcons");
			image.setImmediate(true);
			image.setAlternateText(stepID);
			image.setDescription(description);
			progressIconsLayout.addComponent(image);
			primitives[index] = iconName;
			taskImages[index] = image;

		}
		Embedded image = new Embedded(null, new ThemeResource("img/scripting/pending/done.png"));
		image.addStyleName("stepIcons");
		image.setImmediate(true);
		image.setAlternateText("Done");
		image.setDescription("Done");
		progressIconsLayout.addComponent(image);
		taskImages[stepsIDs.length] = image;

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
				((value = record.getStatus()) != null) && (value = BackupStates.getDescriptions().get(value)) != null ? value : "Invalid",
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

		if (!paramSelected) {
			paramSelected = true;

			// update enable/disabled state of control buttons
			// String controls[] = taskRun.getControls(); this is for when they
			// are server-side driven
			String controls[] = new String[] { "run" };
			for (String key : ctrlButtons.keySet()) {
				NativeButton button = ctrlButtons.get(key);
				button.setEnabled(Arrays.asList(controls).contains(key) ? true : false);
			}

		}
	}

	private void scriptCommand(String cmd) {
		if (cmd.equalsIgnoreCase("run")) {
			start();
		} else if (cmd.equalsIgnoreCase("pause")) {
			pause();
		} else if (cmd.equalsIgnoreCase("stop")) {
			stop();
		}
	}

	void start() {
		commandSelect.setEnabled(false); // disable command selection immediately
		parameterLayout.setEnabled(false);

		startTime = System.currentTimeMillis();
		resultLabel.setValue("Launching");

		UserObject userObject = VaadinSession.getCurrent().getAttribute(UserObject.class);
		String userID = userObject.getUserID();

		TaskRun taskRun = new TaskRun(nodeInfo.getSystemID(), nodeInfo.getID(), userID, command, params);
		nodeInfo.setTask(taskRun.getTask());

		activateTimer();
		/*
		 * To start the timer at a specific date in the future, the initial
		 * delay needs to be calculated relative to the current time, as in :
		 * Date futureDate = ... long startTime = futureDate.getTime() -
		 * System.currentTimeMillis();
		 */
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

			final long fDelayBetweenRuns = 3;
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

			TaskInfo taskInfo = new TaskInfo(nodeInfo.getTask(), null);
			TaskRecord taskRecord = taskInfo.getTasksList().get(0);

			VaadinSession vaadinSession = VaadinSession.getCurrent();
			vaadinSession.lock();

			try {
				String statusString;
				if ((statusString = taskRecord.getStatus()) == null) {
					return; // we're waiting for something to happen
				}
				resultLabel.setValue(CommandStates.getDescriptions().get(statusString));
				int status = Integer.parseInt(statusString);

				String indexString;
				if ((indexString = taskRecord.getIndex()) == null) {
					return; // we're waiting for something to happen
				}
				int index = Integer.parseInt(indexString) - 1;

				if (scriptingProgressLayout.isVisible()) {
					while (lastProgressIndex < index) {
						ManagerUI.log(nodeInfo.getTask() + " - updating last position");

						taskImages[lastProgressIndex].setSource(new ThemeResource("img/scripting/done/" + primitives[lastProgressIndex] + ".png"));
						lastProgressIndex++;
					}
				} else {
					ManagerUI.log(nodeInfo.getTask() + " - cannot update display");
				}

				if ((status == 2) && (index != lastIndex)) {
					if (scriptingProgressLayout.isVisible()) {
						ManagerUI.log(nodeInfo.getTask() + " - updating running position");

						taskImages[index].setSource(new ThemeResource("img/scripting/active/" + primitives[index] + ".png"));
						progressLabel.setValue(taskImages[index].getDescription());

					} else {
						ManagerUI.log(nodeInfo.getTask() + " - cannot update display");
					}
					lastIndex = index;

				} else if (status == 5) {
					runningTime = System.currentTimeMillis() - startTime;
					if (scriptingProgressLayout.isVisible()) {
						ManagerUI.log(nodeInfo.getTask() + " - updating done position");

						taskImages[lastIndex].setSource(new ThemeResource("img/scripting/done/" + primitives[lastIndex] + ".png"));
						taskImages[lastIndex + 1].setSource(new ThemeResource("img/scripting/done/done.png"));
						String time = String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(runningTime),
								TimeUnit.MILLISECONDS.toSeconds(runningTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(runningTime)));
						DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
						Date date = new Date();
						progressLabel.setValue("Done");
						resultLabel.setValue("Completed successfully<br><br>on " + dateFormat.format(date) + "<br><br>in " + time);

					} else {
						ManagerUI.log(nodeInfo.getTask() + " - cannot update display");
					}

					ManagerUI.log(nodeInfo.getTask() + " - Canceling Timer (done)");

					close();
					lastIndex = -1;
					lastProgressIndex = 0;

				} else if (status == 6) {
					if (scriptingProgressLayout.isVisible()) {
						ManagerUI.log(nodeInfo.getTask() + " - updating error position");

						taskImages[taskImages.length - 1].setSource(new ThemeResource("img/scripting/error.png"));
						progressLabel.setValue("Error!");
						String time = String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(runningTime),
								TimeUnit.MILLISECONDS.toSeconds(runningTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(runningTime)));
						DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
						Date date = new Date();
						resultLabel.setValue("Command failed<br><br>on " + dateFormat.format(date) + "<br><br>after " + time);
					} else {
						ManagerUI.log(nodeInfo.getTask() + " - cannot update display");
					}

					ManagerUI.log(nodeInfo.getTask() + " - Canceling Timer (canceled, error)");

					close();
					// lastIndex = 0; lastProgressIndex = 0;

				}

				// update enable/disabled state of control buttons
				/***
				 * if (scriptingControlsLayout.getWindow() != null) { String
				 * controls[] = taskRecord.getControls(); for (String key :
				 * ctrlButtons.keySet()) { NativeButton button =
				 * ctrlButtons.get(key);
				 * button.setEnabled(Arrays.asList(controls).contains(key) ?
				 * true : false); } }
				 ***/

			} finally {
				vaadinSession.unlock();
			}
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
