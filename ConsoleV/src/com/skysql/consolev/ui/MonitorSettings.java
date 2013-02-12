package com.skysql.consolev.ui;


import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.skysql.consolev.api.SettingsValues;
import com.skysql.consolev.api.SystemProperties;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.VerticalLayout;

public class MonitorSettings {

	private static final String PROPERTY_MONITOR_INTERVAL = "MonitorInterval";

	private String monitorInterval;

	MonitorSettings(final HorizontalLayout monitorTab, final String systemID) {

		monitorTab.setWidth(Sizeable.SIZE_UNDEFINED, 0); // Default
		monitorTab.setHeight(Sizeable.SIZE_UNDEFINED, 0); // Default
    	
		monitorTab.addStyleName("backupTab");
		monitorTab.setSpacing(true);

		VerticalLayout layout = new VerticalLayout();
		monitorTab.addComponent(layout);
		layout.setSpacing(true);
		layout.setMargin(true);

        final SystemProperties systemProperties = new SystemProperties(systemID);
 		LinkedHashMap<String, String> properties = systemProperties.getProperties();
        if (properties != null) {
        	monitorInterval = properties.get(PROPERTY_MONITOR_INTERVAL);
        }
        	
        NativeSelect selectInterval = new NativeSelect("Monitor interval in seconds");
        selectInterval.setImmediate(true);
        SettingsValues intervalValues = new SettingsValues(PROPERTY_MONITOR_INTERVAL);
        ArrayList<String> intervals = intervalValues.getValues();
        for (String value : intervals) {
        	selectInterval.addItem(value);
        }
        selectInterval.select(monitorInterval);
        selectInterval.setNullSelectionAllowed(false);
		layout.addComponent(selectInterval);
		selectInterval.addListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;
            public void valueChange(ValueChangeEvent event) {
            	monitorInterval = (String)((NativeSelect)event.getProperty()).getValue();
                systemProperties.setProperty(systemID, PROPERTY_MONITOR_INTERVAL, monitorInterval);
                
            }
        });

	}

}

