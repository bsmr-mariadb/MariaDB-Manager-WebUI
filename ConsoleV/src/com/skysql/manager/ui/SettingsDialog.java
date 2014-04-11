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

import com.skysql.manager.ClusterComponent;
import com.skysql.manager.api.SystemInfo;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;

/**
 * The Class SettingsDialog.
 */
public class SettingsDialog implements Window.CloseListener {

	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private ModalWindow dialogWindow;
	private Button openButton;
	private TabSheet tabsheet;
	private String selectedTab;
	private boolean refresh = false;
	private boolean switchAllowed = true;
	private SettingsDialog settingsDialog;

	/** The settings dialog open listener. */
	private ClickListener settingsDialogOpenListener = new ClickListener() {
		private static final long serialVersionUID = 0x4C656F6E6172646FL;

		public void buttonClick(Button.ClickEvent event) {

			dialogWindow = new ModalWindow("Settings", "500px");
			dialogWindow.addCloseListener(settingsDialog);
			UI.getCurrent().addWindow(dialogWindow);

			tabsheet = new TabSheet();
			tabsheet.setImmediate(true);

			// General Tab
			GeneralSettings backupsTab = new GeneralSettings(settingsDialog);
			Tab tab = tabsheet.addTab(backupsTab, "General");
			if (selectedTab != null && selectedTab.equals("General")) {
				tabsheet.setSelectedTab(tab);
			}

			// Users Tab
			UsersSettings usersTab = new UsersSettings();
			tab = tabsheet.addTab(usersTab, "Users");
			if (selectedTab != null && selectedTab.equals("Users")) {
				tabsheet.setSelectedTab(tab);
			}

			// Monitors Tab
			SystemInfo systemInfo = VaadinSession.getCurrent().getAttribute(SystemInfo.class);
			String systemID = systemInfo.getCurrentID();
			String systemType = null;
			if (systemID.equals(SystemInfo.SYSTEM_ROOT)) {
				ClusterComponent clusterComponent = VaadinSession.getCurrent().getAttribute(ClusterComponent.class);
				if (clusterComponent != null) {
					systemType = clusterComponent.getSystemType();
					systemID = clusterComponent.getID();
				}
			} else {
				systemType = systemInfo.getCurrentSystem().getSystemType();
			}
			if (systemType != null) {
				MonitorsSettings monitorsTab = new MonitorsSettings(settingsDialog, systemID, systemType);
				tab = tabsheet.addTab(monitorsTab, "Monitors");
				if (selectedTab != null && selectedTab.equals("Monitors")) {
					tabsheet.setSelectedTab(tab);
				}
			}

			// About Tab
			AboutSettings aboutTab = new AboutSettings(event.isAltKey());
			tab = tabsheet.addTab(aboutTab, "About");
			if (selectedTab != null && selectedTab.equals("About")) {
				tabsheet.setSelectedTab(tab);
			}

			((ComponentContainer) dialogWindow.getContent()).addComponent(tabsheet);

			// Handling tab changes
			tabsheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
				private static final long serialVersionUID = -2358653511430014752L;

				Component selected = tabsheet.getSelectedTab();
				boolean preventEvent = false;

				public void selectedTabChange(SelectedTabChangeEvent event) {
					if (preventEvent) {
						preventEvent = false;
						return;
					}
					// Check the previous tab
					if (switchAllowed) {
						selected = tabsheet.getSelectedTab();
					} else {
						// Revert the tab change
						preventEvent = true; // Prevent secondary change event
						tabsheet.setSelectedTab(selected);
					}
				}
			});

		}
	};

	/**
	 * Instantiates a new settings dialog.
	 *
	 * @param label the label
	 */
	public SettingsDialog(String label) {

		openButton = new Button(label, settingsDialogOpenListener);
		settingsDialog = this;

	}

	/**
	 * Instantiates a new settings dialog.
	 *
	 * @param label the label
	 * @param selectedTab the selected tab
	 */
	public SettingsDialog(String label, String selectedTab) {

		this(label);
		this.selectedTab = selectedTab;

	}

	/**
	 * Sets the refresh.
	 *
	 * @param refresh the new refresh
	 */
	public void setRefresh(boolean refresh) {
		this.refresh = refresh;
	}

	/**
	 * Sets the close.
	 *
	 * @param close the new close
	 */
	public void setClose(boolean close) {
		dialogWindow.setClose(close);
		switchAllowed = close;
		//dialogWindow.setClosable(close);
	}

	/**
	 * Gets the button.
	 *
	 * @return the button
	 */
	public Button getButton() {
		return (openButton);
	}

	/* (non-Javadoc)
	 * @see com.vaadin.ui.Window.CloseListener#windowClose(com.vaadin.ui.Window.CloseEvent)
	 */
	public void windowClose(CloseEvent e) {
		if (refresh) {
			OverviewPanel overviewPanel = VaadinSession.getCurrent().getAttribute(OverviewPanel.class);
			overviewPanel.refresh();
		}

	}

}
