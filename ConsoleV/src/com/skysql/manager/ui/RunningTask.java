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

package com.skysql.manager.ui;

import java.util.concurrent.ScheduledFuture;

import com.skysql.manager.Commands;
import com.skysql.manager.Commands.Command;
import com.skysql.manager.ExecutorFactory;
import com.skysql.manager.ManagerUI;
import com.skysql.manager.MonitorLatest;
import com.skysql.manager.TaskRecord;
import com.skysql.manager.api.Monitors.MonitorNames;
import com.skysql.manager.api.NodeInfo;
import com.skysql.manager.api.TaskRun;
import com.skysql.manager.api.UserObject;
import com.skysql.manager.ui.components.ComponentButton;
import com.skysql.manager.ui.components.ParametersLayout;
import com.skysql.manager.ui.components.ScriptingControlsLayout;
import com.skysql.manager.ui.components.ScriptingControlsLayout.Controls;
import com.skysql.manager.ui.components.ScriptingProgressLayout;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.VerticalLayout;

/**
 * The Class RunningTask.
 */
public final class RunningTask {

	private static final int SHORT_REFRESH_DELAY = 3;

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
	private ListSelect commandSelect;
	private ParametersLayout parametersLayout;
	private ValueChangeListener listener;
	private OverviewPanel overviewPanel = VaadinSession.getCurrent().getAttribute(OverviewPanel.class);
	private TaskRun taskRun;

	/**
	 * Instantiates a new running task.
	 *
	 * @param command the command
	 * @param nodeInfo the node info
	 * @param commandSelect the command select
	 */
	RunningTask(String command, NodeInfo nodeInfo, ListSelect commandSelect) {
		this.command = command;
		this.nodeInfo = nodeInfo;
		this.commandSelect = commandSelect;

		ManagerUI.log("RunningTask - command: " + command + ", node: " + nodeInfo.getName());

		if (command == null) {
			observerMode = true;
			taskRecord = nodeInfo.getTask();
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
		containerLayout.setComponentAlignment(scriptingLayout, Alignment.MIDDLE_LEFT);

		// COLUMN 1. PARAMETERS
		Commands.Command commandEnum = Commands.Command.valueOf(command);
		switch (commandEnum) {
		case backup:
		case connect:
		case restore:
			if (observerMode) {
				//				parametersLayout = new HorizontalLayout();
				//				parametersLayout.addComponent(new Label("Parameters: " + taskRecord.getParams()));
				//				scriptingLayout.addComponent(parametersLayout);
				//				scriptingLayout.setComponentAlignment(parametersLayout, Alignment.MIDDLE_LEFT);
			} else {
				parametersLayout = new ParametersLayout(this, nodeInfo, commandEnum);
				scriptingLayout.addComponent(parametersLayout);
				scriptingLayout.setComponentAlignment(parametersLayout, Alignment.TOP_LEFT);
			}
			break;
		default:
			break;
		}

		// COLUMN 2. CONTROLS
		// controls = taskRun.getControls(); this is for when they are server-side driven
		scriptingControlsLayout = new ScriptingControlsLayout(this, new Controls[] { Controls.Run, Controls.Stop });
		scriptingLayout.addComponent(scriptingControlsLayout);
		scriptingLayout.setComponentAlignment(scriptingControlsLayout, Alignment.MIDDLE_LEFT);

		// TODO: this needs to be done properly
		if (observerMode) {
			scriptingControlsLayout.enableControls(true, Controls.Stop);
		} else {
			if (parametersLayout == null || parametersLayout.isParameterReady()) {
				scriptingControlsLayout.enableControls(true, Controls.Run);
			}
		}

		// COLUMN 3. PROGRESS & RESULT
		scriptingProgressLayout = new ScriptingProgressLayout(this, observerMode);
		scriptingLayout.addComponent(scriptingProgressLayout);
		scriptingLayout.setComponentAlignment(scriptingProgressLayout, Alignment.MIDDLE_LEFT);

		if (observerMode) {
			scriptingProgressLayout.buildProgress(taskRecord, command, taskRecord.getSteps());
			activateTimer();
		} else {
			scriptingProgressLayout.buildProgress(taskRecord, command, nodeInfo.getCommands().getSteps(command));
		}

	}

	/**
	 * Gets the layout.
	 *
	 * @return the layout
	 */
	public VerticalLayout getLayout() {
		return containerLayout;
	}

	/**
	 * Gets the controls layout.
	 *
	 * @return the controls layout
	 */
	public ScriptingControlsLayout getControlsLayout() {
		return scriptingControlsLayout;
	}

	/**
	 * Gets the scripting layout.
	 *
	 * @return the scripting layout
	 */
	public HorizontalLayout getScriptingLayout() {
		return scriptingLayout;
	}

	/**
	 * Gets the command.
	 *
	 * @return the command
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * Select parameter.
	 *
	 * @param parameter the parameter
	 */
	public void selectParameter(String parameter) {
		params = parameter;

		if (!paramsReady) {
			paramsReady = true;
		}
	}

	/**
	 * Control clicked.
	 *
	 * @param control the control
	 */
	public void controlClicked(Controls control) {
		switch (control) {
		case Run:
			start();
			break;

		case Schedule:
			schedule();
			break;

		case Pause:
			pause();
			break;

		case Stop:
			stop();
			break;

		}
	}

	/**
	 * Start.
	 */
	void start() {
		// disable further command selection immediately
		commandSelect.setEnabled(false);
		if (parametersLayout != null) {
			parametersLayout.setEnabled(false);
		}
		scriptingProgressLayout.start();

		UserObject userObject = VaadinSession.getCurrent().getAttribute(UserObject.class);
		String userID = userObject.getUserID();
		// we used to look up user-settable setting of loose/strict; after discussion with Mark on Nov 15, 
		// we make is strict only for backup, which currently is the only command where different steps sequences apply depending on node state
		String state = (command.equals(Command.backup) ? nodeInfo.getState() : null);
		taskRun = new TaskRun(nodeInfo.getParentID(), nodeInfo.getID(), userID, command, params, state);
		if (taskRun.getTaskRecord() == null) {
			command = null;
			commandSelect.select(null);
			commandSelect.setEnabled(true);
			if (parametersLayout != null) {
				parametersLayout.setEnabled(true);
			}
			scriptingProgressLayout.setResult("Failed to launch");
			scriptingProgressLayout.setErrorInfo(taskRun.getError());
			overviewPanel.refresh();
			return;
		}

		String taskSteps = taskRun.getTaskRecord().getSteps();
		String nodeSteps = nodeInfo.getCommands().getSteps(command);
		if (!taskSteps.equals(nodeSteps)) {
			scriptingProgressLayout.buildProgress(taskRecord, command, taskSteps);
			scriptingProgressLayout.setTitle(command + " (Updated Steps)");
		}

		nodeInfo.setTask(taskRun.getTaskRecord());
		taskRecord = taskRun.getTaskRecord();

		scriptingControlsLayout.enableControls(true, Controls.Stop);

		activateTimer();

	}

	/**
	 * Schedule.
	 */
	void schedule() {
	}

	/**
	 * Stop.
	 */
	void stop() {
		TaskRun.delete(taskRecord.getID());
		Runnable runTimerTask = new RunTimerTask();
		runTimerTask.run();
		close();
	}

	/**
	 * Pause.
	 */
	void pause() {

	}

	/**
	 * Close.
	 */
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

		scriptingControlsLayout.enableControls(false, Controls.Stop);

		if (listener != null) {
			listener.valueChange(null);
		}

	}

	/**
	 * Adds the refresh listener.
	 *
	 * @param listener the listener
	 */
	public void addRefreshListener(ValueChangeListener listener) {
		this.listener = listener;
	}

	/**
	 * Activate timer.
	 */
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

	/**
	 * The Class RunTimerTask.
	 */
	private final class RunTimerTask implements Runnable {

		/** The count. */
		private int fCount;

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			++fCount;
			ManagerUI.log("RunningTask - task:" + nodeInfo.getTask() + " - " + fCount);

			nodeInfo.updateTask();
			TaskRecord taskRecord = nodeInfo.getTask();
			if (taskRecord != null) {
				scriptingProgressLayout.refresh(taskRecord);
			} else {
				// we got no TaskRecord; shut down the timer and cleanup.
				close();
			}

			// ComponentButton button = nodeInfo.getButton();
			ComponentButton button = overviewPanel.getNodeButton(nodeInfo.getID());
			MonitorLatest monitorLatest = nodeInfo.getMonitorLatest();
			String newCapacity = monitorLatest.getData().get(MonitorNames.capacity.name());
			overviewPanel.updateButton(button, nodeInfo.getState(), taskRecord, newCapacity);

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
