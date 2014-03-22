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

import java.util.ArrayList;
import java.util.Arrays;

import com.skysql.manager.ClusterComponent;
import com.skysql.manager.DateConversion;
import com.skysql.manager.ManagerUI;
import com.skysql.manager.TaskRecord;
import com.skysql.manager.api.CommandStates;
import com.skysql.manager.api.NodeInfo;
import com.skysql.manager.api.TaskInfo;
import com.skysql.manager.api.UserInfo;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * The Class PanelControl.
 */
public class PanelControl extends VerticalLayout {

	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private NodeInfo nodeInfo;
	private VerticalLayout commandsLayout, runningContainerLayout, placeholderLayout;
	private HorizontalLayout newLayout, logsLayout;
	private ListSelect commandSelect;
	private String[] oldcommands;
	private Table logsTable;
	private String lastNodeID;
	private int oldTasksCount;
	private UpdaterThread updaterThread;
	private Label placeholderLabel;
	private Object firstObject;

	/** The command listener. */
	private ValueChangeListener commandListener = new ValueChangeListener() {
		private static final long serialVersionUID = 0x4C656F6E6172646FL;

		public void valueChange(ValueChangeEvent event) {

			ManagerUI.log("commandListener()");

			String command = (String) event.getProperty().getValue();
			if (command != null) {
				selectCommand(command);
			}

		}
	};

	/** The refresh listener. */
	private ValueChangeListener refreshListener = new ValueChangeListener() {
		private static final long serialVersionUID = 0x4C656F6E6172646FL;

		public void valueChange(ValueChangeEvent event) {

			ManagerUI.log("refreshListener()");

			lastNodeID = null;
			OverviewPanel overviewPanel = getSession().getAttribute(OverviewPanel.class);
			overviewPanel.refresh();

		}
	};

	/**
	 * Instantiates a new panel control.
	 */
	PanelControl() {

		setSizeFull();
		addStyleName("controlTab");

		createNewLayout();
		createLogsLayout();

	}

	/**
	 * Creates the new layout.
	 */
	private void createNewLayout() {

		newLayout = new HorizontalLayout();
		newLayout.addStyleName("newLayout");
		newLayout.setMargin(true);
		newLayout.setSpacing(true);
		addComponent(newLayout);

		// COMMANDS
		commandsLayout = new VerticalLayout();
		commandsLayout.addStyleName("instructions");
		commandsLayout.setSizeUndefined();
		newLayout.addComponent(commandsLayout);

		commandSelect = new ListSelect("Commands");
		commandSelect.setImmediate(true);
		commandSelect.setRows(10);
		commandSelect.setNullSelectionAllowed(false);
		commandSelect.setWidth("12em");
		commandSelect.addValueChangeListener(commandListener);

		commandsLayout.addComponent(commandSelect);
		commandsLayout.setComponentAlignment(commandSelect, Alignment.MIDDLE_CENTER);

		// Scripting layout placeholder
		placeholderLayout = new VerticalLayout();
		placeholderLayout.addStyleName("placeholderLayout");

		placeholderLabel = new Label("No Command is currently running on this node");
		placeholderLabel.addStyleName("instructions");
		placeholderLabel.setSizeUndefined();
		placeholderLayout.addComponent(placeholderLabel);
		placeholderLayout.setComponentAlignment(placeholderLabel, Alignment.MIDDLE_CENTER);

		newLayout.addComponent(placeholderLayout);
		newLayout.setComponentAlignment(placeholderLayout, Alignment.MIDDLE_CENTER);
		runningContainerLayout = placeholderLayout;

	}

	/**
	 * Creates the logs layout.
	 */
	private void createLogsLayout() {
		Label separator = new Label();
		separator.addStyleName("separator");
		//separator.setHeight(Sizeable.SIZE_UNDEFINED, Unit.PIXELS);
		addComponent(separator);

		logsLayout = new HorizontalLayout();
		logsLayout.addStyleName("logsLayout");
		logsLayout.setSpacing(true);
		logsLayout.setMargin(true);
		addComponent(logsLayout);
		setExpandRatio(logsLayout, 1.0f);

		logsTable = new Table("Previously run Commands");
		logsTable.setPageLength(10);
		logsTable.addContainerProperty("Command", String.class, null);
		logsTable.addContainerProperty("State", String.class, null);
		logsTable.addContainerProperty("Info", Embedded.class, null);
		logsTable.addContainerProperty("Started", String.class, null);
		logsTable.addContainerProperty("Completed", String.class, null);
		logsTable.addContainerProperty("Steps", String.class, null);
		logsTable.addContainerProperty("Parameters", String.class, null);
		logsTable.addContainerProperty("User", String.class, null);

		logsLayout.addComponent(logsTable);
		logsLayout.setComponentAlignment(logsTable, Alignment.MIDDLE_CENTER);

	}

	/**
	 * Refresh.
	 */
	public void refresh() {

		ManagerUI.log("PanelControl refresh()");
		updaterThread = new UpdaterThread(updaterThread);
		updaterThread.start();

	}

	/**
	 * The Class UpdaterThread.
	 */
	class UpdaterThread extends Thread {

		/** The old updater thread. */
		UpdaterThread oldUpdaterThread;

		/** The flagged. */
		volatile boolean flagged = false;

		/** The adjust. */
		volatile boolean adjust;

		/** The format. */
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
				ManagerUI.log("PanelControl - Old thread is alive: " + oldUpdaterThread);
				oldUpdaterThread.flagged = true;
				oldUpdaterThread.interrupt();
				try {
					ManagerUI.log("PanelControl - Before Join");
					oldUpdaterThread.join();
					ManagerUI.log("PanelControl - After Join");
				} catch (InterruptedException iex) {
					ManagerUI.log("PanelControl - Interrupted Exception");
					return;
				}

			}

			ManagerUI.log("PanelControl - UpdaterThread.this: " + this);
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

		nodeInfo = (NodeInfo) getSession().getAttribute(ClusterComponent.class);
		final String newNodeID = nodeInfo.getID();

		final UserInfo userInfo = (UserInfo) getSession().getAttribute(UserInfo.class);

		TaskRecord taskRecord = nodeInfo.getTask();
		final String taskID = (taskRecord != null && taskRecord.getState().equals("running")) ? taskRecord.getID() : null;

		// update command history section
		TaskInfo taskInfo = new TaskInfo(null, nodeInfo.getParentID(), nodeInfo.getID());
		final ArrayList<TaskRecord> tasksList = taskInfo.getTasksList();

		managerUI.access(new Runnable() {
			@Override
			public void run() {
				// Here the UI is locked and can be updated

				ManagerUI.log("PanelControl access run() - taskID: " + taskID);

				RunningTask runningTask = nodeInfo.getCommandTask();

				DateConversion dateConversion = getSession().getAttribute(DateConversion.class);
				boolean adjust = dateConversion.isAdjustedToLocal();
				String format = dateConversion.getFormat();

				if (!newNodeID.equals(lastNodeID) || (tasksList != null && tasksList.size() != oldTasksCount) || adjust != updaterThread.adjust
						|| !format.equals(updaterThread.format)) {

					updaterThread.adjust = adjust;
					updaterThread.format = format;

					logsTable.removeAllItems();

					if (tasksList != null) {

						oldTasksCount = tasksList.size();
						firstObject = null;
						for (TaskRecord taskRecord : tasksList) {
							Embedded info = null;
							if (taskRecord.getState().equals(CommandStates.States.error.name())) {
								info = new Embedded(null, new ThemeResource("img/alert.png"));
								info.addStyleName("infoButton");
								info.setDescription(taskRecord.getError());
							}
							Object itemID = logsTable.addItem(new Object[] { taskRecord.getCommand(),
									CommandStates.getDescriptions().get(taskRecord.getState()), info, dateConversion.adjust(taskRecord.getStart()),
									dateConversion.adjust(taskRecord.getEnd()), taskRecord.getSteps(), taskRecord.getParams(), taskRecord.getUserID() },
									taskRecord.getID());
							if (firstObject == null) {
								firstObject = itemID;
							}

						}
					}
				} else if (tasksList.size() > 0 && firstObject != null) {
					// update top of the list with last task info
					TaskRecord taskRecord = tasksList.get(0);
					Embedded info = null;
					if (taskRecord.getState().equals(CommandStates.States.error.name())) {
						info = new Embedded(null, new ThemeResource("img/alert.png"));
						info.addStyleName("infoButton");
						info.setDescription(taskRecord.getError());
					}
					Item tableRow = logsTable.getItem(firstObject);
					tableRow.getItemProperty("State").setValue(CommandStates.getDescriptions().get(taskRecord.getState()));
					tableRow.getItemProperty("Info").setValue(info);
					tableRow.getItemProperty("Completed").setValue(dateConversion.adjust(taskRecord.getEnd()));
				}

				// task is running although it was not started by us
				if (taskID != null && runningTask == null) {
					runningTask = new RunningTask(null, nodeInfo, commandSelect);
					runningTask.addRefreshListener(refreshListener);
				}

				if (nodeInfo.getCommands() == null || nodeInfo.getCommands().getNames().isEmpty()) {
					commandSelect.removeAllItems();
					oldcommands = null;
					placeholderLabel.setValue("No Command is currently available on this node");
				} else {
					placeholderLabel.setValue("No Command is currently running on this node");
					String commands[] = new String[nodeInfo.getCommands().getNames().keySet().size()];
					nodeInfo.getCommands().getNames().keySet().toArray(commands);
					if (!newNodeID.equals(lastNodeID) || !Arrays.equals(commands, oldcommands)) {
						oldcommands = commands;
						commandSelect.removeValueChangeListener(commandListener);

						// rebuild list of commands with what node is accepting
						commandSelect.removeAllItems();
						if ((commands != null) && (commands.length != 0)) {
							for (String command : commands) {
								commandSelect.addItem(command);
							}
						}

						commandSelect.addValueChangeListener(commandListener);
					}

					commandSelect.removeValueChangeListener(commandListener);
					String selected = (runningTask != null) ? runningTask.getCommand() : null;
					commandSelect.select(selected);
					commandSelect.addValueChangeListener(commandListener);

				}
				commandSelect.setEnabled(taskID != null ? false : true);

				if (runningTask != null) {
					VerticalLayout newScriptingLayout = runningTask.getLayout();
					newLayout.replaceComponent(runningContainerLayout, newScriptingLayout);
					runningContainerLayout = newScriptingLayout;
				} else if (runningContainerLayout != placeholderLayout) {
					newLayout.replaceComponent(runningContainerLayout, placeholderLayout);
					newLayout.setComponentAlignment(placeholderLayout, Alignment.MIDDLE_CENTER);
					runningContainerLayout = placeholderLayout;
				}

				lastNodeID = newNodeID;
			}
		});

	}

	/**
	 * Select command.
	 *
	 * @param command the command
	 */
	public void selectCommand(String command) {
		RunningTask runningTask = nodeInfo.getCommandTask();

		ManagerUI.log("selectCommand() - runningTask: " + runningTask);

		runningTask = new RunningTask(command, nodeInfo, commandSelect);
		runningTask.addRefreshListener(refreshListener);

		// add SCRIPTING layout
		VerticalLayout newScriptingLayout = runningTask.getLayout();
		newLayout.replaceComponent(runningContainerLayout, newScriptingLayout);
		newLayout.setComponentAlignment(newScriptingLayout, Alignment.MIDDLE_LEFT);
		runningContainerLayout = newScriptingLayout;

	}

}
