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
 * Copyright SkySQL Ab
 */

package com.skysql.consolev.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.skysql.consolev.api.SettingsValues;
import com.skysql.consolev.api.SystemInfo;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.VerticalLayout;

public class BackupSettings {

	private String maxBackupSize, maxBackupCount;

	BackupSettings(final HorizontalLayout backupTab) {

		backupTab.addStyleName("backupTab");
		backupTab.setSpacing(true);

		VerticalLayout layout = new VerticalLayout();
		backupTab.addComponent(layout);
		layout.setSpacing(true);
		layout.setMargin(true);

		final SystemInfo systemInfo = VaadinSession.getCurrent().getAttribute(SystemInfo.class);
		LinkedHashMap<String, String> properties = systemInfo.getProperties();
		if (properties != null) {
			maxBackupCount = properties.get(SystemInfo.PROPERTY_DEFAULTMAXBACKUPCOUNT);
			maxBackupSize = properties.get(SystemInfo.PROPERTY_DEFAULTMAXBACKUPSIZE);
		}

		NativeSelect selectCount = new NativeSelect("Max number of backups");
		selectCount.setImmediate(true);

		SettingsValues countValues = new SettingsValues(SettingsValues.SETTINGS_MAX_BACKUP_COUNT);
		ArrayList<String> counts = countValues.getValues();
		for (String value : counts) {
			selectCount.addItem(value);
		}
		selectCount.select(maxBackupCount);
		selectCount.setNullSelectionAllowed(false);
		layout.addComponent(selectCount);
		selectCount.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {
				maxBackupCount = (String) ((NativeSelect) event.getProperty()).getValue();
				systemInfo.setProperty(SystemInfo.PROPERTY_DEFAULTMAXBACKUPCOUNT, maxBackupCount);

			}
		});

		NativeSelect selectSize = new NativeSelect("Max total backup size");
		selectSize.setImmediate(true);
		SettingsValues sizeValues = new SettingsValues(SettingsValues.SETTINGS_MAX_BACKUP_SIZE);
		ArrayList<String> sizes = sizeValues.getValues();
		for (String value : sizes) {
			selectSize.addItem(value + " GB");
		}
		selectSize.select(maxBackupSize + " GB");
		selectSize.setNullSelectionAllowed(false);
		layout.addComponent(selectSize);
		selectSize.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {
				maxBackupSize = (String) ((NativeSelect) event.getProperty()).getValue();
				String value = maxBackupSize.substring(0, maxBackupSize.indexOf(" GB"));
				systemInfo.setProperty(SystemInfo.PROPERTY_DEFAULTMAXBACKUPSIZE, value);

			}
		});

	}

}
