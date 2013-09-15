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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import com.skysql.manager.ClusterComponent;
import com.skysql.manager.DateConversion;
import com.skysql.manager.ManagerUI;
import com.skysql.manager.TaskRecord;
import com.skysql.manager.api.CommandStates;
import com.skysql.manager.api.NodeInfo;
import com.skysql.manager.api.TaskInfo;
import com.skysql.manager.api.UserInfo;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

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

	private ValueChangeListener refreshListener = new ValueChangeListener() {
		private static final long serialVersionUID = 0x4C656F6E6172646FL;

		public void valueChange(ValueChangeEvent event) {

			ManagerUI.log("refreshListener()");

			lastNodeID = null;
			OverviewPanel overviewPanel = getSession().getAttribute(OverviewPanel.class);
			overviewPanel.refresh();

		}
	};

	PanelControl() {

		setSizeFull();
		addStyleName("controlTab");

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

		// COMMANDS
		commandsLayout = new VerticalLayout();
		commandsLayout.addStyleName("instructions");
		commandsLayout.setSizeUndefined();
		newLayout.addComponent(commandsLayout);

		commandSelect = new ListSelect("Commands");
		commandSelect.setImmediate(true);
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
		logsTable.addContainerProperty("Started", String.class, null);
		logsTable.addContainerProperty("Completed", String.class, null);
		logsTable.addContainerProperty("Command", String.class, null);
		logsTable.addContainerProperty("Parameters", String.class, null);
		logsTable.addContainerProperty("Steps", String.class, null);
		logsTable.addContainerProperty("PID", String.class, null);
		logsTable.addContainerProperty("Private IP", String.class, null);
		logsTable.addContainerProperty("User", String.class, null);
		logsTable.addContainerProperty("State", String.class, null);

		logsLayout.addComponent(logsTable);
		logsLayout.setComponentAlignment(logsTable, Alignment.MIDDLE_CENTER);

	}

	public void refresh() {

		ManagerUI.log("PanelControl refresh()");
		updaterThread = new UpdaterThread(updaterThread);
		updaterThread.start();

	}

	class UpdaterThread extends Thread {
		UpdaterThread oldUpdaterThread;
		volatile boolean flagged = false;

		UpdaterThread(UpdaterThread oldUpdaterThread) {
			this.oldUpdaterThread = oldUpdaterThread;
		}

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

	private void asynchRefresh(final UpdaterThread updaterThread) {

		ManagerUI managerUI = getSession().getAttribute(ManagerUI.class);

		nodeInfo = (NodeInfo) getSession().getAttribute(ClusterComponent.class);
		final String newNodeID = nodeInfo.getID();

		final UserInfo userInfo = (UserInfo) getSession().getAttribute(UserInfo.class);

		final String taskID = nodeInfo.getTask();
		// String taskCommand = nodeInfo.getCommand();
		final RunningTask runningTask = nodeInfo.getCommandTask();

		// update command history section
		TaskInfo taskInfo = new TaskInfo(null, nodeInfo.getParentID(), nodeInfo.getID());
		final ArrayList<TaskRecord> tasksList = taskInfo.getTasksList();

		managerUI.access(new Runnable() {
			@Override
			public void run() {
				// Here the UI is locked and can be updated

				ManagerUI.log("PanelControl access run() - taskID: " + taskID);

				if (!newNodeID.equals(lastNodeID) || (tasksList != null && tasksList.size() != oldTasksCount)) {

					logsTable.removeAllItems();

					if (tasksList != null) {
						oldTasksCount = tasksList.size();
						Collections.reverse(tasksList);
						for (TaskRecord taskRecord : tasksList) {
							logsTable.addItem(new Object[] { DateConversion.adjust(taskRecord.getStart()), DateConversion.adjust(taskRecord.getEnd()),
									taskRecord.getCommand(), taskRecord.getParams(), taskRecord.getSteps(), taskRecord.getPID(), taskRecord.getPrivateIP(),
									userInfo.findNameByID(taskRecord.getUserID()), CommandStates.getDescriptions().get(taskRecord.getState()) },
									taskRecord.getID());
						}
					}
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
