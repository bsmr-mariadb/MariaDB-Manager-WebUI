package com.skysql.consolev.ui;

import java.io.Serializable;

import com.skysql.consolev.api.NodeInfo;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;

public class TabbedPanel implements Serializable {

	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private static final String SYSTEM_NODEID = "0";

	private NodeInfo currentData;
	private Component currentTab;
	private TabSheet tabsheet;
	private PanelNodeInfo panelNodeInfo;
	private PanelControl panelControl;
	private PanelBackup panelBackup;
	private PanelTools panelTools;
	private HorizontalLayout controlTab, backupTab, toolsTab;

	public TabbedPanel() {
		// Set another root layout for the middle panels section.
		tabsheet = new TabSheet();
		tabsheet.setImmediate(true);
		tabsheet.setSizeFull();

		// INFO TAB
		panelNodeInfo = new PanelNodeInfo();
		currentTab = panelNodeInfo;
		tabsheet.addTab(currentTab).setCaption("Info");

		// CONTROL TAB
		controlTab = new HorizontalLayout();
		panelControl = new PanelControl(controlTab);
		tabsheet.addTab(controlTab).setCaption("Control");

		// BACKUP TAB
		backupTab = new HorizontalLayout();
		backupTab.setImmediate(true);
		panelBackup = new PanelBackup(backupTab);
		tabsheet.addTab(backupTab).setCaption("Backup");

		// TOOLS TAB
		toolsTab = new HorizontalLayout();
		toolsTab.setImmediate(true);
		panelTools = new PanelTools(toolsTab);
		tabsheet.addTab(toolsTab).setCaption("Tools");

		// ADD LISTENERS TO TABS
		tabsheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void selectedTabChange(SelectedTabChangeEvent event) {
				final TabSheet source = (TabSheet) event.getSource();
				if (source == tabsheet) {
					Component selectedTab = source.getSelectedTab();
					if (selectedTab != currentTab) {
						currentTab = selectedTab;
						refresh(currentData);
					}
				}
			}
		});
	}

	public TabSheet getTabSheet() {
		return this.tabsheet;
	}

	public void refresh(NodeInfo nodeInfo) {
		tabsheet.getTab(backupTab).setVisible((nodeInfo.getNodeID().equalsIgnoreCase(SYSTEM_NODEID)) ? false : true);

		if (currentTab == panelNodeInfo) {
			panelNodeInfo.refresh(nodeInfo);
		} else if (currentTab == controlTab) {
			panelControl.refresh(nodeInfo);
		} else if (currentTab == backupTab) {
			panelBackup.refresh(nodeInfo);
		} else if (currentTab == toolsTab) {
			panelTools.refresh(nodeInfo);
		}

		currentData = nodeInfo;
	}

}
