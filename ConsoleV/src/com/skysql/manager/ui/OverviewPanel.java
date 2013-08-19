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

import com.skysql.manager.ClusterComponent;
import com.skysql.manager.ManagerUI;
import com.skysql.manager.SystemRecord;
import com.skysql.manager.api.NodeInfo;
import com.skysql.manager.api.SystemInfo;
import com.skysql.manager.ui.components.ComponentButton;
import com.skysql.manager.ui.components.NodesLayout;
import com.skysql.manager.ui.components.SystemLayout;
import com.vaadin.server.Sizeable;
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

@SuppressWarnings("serial")
public class OverviewPanel extends Panel {

	private SystemInfo systemInfo;
	private SystemRecord systemRecord;
	public ComponentButton selectedButton;
	private NodesLayout nodesLayout;
	private SystemLayout systemLayout;
	private HorizontalLayout backButton;
	private UpdaterThread updaterThread;

	public OverviewPanel() {

		HorizontalLayout overviewContainer = new HorizontalLayout();
		overviewContainer.addStyleName("overviewPanel");
		overviewContainer.setWidth("100%");
		setContent(overviewContainer);

		VerticalLayout systemSlot = new VerticalLayout();
		systemSlot.addStyleName("systemSlot");
		systemSlot.setWidth(Sizeable.SIZE_UNDEFINED, Sizeable.Unit.PERCENTAGE);
		systemSlot.setMargin(new MarginInfo(false, false, true, false));
		overviewContainer.addComponent(systemSlot);

		final HorizontalLayout systemHeader = new HorizontalLayout();
		systemHeader.addStyleName("panelHeaderLayout");
		systemHeader.setWidth("100%");
		systemHeader.setHeight("23px");
		systemSlot.addComponent(systemHeader);
		final Label systemLabel = new Label("System");
		systemLabel.setSizeUndefined();
		systemHeader.addComponent(systemLabel);
		systemHeader.setComponentAlignment(systemLabel, Alignment.MIDDLE_CENTER);

		systemInfo = VaadinSession.getCurrent().getAttribute(SystemInfo.class);
		systemRecord = systemInfo.getCurrentSystem();
		systemLayout = new SystemLayout(systemRecord);
		systemSlot.addComponent(systemLayout);

		backButton = new HorizontalLayout();
		backButton.setStyleName("backButton");
		systemSlot.addComponent(backButton);

		VerticalLayout nodesSlot = new VerticalLayout();
		nodesSlot.addStyleName("nodesSlot");
		nodesSlot.setMargin(new MarginInfo(false, false, false, true));
		overviewContainer.addComponent(nodesSlot);
		overviewContainer.setExpandRatio(nodesSlot, 1.0f);

		final HorizontalLayout nodesHeader = new HorizontalLayout();
		nodesHeader.addStyleName("panelHeaderLayout");
		nodesHeader.setWidth("100%");
		nodesSlot.addComponent(nodesHeader);
		final Label nodesLabel = new Label("Nodes");
		nodesLabel.setSizeUndefined();
		nodesHeader.addComponent(nodesLabel);
		nodesHeader.setComponentAlignment(nodesLabel, Alignment.MIDDLE_CENTER);
		nodesHeader.setExpandRatio(nodesLabel, 1.0f);

		final HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		buttonsLayout.setMargin(new MarginInfo(false, true, false, false));
		nodesHeader.addComponent(buttonsLayout);
		nodesHeader.setComponentAlignment(buttonsLayout, Alignment.MIDDLE_RIGHT);

		final Button addNodeButton = new Button("Add Node...");
		addNodeButton.setVisible(false);
		buttonsLayout.addComponent(addNodeButton);
		addNodeButton.addClickListener(new Button.ClickListener() {

			public void buttonClick(ClickEvent event) {
				new NodeDialog(null, null);
			}
		});

		final Button editButton = new Button("Edit");
		final Button saveButton = new Button("Done");
		buttonsLayout.addComponent(editButton);
		editButton.addClickListener(new Button.ClickListener() {

			public void buttonClick(ClickEvent event) {
				buttonsLayout.replaceComponent(editButton, saveButton);
				systemLayout.setEditable(true);
				nodesLayout.setEditable(true);
				addNodeButton.setVisible(true);
			}
		});

		saveButton.addClickListener(new Button.ClickListener() {

			public void buttonClick(ClickEvent event) {
				buttonsLayout.replaceComponent(saveButton, editButton);
				systemLayout.setEditable(false);
				nodesLayout.setEditable(false);
				addNodeButton.setVisible(false);
			}
		});

		Panel panel = new Panel();
		panel.setHeight("155px");
		panel.addStyleName(Runo.PANEL_LIGHT);
		nodesSlot.addComponent(panel);

		nodesLayout = new NodesLayout(systemRecord);
		nodesLayout.addStyleName("nodesLayout");
		nodesLayout.setWidth("100%");
		panel.setContent(nodesLayout);

	}

	public ArrayList<NodeInfo> getNodes() {
		return nodesLayout.getNodes();
	}

	public void clickLayout(final int buttonIndex) {
		if (buttonIndex > 0) {
			clickLayout(getNodes().get(buttonIndex - 1).getButton());
		} else {
			clickLayout(systemLayout.getButton());
		}
	}

	public void clickLayout(final ComponentButton button_node) {
		if (selectedButton != null) {
			selectedButton.setSelected(false);
		}
		selectedButton = button_node;
		selectedButton.setSelected(true);

		ClusterComponent componentInfo = (ClusterComponent) button_node.getData();
		getSession().setAttribute(ClusterComponent.class, componentInfo);

		TabbedPanel tabbedPanel = getSession().getAttribute(TabbedPanel.class);
		tabbedPanel.refresh();

	}

	public void refresh() {

		ManagerUI.log("OverviewPanel refresh()");
		updaterThread = new UpdaterThread(updaterThread);
		updaterThread.start();

	}

	public class UpdaterThread extends Thread {
		UpdaterThread oldUpdaterThread;
		public volatile boolean flagged = false;

		UpdaterThread(UpdaterThread oldUpdaterThread) {
			this.oldUpdaterThread = oldUpdaterThread;
		}

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

	private void asynchRefresh(UpdaterThread updaterThread) {

		boolean refresh = false;
		ManagerUI managerUI = getSession().getAttribute(ManagerUI.class);

		SystemRecord newSystemRecord = systemInfo.updateSystem(systemRecord.getID());
		final ComponentButton button = systemRecord.getButton();

		final String newName;
		if ((newName = newSystemRecord.getName()) != null && !newName.equals(systemRecord.getName())) {
			refresh = true;
		}

		String[] newNodes;
		boolean reloadNodes = false;
		if ((newNodes = newSystemRecord.getNodes()) != null && (!Arrays.equals(newNodes, systemRecord.getNodes()) || nodesLayout.getButtons().isEmpty())) {
			ManagerUI.log("ReloadNodes");
			refresh = true;
			reloadNodes = true;
		}

		if (refresh) {
			newSystemRecord.setButton(systemRecord.getButton());
			systemRecord = newSystemRecord;
			// will this have already been updated?
			//getSession().setAttribute(SystemInfo.class, systemInfo);

			managerUI.access(new Runnable() {
				@Override
				public void run() {
					// Here the UI is locked and can be updated

					ManagerUI.log("OverviewPanel access run()");

					button.setName(newName);
					button.setData(systemRecord);
					button.setDescription(systemRecord.ToolTip());
				}
			});

		}

		if (updaterThread.flagged) {
			ManagerUI.log("OverviewPanel - flagged is set before Nodes refresh");
			return;
		}

		nodesLayout.refresh(updaterThread, reloadNodes ? newSystemRecord : null);

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
