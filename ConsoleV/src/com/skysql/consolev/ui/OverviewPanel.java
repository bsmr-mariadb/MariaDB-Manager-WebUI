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

import java.util.ArrayList;

import com.skysql.consolev.MonitorRecord;
import com.skysql.consolev.api.ClusterComponent;
import com.skysql.consolev.api.MonitorDataLatest;
import com.skysql.consolev.api.Monitors;
import com.skysql.consolev.api.NodeInfo;
import com.skysql.consolev.api.NodeStates;
import com.skysql.consolev.api.SystemInfo;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public class OverviewPanel extends Panel {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private static final String ICON_MASTER = "master";
	private static final String ICON_SLAVE = "slave";

	private SystemInfo systemInfo;
	private ArrayList<NodeInfo> nodes = new ArrayList<NodeInfo>();
	private ArrayList<VerticalLayout> buttons = new ArrayList<VerticalLayout>();
	private VerticalLayout selectedButton;

	public OverviewPanel() {

		setHeight("176px");

		HorizontalLayout strip = new HorizontalLayout();
		strip.addStyleName("overviewPanel");
		strip.setMargin(true);
		strip.setWidth("100%");
		setContent(strip);

		systemInfo = VaadinSession.getCurrent().getAttribute(SystemInfo.class);

		// initialize System button
		selectedButton = createButton(strip, systemInfo);

		// initialize Node buttons
		for (String nodeID : systemInfo.getNodes()) {
			NodeInfo nodeInfo = new NodeInfo(systemInfo.getID(), nodeID);
			nodes.add(nodeInfo);
			createButton(strip, nodeInfo);
		}

	}

	public ArrayList<NodeInfo> getNodes() {
		return nodes;
	}

	private VerticalLayout createButton(HorizontalLayout strip, ClusterComponent componentInfo) {
		final VerticalLayout button_node = new VerticalLayout();
		button_node.setWidth(componentInfo.getType() == ClusterComponent.CCType.system ? "128px" : "96px");
		button_node.setHeight("120px");
		Label nodeName = new Label(componentInfo.getName());
		nodeName.setSizeUndefined();
		button_node.addComponent(nodeName);
		button_node.setComponentAlignment(nodeName, Alignment.BOTTOM_CENTER);

		componentInfo.setButton(button_node);

		button_node.addStyleName(componentInfo.getType().toString());
		String status = componentInfo.getStatus();
		String icon = NodeStates.getNodeIcon(status);
		button_node.addStyleName(icon);
		button_node.setImmediate(true);

		String description;
		switch (componentInfo.getType()) {
		case system:
			description = ((SystemInfo) componentInfo).ToolTip();
			break;
		case node:
			description = ((NodeInfo) componentInfo).ToolTip();
			break;
		default:
			description = "Unknown component type";
			break;
		}
		button_node.setDescription(description);
		button_node.setData(componentInfo);
		strip.addComponent(button_node);
		buttons.add(button_node);

		button_node.addLayoutClickListener(new LayoutClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			@Override
			public void layoutClick(LayoutClickEvent event) {
				clickLayout(button_node);
			}
		});

		return button_node;
	}

	public void clickLayout(final int buttonIndex) {
		clickLayout(buttons.get(buttonIndex));
	}

	private void clickLayout(final VerticalLayout button_node) {
		if (selectedButton != null) {
			String styleName = selectedButton.getStyleName();
			if (styleName.contains("selected")) {
				selectedButton.setStyleName(styleName.replace("selected", ""));
			}
		}

		selectedButton = button_node;
		button_node.addStyleName("selected");
		ClusterComponent componentInfo = (ClusterComponent) button_node.getData();
		VaadinSession.getCurrent().setAttribute(ClusterComponent.class, componentInfo);

		TabbedPanel tabbedPanel = VaadinSession.getCurrent().getAttribute(TabbedPanel.class);
		tabbedPanel.refresh();

	}

	public void refresh() {

		boolean refresh = false;
		SystemInfo newSystemInfo = new SystemInfo(systemInfo.getID());
		VerticalLayout button = systemInfo.getButton();

		String newName;
		if ((newName = newSystemInfo.getName()) != null && !newName.equals(systemInfo.getID())) {
			refresh = true;
			Label buttonLabel = (Label) button.getComponent(0);
			buttonLabel.setValue(newName);
		}

		if (refresh) {
			newSystemInfo.setButton(systemInfo.getButton());
			systemInfo = newSystemInfo;
			VaadinSession.getCurrent().setAttribute(SystemInfo.class, systemInfo);
			button.setData(systemInfo);
			button.setDescription(systemInfo.ToolTip());
		}

		refresh = false;
		for (NodeInfo nodeInfo : nodes) {
			NodeInfo newInfo = new NodeInfo(nodeInfo.getSystemID(), nodeInfo.getID());

			// copies the Button object to new nodeInfo and replaces old one
			// with it in the button Data and in the array
			button = nodeInfo.getButton();
			newInfo.setButton(button);

			// fetch current capacity from monitor
			MonitorRecord capacityMonitor = Monitors.getMonitor(Monitors.MONITOR_CAPACITY);
			if (capacityMonitor != null) {
				MonitorDataLatest monitorData = new MonitorDataLatest(capacityMonitor, newInfo.getSystemID(), newInfo.getID());
				Number dataPoint = monitorData.getLatestValue();
				newInfo.setCapacity((dataPoint != null) ? String.valueOf(dataPoint) : null);
			}

			if ((newName = newInfo.getName()) != null && !newName.equals(nodeInfo.getName())) {
				refresh = true;
				Label buttonLabel = (Label) button.getComponent(0);
				buttonLabel.setValue(newName);
			}

			String newStatus, newCapacity;
			if (((newStatus = newInfo.getStatus()) != null && !newStatus.equals(nodeInfo.getStatus()))
					|| ((newStatus != null) && (newCapacity = newInfo.getCapacity()) != null && !newCapacity.equals(nodeInfo.getCapacity()))) {
				refresh = true;

				String icon = NodeStates.getNodeIcon(newStatus);
				if ((newCapacity = newInfo.getCapacity()) != null && (icon.equals(ICON_MASTER) || icon.equals(ICON_SLAVE))) {

					double capacity_num = Double.parseDouble(newCapacity);
					if (capacity_num > 0 && capacity_num < 20)
						icon += "-20";
					else if (capacity_num < 40)
						icon += "-40";
					else if (capacity_num < 60)
						icon += "-60";
					else if (capacity_num < 80)
						icon += "-80";
					else if (capacity_num <= 100)
						icon += "-100";
				}

				button.setStyleName(icon);
				button.addStyleName(nodeInfo.getType().toString());
				if (button == selectedButton) {
					button.addStyleName("selected");
				}
			}

			String newTask = newInfo.getTask();
			String oldTask = nodeInfo.getTask();
			if (((newTask == null) && (oldTask != null)) || (newTask != null) && ((oldTask == null) || (!newTask.equals(oldTask)))) {
				refresh = true;
			}

			if (refresh) {
				newInfo.setButton(button);
				// carry over RunningTask(s)
				newInfo.setCommandTask(nodeInfo.getCommandTask());

				nodes.set(nodes.indexOf(nodeInfo), newInfo);
				if (nodeInfo == VaadinSession.getCurrent().getAttribute(ClusterComponent.class)) {
					VaadinSession.getCurrent().setAttribute(ClusterComponent.class, newInfo);
				}
				button.setData(newInfo);
				button.setDescription(newInfo.ToolTip());
			}

		} // for all nodes

	}
}
