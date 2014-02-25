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

import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;

import com.skysql.manager.DateConversion;
import com.skysql.manager.api.SettingsValues;
import com.skysql.manager.api.SystemInfo;
import com.skysql.manager.api.UserObject;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.server.UserError;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class GeneralSettings extends HorizontalLayout {
	public static boolean DEFAULT_TIME_ADJUST = false;
	public static boolean DEFAULT_COMMAND_EXECUTION = false;

	private String maxBackupSize, maxBackupCount;
	private UserObject userObject;
	private SettingsDialog settingsDialog;

	GeneralSettings(SettingsDialog settingsDialog) {
		this.settingsDialog = settingsDialog;

		addStyleName("generalTab");
		setWidth("458px");
		setSpacing(false);

		VerticalLayout layout = new VerticalLayout();
		layout.setWidth("100%");
		addComponent(layout);

		final Label separator = new Label("<hr>", ContentMode.HTML);

		userObject = VaadinSession.getCurrent().getAttribute(UserObject.class);

		//layout.addComponent(backupsLayout());
		//layout.addComponent(separator);
		layout.addComponent(timeLayout());
		//layout.addComponent(separator);
		//layout.addComponent(commandsLayout());

	}

	private VerticalLayout backupsLayout() {

		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);

		final Label label = new Label("<h2>Backups</h2>", ContentMode.HTML);
		layout.addComponent(label);

		final SystemInfo systemInfo = VaadinSession.getCurrent().getAttribute(SystemInfo.class);
		LinkedHashMap<String, String> properties = systemInfo.getCurrentSystem().getProperties();
		if (properties != null) {
			maxBackupCount = properties.get(SystemInfo.PROPERTY_DEFAULTMAXBACKUPCOUNT);
			maxBackupSize = properties.get(SystemInfo.PROPERTY_DEFAULTMAXBACKUPSIZE);
		}

		NativeSelect selectCount = new NativeSelect("Max number of backups");
		selectCount.setImmediate(true);

		SettingsValues countValues = new SettingsValues(SettingsValues.SETTINGS_MAX_BACKUP_COUNT);
		String[] counts = countValues.getValues();
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
		String[] sizes = sizeValues.getValues();
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

		return layout;
	}

	private VerticalLayout timeLayout() {

		VerticalLayout layout = new VerticalLayout();
		layout.setWidth("100%");
		layout.setSpacing(true);
		layout.setMargin(new MarginInfo(false, true, false, true));

		final Label title = new Label("<h3>Date & Time Presentation</h3>", ContentMode.HTML);
		title.setSizeUndefined();
		layout.addComponent(title);

		final Label explanation = new Label(
				"Determines if date & time stamps are displayed in the originally recorded format or adjusted to the local timezone.");
		explanation.setSizeFull();
		layout.addComponent(explanation);

		OptionGroup option = new OptionGroup("Select an option");
		option.addItem(false);
		option.setItemCaption(false, "Show original time");
		option.addItem(true);
		option.setItemCaption(true, "Adjust to local timezone and customize format");

		String propertyTimeAdjust = userObject.getProperty(UserObject.PROPERTY_TIME_ADJUST);
		option.select(propertyTimeAdjust == null ? DEFAULT_TIME_ADJUST : Boolean.valueOf(propertyTimeAdjust));
		option.setNullSelectionAllowed(false);
		option.setHtmlContentAllowed(true);
		option.setImmediate(true);
		layout.addComponent(option);

		final HorizontalLayout defaultLayout = new HorizontalLayout();
		defaultLayout.setSpacing(true);
		layout.addComponent(defaultLayout);

		final Form form = new Form();
		form.setFooter(null);
		final TextField timeFormat = new TextField("Format");
		form.addField("timeFormat", timeFormat);
		defaultLayout.addComponent(form);

		final DateConversion dateConversion = VaadinSession.getCurrent().getAttribute(DateConversion.class);

		option.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(final ValueChangeEvent event) {
				boolean value = (Boolean) event.getProperty().getValue();
				dateConversion.setAdjuts(value);
				userObject.setProperty(UserObject.PROPERTY_TIME_ADJUST, String.valueOf(value));
				timeFormat.setEnabled(value);
				settingsDialog.setRefresh(true);
				if (value == false) {
					timeFormat.removeAllValidators();
					timeFormat.setComponentError(null);
					form.setComponentError(null);
					form.setValidationVisible(false);
					settingsDialog.setClose(true);
				} else {
					timeFormat.addValidator(new TimeFormatValidator());
					timeFormat.setInputPrompt(DateConversion.DEFAULT_TIME_FORMAT);
					//timeFormat.setClickShortcut(KeyCode.ENTER);
				}
			}
		});

		String propertyTimeFormat = userObject.getProperty(UserObject.PROPERTY_TIME_FORMAT);
		propertyTimeFormat = (propertyTimeFormat == null ? DateConversion.DEFAULT_TIME_FORMAT : String.valueOf(propertyTimeFormat));

		timeFormat.setColumns(16);
		timeFormat.setValue(propertyTimeFormat);
		timeFormat.setImmediate(true);
		timeFormat.setRequired(true);
		timeFormat.setRequiredError("Format cannot be empty.");
		if (option.isSelected(false)) {
			timeFormat.setEnabled(false);
		} else {
			timeFormat.addValidator(new TimeFormatValidator());
		}

		timeFormat.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(final ValueChangeEvent event) {
				String value = (String) event.getProperty().getValue();
				try {
					form.setComponentError(null);
					form.commit();
					timeFormat.setComponentError(null);
					form.setValidationVisible(false);
					dateConversion.setFormat(value);
					userObject.setProperty(UserObject.PROPERTY_TIME_FORMAT, value);
					settingsDialog.setClose(true);
					settingsDialog.setRefresh(true);
				} catch (InvalidValueException e) {
					settingsDialog.setClose(false);
					timeFormat.setComponentError(new UserError("Invalid Format (Java SimpleDateFormat)."));
					timeFormat.focus();
				}
			}
		});

		Button defaultButton = new Button("Restore Default");
		defaultLayout.addComponent(defaultButton);
		defaultLayout.setComponentAlignment(defaultButton, Alignment.MIDDLE_LEFT);
		defaultButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(Button.ClickEvent event) {
				timeFormat.setValue(DateConversion.DEFAULT_TIME_FORMAT);
			}
		});

		return layout;
	}

	private VerticalLayout commandsLayout() {

		VerticalLayout layout = new VerticalLayout();
		layout.setWidth("100%");
		layout.setSpacing(true);
		layout.setMargin(new MarginInfo(false, true, true, true));

		final Label title = new Label("<h3>Commands Execution</h3>", ContentMode.HTML);
		title.setSizeUndefined();
		layout.addComponent(title);

		final Label explanation = new Label(
				"Determines if the command you selected will be executed only if the original steps displayed at the time of selection are still applicable, or if a suitable variation (depending on node state) can be substituted when you press \"Run\".");
		explanation.setSizeFull();
		layout.addComponent(explanation);

		OptionGroup option = new OptionGroup("Select an option");
		option.addItem(false);
		option.setItemCaption(false, "Strict - only original steps");
		option.addItem(true);
		option.setItemCaption(true, "Loose - any variation");

		String propertyCommandExecution = userObject.getProperty(UserObject.PROPERTY_COMMAND_EXECUTION);
		option.select(propertyCommandExecution == null ? DEFAULT_COMMAND_EXECUTION : Boolean.valueOf(propertyCommandExecution));
		option.setNullSelectionAllowed(false);
		option.setHtmlContentAllowed(true);
		option.setImmediate(true);
		layout.addComponent(option);

		option.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(final ValueChangeEvent event) {
				Object value = event.getProperty().getValue();
				userObject.setProperty(UserObject.PROPERTY_COMMAND_EXECUTION, String.valueOf(value));
			}
		});

		return layout;
	}

	class TimeFormatValidator implements Validator {
		private String error;

		public boolean isValid(Object value) {
			// ignore an empty field
			if (value == null || (value != null && value.toString().isEmpty())) {
				return true;
			}

			try {
				new SimpleDateFormat((String) value);
				return true;
			} catch (IllegalArgumentException e) {
				settingsDialog.setClose(false);
				//timeFormat.focus();
				return false;
			}
		}

		// Upon failure, the validate() method throws an exception
		public void validate(Object value) throws InvalidValueException {
			if (!isValid(value)) {
				throw new InvalidValueException(error);
			} else {

			}
		}
	}

}
