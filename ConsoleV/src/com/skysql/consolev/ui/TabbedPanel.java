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

import java.io.Serializable;

import com.skysql.consolev.api.ClusterComponent;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;

public class TabbedPanel implements Serializable {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private Component currentTab;
	private TabSheet tabsheet;
	private PanelInfo panelInfo;
	private PanelControl panelControl;
	private PanelBackup panelBackup;
	private PanelTools panelTools;

	public TabbedPanel() {

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
		panelTools = new PanelTools();
		panelTools.setImmediate(true);
		tabsheet.addTab(panelTools).setCaption("Tools");

		// ADD LISTENERS TO TABS
		tabsheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

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
		ClusterComponent componentInfo = VaadinSession.getCurrent().getAttribute(ClusterComponent.class);

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
