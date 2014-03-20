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

import com.skysql.manager.ClusterComponent;
import com.skysql.manager.ManagerUI;
import com.skysql.manager.SystemRecord;
import com.skysql.manager.TaskRecord;
import com.skysql.manager.api.NodeInfo;
import com.skysql.manager.api.SystemInfo;
import com.skysql.manager.ui.components.ComponentButton;
import com.skysql.manager.ui.components.NodesLayout;
import com.skysql.manager.ui.components.SystemLayout;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;

/**
 * The Class OverviewPanel is used to render the Navigation panel across the top of the screen. "Button" is used to mean custom layout representing a system or node.
 */
@SuppressWarnings("serial")
public class OverviewPanel extends Panel {

	public static float PANEL_HEIGHT = ComponentButton.COMPONENT_HEIGHT + 40;

	public ComponentButton selectedButton;
	private SystemInfo systemInfo;
	private SystemRecord systemRecord;
	private NodesLayout nodesLayout;
	private SystemLayout systemLayout;
	private UpdaterThread updaterThread;
	private boolean isEditable = false;
	private Button addSystemButton, addNodeButton;
	final Label nodesLabel;

	/**
	 * Instantiates a new overview panel.
	 */
	public OverviewPanel() {

		HorizontalLayout overviewContainer = new HorizontalLayout();
		overviewContainer.addStyleName("overviewPanel");
		overviewContainer.setWidth("100%");
		setContent(overviewContainer);

		systemInfo = VaadinSession.getCurrent().getAttribute(SystemInfo.class);
		systemRecord = systemInfo.getCurrentSystem();
		systemLayout = new SystemLayout(systemRecord);
		overviewContainer.addComponent(systemLayout);

		VerticalLayout nodesSlot = new VerticalLayout();
		nodesSlot.addStyleName("nodesSlot");
		nodesSlot.setMargin(new MarginInfo(false, false, false, false));
		overviewContainer.addComponent(nodesSlot);
		overviewContainer.setExpandRatio(nodesSlot, 1.0f);

		final HorizontalLayout nodesHeader = new HorizontalLayout();
		nodesHeader.setStyleName("panelHeaderLayout");
		nodesHeader.setWidth("100%");
		nodesSlot.addComponent(nodesHeader);
		nodesLabel = new Label(" ");
		nodesLabel.setSizeUndefined();
		nodesHeader.addComponent(nodesLabel);
		nodesHeader.setComponentAlignment(nodesLabel, Alignment.MIDDLE_CENTER);
		nodesHeader.setExpandRatio(nodesLabel, 1.0f);

		final HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		buttonsLayout.setMargin(new MarginInfo(false, true, false, false));
		nodesHeader.addComponent(buttonsLayout);
		nodesHeader.setComponentAlignment(buttonsLayout, Alignment.MIDDLE_RIGHT);

		addSystemButton = new Button("Add System...");
		addSystemButton.setDescription("Add System");
		addSystemButton.setVisible(false);
		buttonsLayout.addComponent(addSystemButton);
		addSystemButton.addClickListener(new Button.ClickListener() {

			public void buttonClick(ClickEvent event) {
				new SystemDialog(null, null);
			}
		});

		addNodeButton = new Button("Add Node...");
		addNodeButton.setDescription("Add Node to the current System");
		addNodeButton.setVisible(false);
		buttonsLayout.addComponent(addNodeButton);
		addNodeButton.addClickListener(new Button.ClickListener() {

			public void buttonClick(ClickEvent event) {
				new NodeDialog(null, null);
			}
		});

		final Button editButton = new Button("Edit");
		editButton.setDescription("Enter Editing mode");
		final Button saveButton = new Button("Done");
		saveButton.setDescription("Exit Editing mode");
		buttonsLayout.addComponent(editButton);

		editButton.addClickListener(new Button.ClickListener() {

			public void buttonClick(ClickEvent event) {
				buttonsLayout.replaceComponent(editButton, saveButton);
				isEditable = true;
				systemLayout.setEditable(true);
				nodesLayout.setEditable(true);
				nodesHeader.setStyleName("panelHeaderLayout-editable");
				if (systemRecord != null && !SystemInfo.SYSTEM_ROOT.equals(systemRecord.getID())) {
					addNodeButton.setVisible(true);
				} else {
					addSystemButton.setVisible(true);
				}
				if (systemRecord == null || (systemRecord != null && systemRecord.getNodes().length == 0)) {
					nodesLayout.placeholderLayout(null);
				}
			}
		});

		saveButton.addClickListener(new Button.ClickListener() {

			public void buttonClick(ClickEvent event) {
				buttonsLayout.replaceComponent(saveButton, editButton);
				isEditable = false;
				systemLayout.setEditable(false);
				nodesLayout.setEditable(false);
				nodesHeader.setStyleName("panelHeaderLayout");
				if (systemRecord != null && systemRecord.getNodes().length == 0) {
					nodesLayout.placeholderLayout(null);
				}
				addNodeButton.setVisible(false);
				addSystemButton.setVisible(false);
			}
		});

		Panel panel = new Panel();
		panel.setHeight(PANEL_HEIGHT, Unit.PIXELS);
		panel.addStyleName(Runo.PANEL_LIGHT);
		nodesSlot.addComponent(panel);

		nodesLayout = new NodesLayout(systemRecord);
		nodesLayout.addStyleName("nodesLayout");
		nodesLayout.setWidth("100%");
		panel.setContent(nodesLayout);

	}

	/**
	 * Update button.
	 *
	 * @param button the button
	 * @param state the state
	 * @param taskRecord the task record
	 * @param capacity the capacity
	 */
	public void updateButton(ComponentButton button, String state, TaskRecord taskRecord, String capacity) {
		button.setIcon("node", state, capacity);
		button.setCommandLabel(taskRecord);
	}

	/* (non-Javadoc)
	 * @see com.vaadin.ui.AbstractComponent#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		systemLayout.setEnabled(enabled);
		nodesLayout.setEnabled(enabled);
	}

	/**
	 * Gets the nodes.
	 *
	 * @return the nodes
	 */
	public ArrayList<NodeInfo> getNodes() {
		return nodesLayout.getNodes();
	}

	/**
	 * Gets the node button.
	 *
	 * @param nodeID the node id
	 * @return the node button
	 */
	public ComponentButton getNodeButton(String nodeID) {
		return nodesLayout.getButton(systemRecord.getID(), nodeID);
	}

	/**
	 * Click component button.
	 *
	 * @param buttonIndex the button index
	 * @param isManual the is manual
	 */
	public void clickComponentButton(final int buttonIndex, boolean isManual) {
		clickLayout(nodesLayout.getButton(buttonIndex), isManual);
	}

	/**
	 * Click system button.
	 *
	 * @param isManual the is manual
	 */
	public void clickSystemButton(boolean isManual) {
		clickLayout(systemLayout.getButton(), isManual);
	}

	/**
	 * Click layout.
	 *
	 * @param button_node the button_node
	 * @param isManual the is manual
	 */
	public void clickLayout(final ComponentButton button_node, boolean isManual) {
		ManagerUI.log("clickLayout: " + button_node);

		if (button_node == selectedButton) {
			return;
		}

		if (selectedButton != null) {
			selectedButton.setSelected(false);
		}

		if (button_node != null) {
			button_node.setSelected(true);
			ClusterComponent componentInfo = (ClusterComponent) button_node.getData();
			getSession().setAttribute(ClusterComponent.class, componentInfo);
		} else {
			getSession().setAttribute(ClusterComponent.class, null);
		}

		if (isManual) {
			TabbedPanel tabbedPanel = getSession().getAttribute(TabbedPanel.class);
			tabbedPanel.refresh();
		}

		selectedButton = button_node;

	}

	/**
	 * Select current button.
	 *
	 * @param systemRecord the system record
	 */
	public void selectCurrentButton(SystemRecord systemRecord) {
		if (systemRecord == null) {
			return;
		}

		if (systemRecord.getParentID() == null) {
			clickComponentButton(0, false);
		} else {
			clickSystemButton(false);
		}
	}

	/**
	 * Refresh.
	 */
	public void refresh() {

		ManagerUI.log("OverviewPanel refresh()");
		updaterThread = new UpdaterThread(updaterThread);
		updaterThread.start();

	}

	/**
	 * The Class UpdaterThread.
	 */
	public class UpdaterThread extends Thread {

		/** The old updater thread. */
		UpdaterThread oldUpdaterThread;

		/** The flagged. */
		public volatile boolean flagged = false;

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
				ManagerUI.log("OverviewPanel - Old thread is alive: " + oldUpdaterThread);
				oldUpdaterThread.flagged = true;
				oldUpdaterThread.interrupt();
				try {
					ManagerUI.log("OverviewPanel - Before Join");
					oldUpdaterThread.join();
					ManagerUI.log("OverviewPanel- After Join");
				} catch (InterruptedException iex) {
					ManagerUI.log("OverviewPanel- Interrupted Exception");
					return;
				}

			}

			ManagerUI.log("OverviewPanel- UpdaterThread.this: " + this);
			asynchRefresh(this);

		}
	}

	/**
	 * Asynch refresh.
	 *
	 * @param updaterThread the updater thread
	 */
	private void asynchRefresh(UpdaterThread updaterThread) {

		VaadinSession session = getSession();
		ManagerUI managerUI = session.getAttribute(ManagerUI.class);
		SystemInfo systemInfo = session.getAttribute(SystemInfo.class);
		SystemRecord newSystemRecord = systemInfo.updateSystem(systemInfo.getCurrentID());

		if (isEditable) {
			if (newSystemRecord != null && !SystemInfo.SYSTEM_ROOT.equals(newSystemRecord.getID())) {
				addNodeButton.setVisible(true);
				addSystemButton.setVisible(false);
			} else {
				addSystemButton.setVisible(true);
				addNodeButton.setVisible(false);
			}
		}

		systemLayout.refresh(updaterThread, newSystemRecord);

		if (updaterThread.flagged) {
			return;
		}

		nodesLayout.refresh(updaterThread, newSystemRecord);
		nodesLabel.setValue(newSystemRecord == null || SystemInfo.SYSTEM_ROOT.equals(newSystemRecord.getID()) ? "Systems" : "Components");

		if (updaterThread.flagged) {
			return;
		}

		ManagerUI.log("OverviewPanel.refresh() selectedButton: " + selectedButton);

		if (selectedButton == null) {
			selectCurrentButton(newSystemRecord);
		}

		systemRecord = newSystemRecord;

		managerUI.access(new Runnable() {
			@Override
			public void run() {
				// Here the UI is locked and can be updated

				ManagerUI.log("OverviewPanel - TabbedPanel access run()");

				TabbedPanel tabbedPanel = getSession().getAttribute(TabbedPanel.class);
				tabbedPanel.refresh();

			}
		});

	}
}
