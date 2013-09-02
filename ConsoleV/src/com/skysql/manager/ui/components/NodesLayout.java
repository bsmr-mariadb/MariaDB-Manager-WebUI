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

import java.util.ArrayList;
import java.util.Arrays;

import com.skysql.manager.ClusterComponent;
import com.skysql.manager.ClusterComponent.CCType;
import com.skysql.manager.ManagerUI;
import com.skysql.manager.SystemRecord;
import com.skysql.manager.api.NodeInfo;
import com.skysql.manager.api.SystemInfo;
import com.skysql.manager.ui.ComponentDialog;
import com.skysql.manager.ui.OverviewPanel;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class NodesLayout extends HorizontalLayout {

	private boolean isEditable;
	private ArrayList<ComponentButton> buttons = new ArrayList<ComponentButton>();
	private String[] components;
	private String systemID;
	private VerticalLayout placeholderLayout;
	private String message;

	public NodesLayout(SystemRecord systemRecord) {

		addStyleName("network");

		addLayoutClickListener(new LayoutClickListener() {

			public void layoutClick(LayoutClickEvent event) {

				Component child;
				VaadinSession session = getSession();
				boolean isDoubleClick = event.isDoubleClick();
				ManagerUI.log("is DoubleClick: " + isDoubleClick);
				if (event.isDoubleClick() && (child = event.getChildComponent()) != null && (child instanceof ComponentButton)) {
					// Get the child component which was double-clicked
					ComponentButton button = (ComponentButton) child;
					ClusterComponent clusterComponent = (ClusterComponent) button.getData();
					if (isEditable) {
						new ComponentDialog(clusterComponent, button);
					} else {
						if (clusterComponent.getType() == ClusterComponent.CCType.system) {
							SystemInfo systemInfo = session.getAttribute(SystemInfo.class);
							String clusterID = clusterComponent.getID();
							systemInfo.setCurrentSystem(clusterID);
							session.setAttribute(SystemInfo.class, systemInfo);
							ManagerUI.log("new systemID: " + clusterID);
							clusterComponent.setButton(button);
							OverviewPanel overviewPanel = session.getAttribute(OverviewPanel.class);
							overviewPanel.refresh();
						}
					}

				} else if (!isEditable && (child = event.getChildComponent()) != null && (child instanceof ComponentButton)) {
					// Get the child component which was clicked
					ComponentButton button = (ComponentButton) child;
					OverviewPanel overviewPanel = session.getAttribute(OverviewPanel.class);
					overviewPanel.clickLayout(button);
				}

			}
		});

	}

	public ComponentButton getButton(int index) {
		if (!buttons.isEmpty() && index < buttons.size()) {
			return buttons.get(index);
		} else {
			return null;
		}
	}

	public ArrayList<NodeInfo> getNodes() {
		ArrayList<NodeInfo> nodes = new ArrayList<NodeInfo>(buttons.size());
		for (ComponentButton button : buttons) {
			ClusterComponent clusterComponent = (ClusterComponent) button.getData();
			if (clusterComponent.getType() == ClusterComponent.CCType.node) {
				nodes.add((NodeInfo) clusterComponent);
			}
		}
		return nodes;
	}

	public ComponentButton findButton(ClusterComponent component) {
		for (ComponentButton button : buttons) {
			ClusterComponent oldComponent = (ClusterComponent) button.getData();
			if (oldComponent.getID().equals(component.getID()) && (oldComponent.getType().equals(component.getType()))) {
				button.setData(component);
				return button;
			}
		}
		return null;
	}

	public void deleteComponent(ComponentButton button) {
		buttons.remove(button);
		removeComponent(button);
		OverviewPanel overviewPanel = getSession().getAttribute(OverviewPanel.class);
		if (button.isSelected()) {
			overviewPanel.clickComponentButton(0);
		}
		overviewPanel.refresh();
	}

	public void setEditable(boolean editable) {
		isEditable = editable;
		for (ComponentButton button : buttons) {
			button.setEditable(editable);
		}
	}

	public void placeholderLayout(String message) {

		if (placeholderLayout == null) {
			removeAllComponents();
			components = null;
		} else {
			removeComponent(placeholderLayout);
		}

		if (message != null) {
			this.message = message;
		} else {
			message = this.message;
		}

		placeholderLayout = new VerticalLayout();
		placeholderLayout.addStyleName("placeholderLayout");
		placeholderLayout.setHeight(ComponentButton.COMPONENT_HEIGHT);

		Label placeholderLabel = new Label("No " + message + " available");
		placeholderLabel.addStyleName("instructions");
		placeholderLabel.setSizeUndefined();
		placeholderLayout.addComponent(placeholderLabel);
		placeholderLayout.setComponentAlignment(placeholderLabel, Alignment.MIDDLE_CENTER);

		Label placeholderLabel2 = new Label(isEditable ? "Press \"Add...\" to add new " + message + ", then \"Done\" when finished"
				: "Press \"Edit\" to begin adding " + message);
		placeholderLabel2.addStyleName("instructions");
		placeholderLabel2.setSizeUndefined();
		placeholderLayout.addComponent(placeholderLabel2);
		placeholderLayout.setComponentAlignment(placeholderLabel2, Alignment.MIDDLE_CENTER);

		addComponent(placeholderLayout);
		setComponentAlignment(placeholderLayout, Alignment.MIDDLE_CENTER);

		setStyleName(getStyleName().replace("network", ""));

	}

	public synchronized void refresh(final OverviewPanel.UpdaterThread updaterThread, final SystemRecord parentSystemRecord) {

		VaadinSession session = getSession();
		ManagerUI managerUI = session.getAttribute(ManagerUI.class);
		SystemInfo systemInfo = session.getAttribute(SystemInfo.class);
		OverviewPanel overviewPanel = session.getAttribute(OverviewPanel.class);

		if (parentSystemRecord == null) {
			systemID = null;
			placeholderLayout("Systems");
			return;
		} else if (parentSystemRecord.getNodes().length == 0) {
			placeholderLayout("Components");
			return;
		} else {
			if (placeholderLayout != null) {
				removeComponent(placeholderLayout);
				placeholderLayout = null;

				addStyleName("network");
			}
		}

		String[] newComponents = parentSystemRecord.getNodes();
		if (!parentSystemRecord.getID().equals(systemID) || (newComponents != null && !Arrays.equals(newComponents, components))) {
			systemID = parentSystemRecord.getID();
			components = newComponents;

			ManagerUI.log("Reload Components");

			String currentSelectedID = null;
			CCType currentSelectedType = null;
			ClusterComponent currentClusterComponent = session.getAttribute(ClusterComponent.class);
			if (currentClusterComponent != null) {
				currentSelectedID = currentClusterComponent.getID();
				currentSelectedType = currentClusterComponent.getType();
			}

			session.lock();
			try {

				// Here the UI is locked and can be updated
				ManagerUI.log("NodesLayout access run() removeall");
				removeAllComponents();

				ArrayList<ComponentButton> newButtons = new ArrayList<ComponentButton>();
				for (String componentID : parentSystemRecord.getNodes()) {

					final ClusterComponent clusterComponent;
					if (parentSystemRecord.getParentID() == null) {
						// this is the ROOT record, where "nodes" is really the flat list of systems
						clusterComponent = systemInfo.getSystemRecord(componentID);
					} else {
						// this is a normal System record
						clusterComponent = new NodeInfo(parentSystemRecord.getID(), parentSystemRecord.getSystemType());
						clusterComponent.setID(componentID);
					}

					if (updaterThread.flagged) {
						ManagerUI.log("NodesLayout - flagged is set while adding nodes");
						return;
					}

					ComponentButton button = clusterComponent.getButton();
					if (button == null) {
						ManagerUI.log("NodesLayout access run() button not in component");
						button = findButton(clusterComponent);
					}
					if (button == null) {
						ManagerUI.log("NodesLayout access run() button not found");
						button = new ComponentButton(clusterComponent);
					}
					addComponent(button);
					setComponentAlignment(button, Alignment.MIDDLE_CENTER);
					button.setEditable(isEditable);
					if (clusterComponent.getID().equals(currentSelectedID) && clusterComponent.getType().equals(currentSelectedType) && !button.isSelected()) {
						overviewPanel.clickLayout(button);
					}
					newButtons.add(button);
				}
				buttons = newButtons;
			} finally {
				session.unlock();
			}

		}

		if (updaterThread.flagged) {
			ManagerUI.log("NodesLayout - flagged is set after removeall");
			return;
		}

		ManagerUI.log("NodesLayout - before for loop - buttons: " + buttons.size());

		for (final ComponentButton button : buttons) {
			final ClusterComponent currentComponent = (ClusterComponent) button.getData();

			final ClusterComponent newComponent;

			switch (currentComponent.getType()) {
			case system:
				newComponent = systemInfo.updateSystem(currentComponent.getID());
				break;

			case node:
				NodeInfo nodeInfo = (NodeInfo) currentComponent;
				newComponent = new NodeInfo(nodeInfo.getParentID(), nodeInfo.getSystemType(), nodeInfo.getID());
				break;

			default:
				continue;
			}

			// fetch current capacity from monitor
			/***  disabled until we figure out availability of MONITOR_CAPACITY
			MonitorRecord capacityMonitor = Monitors.getMonitor(Monitors.MONITOR_CAPACITY);
			if (capacityMonitor != null) {
				MonitorDataLatest monitorData = new MonitorDataLatest(capacityMonitor, newInfo.getSystemID(), newInfo.getID());
				Number dataPoint = monitorData.getLatestValue();
				newInfo.setCapacity((dataPoint != null) ? String.valueOf(dataPoint) : null);
			}
			***/

			if (updaterThread.flagged) {
				ManagerUI.log("NodesLayout - flagged is set before Node: " + button.getName());
				return;
			}

			managerUI.access(new Runnable() {
				@Override
				public void run() {
					// Here the UI is locked and can be updated

					ManagerUI.log("NodesLayout access run() button " + newComponent.getName());

					String newName = newComponent.getName();
					if (newName != null && !newName.equals(currentComponent.getName())) {
						button.setName(newName);
					}

					String newState = newComponent.getState();

					String newCapacity = newComponent.getCapacity();
					if ((newState != null && (!newState.equals(currentComponent.getState())) || (newCapacity != null && !newCapacity.equals(currentComponent
							.getCapacity())))) {

						button.setIcon(currentComponent.getType().toString(), newState, newCapacity);
					}

					String toolTip = null;
					switch (newComponent.getType()) {
					case system:
						toolTip = ((SystemRecord) newComponent).ToolTip();
						break;
					case node:
						NodeInfo nodeInfo = (NodeInfo) newComponent;
						toolTip = nodeInfo.ToolTip();
						// carry over RunningTask(s)
						nodeInfo.setCommandTask(((NodeInfo) currentComponent).getCommandTask());
						break;
					default:
						toolTip = "Unknown component type";
						System.err.println(toolTip);
						break;
					}
					// add to imagelayout instead?
					button.setDescription(toolTip);
					button.setData(newComponent);
					if (button.isSelected()) {
						getSession().setAttribute(ClusterComponent.class, newComponent);
					}
				}
			});

		} // for all nodes

	}
}
