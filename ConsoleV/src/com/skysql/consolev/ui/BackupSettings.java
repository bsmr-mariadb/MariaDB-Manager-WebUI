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


public class BackupSettings {

	private static final String PROPERTY_MAX_BACKUP_COUNT = "maxBackupCount";
	private static final String PROPERTY_MAX_BACKUP_SIZE = "maxBackupSize";

	private String maxBackupSize, maxBackupCount;

	BackupSettings(final HorizontalLayout backupTab, final String systemID) {

		backupTab.setWidth(Sizeable.SIZE_UNDEFINED, 0); // Default
		backupTab.setHeight(Sizeable.SIZE_UNDEFINED, 0); // Default
    	
		backupTab.addStyleName("backupTab");
		backupTab.setSpacing(true);

		VerticalLayout layout = new VerticalLayout();
		backupTab.addComponent(layout);
		layout.setSpacing(true);
		layout.setMargin(true);

        final SystemProperties systemProperties = new SystemProperties(systemID);
 		LinkedHashMap<String, String> properties = systemProperties.getProperties();
        if (properties != null) {
        	maxBackupCount = properties.get(PROPERTY_MAX_BACKUP_COUNT);
        	maxBackupSize = properties.get(PROPERTY_MAX_BACKUP_SIZE);
        }
        	
        NativeSelect selectCount = new NativeSelect("Max number of backups");
        selectCount.setImmediate(true);
        
        SettingsValues countValues = new SettingsValues(PROPERTY_MAX_BACKUP_COUNT);
        ArrayList<String> counts = countValues.getValues();
        for (String value : counts) {
            selectCount.addItem(value);
        }
        selectCount.select(maxBackupCount);
        selectCount.setNullSelectionAllowed(false);
		layout.addComponent(selectCount);
		selectCount.addListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;
            public void valueChange(ValueChangeEvent event) {
            	maxBackupCount = (String)((NativeSelect)event.getProperty()).getValue();
                systemProperties.setProperty(systemID, PROPERTY_MAX_BACKUP_COUNT, maxBackupCount);
                
            }
        });

		NativeSelect selectSize = new NativeSelect("Max total backup size");
		selectSize.setImmediate(true);
        SettingsValues sizeValues = new SettingsValues(PROPERTY_MAX_BACKUP_SIZE);
        ArrayList<String> sizes = sizeValues.getValues();
        for (String value : sizes) {
        	selectSize.addItem(value + " GB");
        }
        selectSize.select(maxBackupSize + " GB");
		selectSize.setNullSelectionAllowed(false);
		layout.addComponent(selectSize);
		selectSize.addListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;
            public void valueChange(ValueChangeEvent event) {
            	maxBackupSize = (String)((NativeSelect)event.getProperty()).getValue();
            	String value = maxBackupSize.substring(0, maxBackupSize.indexOf(" GB"));
                systemProperties.setProperty(systemID, PROPERTY_MAX_BACKUP_SIZE, value);
                
            }
        });

	}

}

