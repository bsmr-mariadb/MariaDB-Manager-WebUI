package com.skysql.consolev.ui;

import com.skysql.consolev.SessionData;
import com.skysql.consolev.api.NodeInfo;

import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;

public class TabbedPanel {
	private static final String SYSTEM_NODEID = "0";

	private NodeInfo currentData, lastInfoPanelData;
	private Component currentTab;
	private TabSheet tabsheet;
	private PanelNodeInfo panelNodeInfo;
	private PanelSystemInfo panelSystemInfo;
	private PanelControl panelControl;
	private PanelBackup panelBackup;
	private PanelTools panelTools;
	private HorizontalLayout nodeInfoTab, systemInfoTab, controlTab, backupTab, toolsTab;
	
    public TabbedPanel(Object userData) {
		// Set another root layout for the middle panels section.
        tabsheet = new TabSheet();
        tabsheet.setImmediate(true);
        
        //  INFO TAB
        nodeInfoTab = new HorizontalLayout();
        panelNodeInfo = new PanelNodeInfo(nodeInfoTab);
        systemInfoTab = new HorizontalLayout();
        panelSystemInfo = new PanelSystemInfo(systemInfoTab, userData);
        tabsheet.addTab(systemInfoTab).setCaption("Info");
        
        //  CONTROL TAB
        controlTab = new HorizontalLayout();
        panelControl = new PanelControl(controlTab, userData);
        tabsheet.addTab(controlTab).setCaption("Control");

        //  BACKUP TAB
        backupTab = new HorizontalLayout();
        backupTab.setImmediate(true);
        panelBackup = new PanelBackup(backupTab, userData);
        tabsheet.addTab(backupTab).setCaption("Backup");

        //  TOOLS TAB
        toolsTab = new HorizontalLayout();
        toolsTab.setImmediate(true);
        panelTools = new PanelTools(toolsTab, userData);
        tabsheet.addTab(toolsTab).setCaption("Tools");

        currentTab = systemInfoTab;
        
        // ADD LISTENERS TO TABS
        tabsheet.addListener(new TabSheet.SelectedTabChangeListener() {
        	private static final long serialVersionUID = 0x4C656F6E6172646FL;
            public void selectedTabChange(SelectedTabChangeEvent event) {
        		final TabSheet source = (TabSheet)event.getSource();		
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
		boolean refreshed = false;
		
		tabsheet.getTab(backupTab).setVisible((nodeInfo.getNodeID().equalsIgnoreCase(SYSTEM_NODEID)) ? false : true);

		if (currentTab == nodeInfoTab || currentTab == systemInfoTab) {
			// switch out System or Node Info tab, if necessary
			String newNodeID = nodeInfo.getNodeID();
			String oldNodeID = (lastInfoPanelData != null) ? lastInfoPanelData.getNodeID() : null;
			
			if ((oldNodeID != null) && !oldNodeID.equalsIgnoreCase(newNodeID) && (newNodeID.equalsIgnoreCase(SYSTEM_NODEID) || oldNodeID.equalsIgnoreCase(SYSTEM_NODEID))) {
				if (newNodeID.equalsIgnoreCase(SYSTEM_NODEID)) {
					tabsheet.replaceComponent(nodeInfoTab, systemInfoTab);
				} else {
					tabsheet.replaceComponent(systemInfoTab, nodeInfoTab);
				}
			}

			// reload data
			if (newNodeID.equalsIgnoreCase(SYSTEM_NODEID)) {
				refreshed = panelSystemInfo.refresh(nodeInfo);
			} else {
				refreshed = panelNodeInfo.refresh(nodeInfo);
			}
			
			lastInfoPanelData = nodeInfo;
			
		} else if (currentTab == controlTab) {
			refreshed = panelControl.refresh(nodeInfo);
		} else if (currentTab == backupTab) {
			refreshed = panelBackup.refresh(nodeInfo);
		} else if (currentTab == toolsTab) {
			refreshed = panelTools.refresh(nodeInfo);
		}
		
		if (refreshed)
			((SessionData)tabsheet.getApplication().getUser()).getICEPush().push();

		currentData = nodeInfo;
	}

}
