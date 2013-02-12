package com.skysql.consolev.ui;

import java.util.LinkedHashMap;

import com.skysql.consolev.SessionData;

import com.skysql.consolev.api.NodeInfo;
import com.skysql.consolev.api.SystemInfo;
import com.skysql.consolev.api.SystemProperties;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;

public class PanelTools {

	private static final String SYSTEM_NODEID = "0";

	private Link phpLink, monyogLink;
	private String phpUrl;
	
    PanelTools(HorizontalLayout thisTab, Object userData) {

        //thisTab.setSizeFull();
    	thisTab.setWidth(Sizeable.SIZE_UNDEFINED, 0); // Default
    	thisTab.setHeight("200px");
        thisTab.setSpacing(true);

		// External Tools Vertical Module
        SystemInfo sysInfo = ((SessionData)userData).getSystemInfo();
        SystemProperties systemProperties = new SystemProperties(sysInfo.getSystemID());
 		LinkedHashMap<String, String> properties = systemProperties.getProperties();
        if (properties != null) {
        	VerticalLayout externalsLayout = new VerticalLayout();
        	externalsLayout.setWidth("150px");
        	externalsLayout.addStyleName("externalsLayout");
        	externalsLayout.setSpacing(true);
        	        	        	        
        	String EIP = properties.get("EIP");
        	String MONyog = properties.get("MONyog");
        	if (EIP != null && MONyog != null) {
        		String url = "http://" + EIP + MONyog;
        		monyogLink = new Link("MONyog", new ExternalResource(url));
        		monyogLink.setTargetName("_blank");
        		monyogLink.setDescription("Open MONyog for the whole system");
        		monyogLink.setIcon(new ThemeResource("img/externalLink.png"));
        		monyogLink.addStyleName("icon-after-caption");
        		externalsLayout.addComponent(monyogLink);
        		externalsLayout.setComponentAlignment(monyogLink, Alignment.BOTTOM_CENTER);
        	}
                	
        	phpUrl = properties.get("phpMyAdmin");
        	phpLink = new Link("phpMyAdmin", null);
        	phpLink.setTargetName("_blank");
        	phpLink.setDescription("Open phpMyAdmin for the selected node");
        	phpLink.setIcon(new ThemeResource("img/externalLink.png"));
        	phpLink.addStyleName("icon-after-caption");
        	externalsLayout.addComponent(phpLink);
        	externalsLayout.setComponentAlignment(phpLink, Alignment.BOTTOM_CENTER);
        	
        	thisTab.addComponent(externalsLayout);
        	thisTab.setComponentAlignment(externalsLayout, Alignment.MIDDLE_CENTER);

        }
 
		{
	        Label spacer = new Label();
	        spacer.setWidth("40px");
	        thisTab.addComponent(spacer);
		}

        // Scripting layout placeholder
        VerticalLayout placeholderLayout = new VerticalLayout();
        placeholderLayout.addStyleName("placeholderLayout");
        placeholderLayout.setSizeUndefined();

        Label placeholderLabel = new Label("Links to external tools");
        placeholderLabel.addStyleName("placeholder");
        placeholderLayout.addComponent(placeholderLabel);

        thisTab.addComponent(placeholderLayout);
        thisTab.setComponentAlignment(placeholderLayout, Alignment.MIDDLE_CENTER);

 	}
	
	public boolean refresh(NodeInfo nodeInfo) {
		boolean refreshed = true;
		
		if (nodeInfo.getNodeID().equalsIgnoreCase(SYSTEM_NODEID)) {
			phpLink.setVisible(false);
		} else {
    		String url = "http://" + nodeInfo.getPublicIP() + phpUrl;
    		phpLink.setResource(new ExternalResource(url));
			phpLink.setVisible(true);
		}
		
		return refreshed;
	}

}
