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

import java.io.Serializable;

import com.skysql.manager.ClusterComponent;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;

@SuppressWarnings("serial")
public class TabbedPanel implements Serializable {

	private Component currentTab;
	private TabSheet tabsheet;
	private PanelInfo panelInfo;
	private PanelControl panelControl;
	private PanelBackup panelBackup;
	private PanelTools panelTools;
	private VaadinSession session;

	public TabbedPanel(VaadinSession session) {
		this.session = session;

		// Set another root layout for the middle panels section.
		tabsheet = new TabSheet();
		tabsheet.setImmediate(true);
		tabsheet.setSizeFull();

		// INFO TAB
		panelInfo = new PanelInfo();
		tabsheet.addTab(panelInfo).setCaption("Info");
		currentTab = panelInfo;

		// CONTROL TAB
		panelControl = new PanelControl();
		tabsheet.addTab(panelControl).setCaption("Control");

		// BACKUP TAB
		panelBackup = new PanelBackup();
		panelBackup.setImmediate(true);
		tabsheet.addTab(panelBackup).setCaption("Backups");

		// TOOLS TAB
		//		SystemInfo systemInfo = session.getAttribute(SystemInfo.class);
		//		LinkedHashMap<String, String> properties = systemInfo.getCurrentSystem().getProperties();
		//		String EIP = properties.get(SystemInfo.PROPERTY_EIP);
		//		String MONyog = properties.get(SystemInfo.PROPERTY_MONYOG);
		//		String phpUrl = properties.get(SystemInfo.PROPERTY_PHPMYADMIN);
		//		if ((EIP != null && MONyog != null) || phpUrl != null) {
		//			panelTools = new PanelTools();
		//			panelTools.setImmediate(true);
		//			tabsheet.addTab(panelTools).setCaption("Tools");
		//		}

		// ADD LISTENERS TO TABS
		tabsheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {

			public void selectedTabChange(SelectedTabChangeEvent event) {
				final TabSheet source = (TabSheet) event.getSource();
				if (source == tabsheet) {
					Component selectedTab = source.getSelectedTab();
					if (selectedTab != currentTab) {
						currentTab = selectedTab;
						refresh();
					}
				}
			}
		});
	}

	public TabSheet getTabSheet() {
		return this.tabsheet;
	}

	public void refresh() {

		ClusterComponent componentInfo = session.getAttribute(ClusterComponent.class);
		if (componentInfo == null || (componentInfo.getType() == ClusterComponent.CCType.system && componentInfo.getParentID() == null)) {
			currentTab.setVisible(false);
			tabsheet.setSelectedTab(currentTab);
			tabsheet.setEnabled(false);
			return;
		} else {
			tabsheet.setEnabled(true);
			currentTab.setVisible(true);
		}

		tabsheet.getTab(panelBackup).setVisible(componentInfo.getType() == ClusterComponent.CCType.system ? true : false);
		tabsheet.getTab(panelControl).setVisible(componentInfo.getType() == ClusterComponent.CCType.system ? false : true);

		if (currentTab == panelInfo) {
			panelInfo.refresh();
		} else if (currentTab == panelControl) {
			panelControl.refresh();
		} else if (currentTab == panelBackup) {
			panelBackup.refresh();
		} else if (currentTab == panelTools) {
			panelTools.refresh();
		}

	}
}
