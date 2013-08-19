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

import com.skysql.manager.ClusterComponent;
import com.skysql.manager.ManagerUI;
import com.skysql.manager.SystemRecord;
import com.skysql.manager.api.NodeInfo;
import com.skysql.manager.ui.NodeDialog;
import com.skysql.manager.ui.OverviewPanel;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

@SuppressWarnings("serial")
public class NodesLayout extends HorizontalLayout {

	private boolean isEditable;
	private ArrayList<ComponentButton> buttons = new ArrayList<ComponentButton>();

	public NodesLayout(SystemRecord systemRecord) {

		addStyleName("network");

		addLayoutClickListener(new LayoutClickListener() {

			public void layoutClick(LayoutClickEvent event) {

				Component child;
				if (event.isDoubleClick() && (child = event.getChildComponent()) != null && (child instanceof ComponentButton)) {
					// Get the child component which was double-clicked
					ComponentButton button = (ComponentButton) child;
					NodeInfo nodeInfo = (NodeInfo) button.getData();
					if (isEditable) {
						new NodeDialog(nodeInfo, null);
					}

				} else if (!isEditable && (child = event.getChildComponent()) != null && (child instanceof ComponentButton)) {
					// Get the child component which was clicked
					ComponentButton button = (ComponentButton) child;
					OverviewPanel overviewPanel = getSession().getAttribute(OverviewPanel.class);
					overviewPanel.clickLayout(button);
				}

			}
		});

	}

	public ArrayList<ComponentButton> getButtons() {
		return buttons;
	}

	public ArrayList<NodeInfo> getNodes() {
		ArrayList<NodeInfo> nodes = new ArrayList<NodeInfo>(buttons.size());
		for (ComponentButton button : buttons) {
			nodes.add((NodeInfo) button.getData());
		}
		return nodes;
	}

	public void deleteComponent(ComponentButton button) {
		buttons.remove(button);
		removeComponent(button);
		OverviewPanel overviewPanel = getSession().getAttribute(OverviewPanel.class);
		if (button.isSelected()) {
			overviewPanel.clickLayout(0);
		}
		overviewPanel.refresh();
	}

	public void setEditable(boolean editable) {
		isEditable = editable;
		for (ComponentButton button : buttons) {
			button.setEditable(editable);
		}
	}

	public synchronized void refresh(final OverviewPanel.UpdaterThread updaterThread, final SystemRecord systemRecord) {
		boolean refresh = false;

		VaadinSession session = getSession();
		ManagerUI managerUI = session.getAttribute(ManagerUI.class);

		if (systemRecord != null) {
			refresh = true;

			session.lock();
			try {

				// Here the UI is locked and can be updated
				ManagerUI.log("NodesLayout access run() removeall");
				removeAllComponents();

				buttons = new ArrayList<ComponentButton>();
				for (String nodeID : systemRecord.getNodes()) {
					final NodeInfo nodeInfo = new NodeInfo(systemRecord.getID());
					nodeInfo.setID(nodeID);

					if (updaterThread.flagged) {
						ManagerUI.log("NodesLayout - flagged is set while adding nodes");
						return;
					}

					ManagerUI.log("NodesLayout access run() adding nodes");

					ComponentButton button = new ComponentButton(nodeInfo, null);
					addComponent(button);
					button.setEditable(isEditable);
					setComponentAlignment(button, Alignment.MIDDLE_CENTER);
					buttons.add(button);
				}
			} finally {
				session.unlock();
			}

		}
		if (updaterThread.flagged) {
			ManagerUI.log("NodesLayout - flagged is set after removeall");
			return;
		}

		ManagerUI.log("NodesLayout - before for loop - buttons: " + buttons.size());

		final boolean finalRefresh = refresh;

		for (final ComponentButton button : buttons) {
			final NodeInfo nodeInfo = (NodeInfo) button.getData();
			final NodeInfo newInfo = new NodeInfo(nodeInfo.getSystemID(), nodeInfo.getID());
			newInfo.setButton(button);

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

					ManagerUI.log("NodesLayout access run() button " + newInfo.getName());

					boolean refresh = finalRefresh;

					String newName;
					if ((newName = newInfo.getName()) != null && !newName.equals(nodeInfo.getName())) {
						refresh = true;
						button.setName(newName);
					}

					String newStatus = newInfo.getStatus();
					String newCapacity = newInfo.getCapacity();
					if ((newStatus != null && !newStatus.equals(nodeInfo.getStatus()))
							|| ((newStatus != null) && newCapacity != null && !newCapacity.equals(nodeInfo.getCapacity()))) {
						refresh = true;

						button.setIcon(nodeInfo.getType().toString(), newStatus, newCapacity);
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

						if (nodeInfo == getSession().getAttribute(ClusterComponent.class)) {
							getSession().setAttribute(ClusterComponent.class, newInfo);
						}
						button.setData(newInfo);
						button.setDescription(newInfo.ToolTip());
					}
				}
			});

		} // for all nodes

	}
}
