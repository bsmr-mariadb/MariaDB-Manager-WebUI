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

package com.skysql.consolev.ui;

import java.util.Arrays;
import java.util.LinkedHashMap;

import com.skysql.consolev.TaskRecord;
import com.skysql.consolev.api.ClusterComponent;
import com.skysql.consolev.api.CommandStates;
import com.skysql.consolev.api.Commands;
import com.skysql.consolev.api.NodeInfo;
import com.skysql.consolev.api.TaskInfo;
import com.skysql.consolev.api.UserInfo;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.VaadinSession;
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
	private UserInfo userInfo;
	private Table logsTable;
	private String lastNodeID;
	final LinkedHashMap<String, String> names = Commands.getNames();

	private ValueChangeListener refreshListener = new ValueChangeListener() {
		private static final long serialVersionUID = 0x4C656F6E6172646FL;

		public void valueChange(ValueChangeEvent event) {

			lastNodeID = null;
			refresh();
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

		Label placeholderLabel = new Label("No Command is currently running on this node");
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
		logsTable.addContainerProperty("User", String.class, null);
		logsTable.addContainerProperty("Status", String.class, null);

		userInfo = new UserInfo(null);

		logsLayout.addComponent(logsTable);
		logsLayout.setComponentAlignment(logsTable, Alignment.MIDDLE_CENTER);

	}

	public void refresh() {

		nodeInfo = (NodeInfo) VaadinSession.getCurrent().getAttribute(ClusterComponent.class);
		String newNodeID = nodeInfo.getID();

		String taskID = nodeInfo.getTask();
		// String taskCommand = nodeInfo.getCommand();
		RunningTask runningTask = nodeInfo.getCommandTask();
		String commands[] = nodeInfo.getCommands();

		if (!newNodeID.equalsIgnoreCase(lastNodeID)) {
			lastNodeID = newNodeID;

			TaskInfo taskInfo = new TaskInfo(null, null, nodeInfo.getID());
			logsTable.removeAllItems();
			if (taskInfo.getTasksList() != null) {
				for (TaskRecord taskRecord : taskInfo.getTasksList()) {
					logsTable.addItem(new Object[] { taskRecord.getStart(), taskRecord.getEnd(), names.get(taskRecord.getCommand()), taskRecord.getParams(),
							userInfo.findNameByID(taskRecord.getUserID()), CommandStates.getDescriptions().get(taskRecord.getStatus()) }, taskRecord.getID());
				}
			}
		}

		if (taskID != null) {
			commandSelect.setEnabled(false);

			/**
			 * if (runningTask == null &&
			 * !taskCommand.equalsIgnoreCase(CMD_BACKUP) ||
			 * !taskCommand.equalsIgnoreCase(CMD_BACKUP2) ||
			 * !taskCommand.equalsIgnoreCase(CMD_RESTORE) ||
			 * !taskCommand.equalsIgnoreCase(CMD_RESTORE2)) { runningTask = new
			 * RunningTask(null, nodeInfo); runningTask.activateTimer(); }
			 **/

		} else {
			commandSelect.setEnabled(true);

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
		RunningTask runningTask = nodeInfo.getCommandTask();

		if (runningTask != null)
			runningTask.close();

		runningTask = new RunningTask(command, nodeInfo, commandSelect);
		runningTask.addRefreshListener(refreshListener);

		// add SCRIPTING layout
		VerticalLayout newScriptingLayout = runningTask.getLayout();
		newLayout.replaceComponent(runningContainerLayout, newScriptingLayout);
		newLayout.setComponentAlignment(newScriptingLayout, Alignment.MIDDLE_LEFT);
		runningContainerLayout = newScriptingLayout;

	}

}
