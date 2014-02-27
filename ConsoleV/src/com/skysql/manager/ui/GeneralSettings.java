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
import com.vaadin.server.ThemeResource;
import com.vaadin.server.UserError;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
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

		HorizontalLayout titleLayout = new HorizontalLayout();
		titleLayout.setSpacing(true);
		layout.addComponent(titleLayout);

		final Label title = new Label("<h3>Date & Time Presentation</h3>", ContentMode.HTML);
		title.setSizeUndefined();
		titleLayout.addComponent(title);

		Embedded info = new Embedded(null, new ThemeResource("img/info.png"));
		info.addStyleName("infoButton");
		info.setDescription("Determines if date & time stamps are displayed in the originally recorded format or adjusted to the local timezone.<br/>The format can be customized by using the following Java 6 SimpleDateFormat patterns:"
				+ "</blockquote>"
				+ " <table border=0 cellspacing=3 cellpadding=0 summary=\"Chart shows pattern letters, date/time component, presentation, and examples.\">\n"
				+ "     <tr bgcolor=\"#ccccff\">\n"
				+ "         <th align=left>Letter\n"
				+ "         <th align=left>Date or Time Component\n"
				+ "         <th align=left>Presentation\n"
				+ "         <th align=left>Examples\n"
				+ "     <tr>\n"
				+ "         <td><code>G</code>\n"
				+ "         <td>Era designator\n"
				+ "         <td><a href=\"#text\">Text</a>\n"
				+ "         <td><code>AD</code>\n"
				+ "     <tr bgcolor=\"#eeeeff\">\n"
				+ "         <td><code>y</code>\n"
				+ "         <td>Year\n"
				+ "         <td><a href=\"#year\">Year</a>\n"
				+ "         <td><code>1996</code>; <code>96</code>\n"
				+ "     <tr>\n"
				+ "         <td><code>M</code>\n"
				+ "         <td>Month in year\n"
				+ "         <td><a href=\"#month\">Month</a>\n"
				+ "         <td><code>July</code>; <code>Jul</code>; <code>07</code>\n"
				+ "     <tr bgcolor=\"#eeeeff\">\n"
				+ "         <td><code>w</code>\n"
				+ "         <td>Week in year\n"
				+ "         <td><a href=\"#number\">Number</a>\n"
				+ "         <td><code>27</code>\n"
				+ "     <tr>\n"
				+ "         <td><code>W</code>\n"
				+ "         <td>Week in month\n"
				+ "         <td><a href=\"#number\">Number</a>\n"
				+ "         <td><code>2</code>\n"
				+ "     <tr bgcolor=\"#eeeeff\">\n"
				+ "         <td><code>D</code>\n"
				+ "         <td>Day in year\n"
				+ "         <td><a href=\"#number\">Number</a>\n"
				+ "         <td><code>189</code>\n"
				+ "     <tr>\n"
				+ "         <td><code>d</code>\n"
				+ "         <td>Day in month\n"
				+ "         <td><a href=\"#number\">Number</a>\n"
				+ "         <td><code>10</code>\n"
				+ "     <tr bgcolor=\"#eeeeff\">\n"
				+ "         <td><code>F</code>\n"
				+ "         <td>Day of week in month\n"
				+ "         <td><a href=\"#number\">Number</a>\n"
				+ "         <td><code>2</code>\n"
				+ "     <tr>\n"
				+ "         <td><code>E</code>\n"
				+ "         <td>Day in week\n"
				+ "         <td><a href=\"#text\">Text</a>\n"
				+ "         <td><code>Tuesday</code>; <code>Tue</code>\n"
				+ "     <tr bgcolor=\"#eeeeff\">\n"
				+ "         <td><code>a</code>\n"
				+ "         <td>Am/pm marker\n"
				+ "         <td><a href=\"#text\">Text</a>\n"
				+ "         <td><code>PM</code>\n"
				+ "     <tr>\n"
				+ "         <td><code>H</code>\n"
				+ "         <td>Hour in day (0-23)\n"
				+ "         <td><a href=\"#number\">Number</a>\n"
				+ "         <td><code>0</code>\n"
				+ "     <tr bgcolor=\"#eeeeff\">\n"
				+ "         <td><code>k</code>\n"
				+ "         <td>Hour in day (1-24)\n"
				+ "         <td><a href=\"#number\">Number</a>\n"
				+ "         <td><code>24</code>\n"
				+ "     <tr>\n"
				+ "         <td><code>K</code>\n"
				+ "         <td>Hour in am/pm (0-11)\n"
				+ "         <td><a href=\"#number\">Number</a>\n"
				+ "         <td><code>0</code>\n"
				+ "     <tr bgcolor=\"#eeeeff\">\n"
				+ "         <td><code>h</code>\n"
				+ "         <td>Hour in am/pm (1-12)\n"
				+ "         <td><a href=\"#number\">Number</a>\n"
				+ "         <td><code>12</code>\n"
				+ "     <tr>\n"
				+ "         <td><code>m</code>\n"
				+ "         <td>Minute in hour\n"
				+ "         <td><a href=\"#number\">Number</a>\n"
				+ "         <td><code>30</code>\n"
				+ "     <tr bgcolor=\"#eeeeff\">\n"
				+ "         <td><code>s</code>\n"
				+ "         <td>Second in minute\n"
				+ "         <td><a href=\"#number\">Number</a>\n"
				+ "         <td><code>55</code>\n"
				+ "     <tr>\n"
				+ "         <td><code>S</code>\n"
				+ "         <td>Millisecond\n"
				+ "         <td><a href=\"#number\">Number</a>\n"
				+ "         <td><code>978</code>\n"
				+ "     <tr bgcolor=\"#eeeeff\">\n"
				+ "         <td><code>z</code>\n"
				+ "         <td>Time zone\n"
				+ "         <td><a href=\"#timezone\">General time zone</a>\n"
				+ "         <td><code>Pacific Standard Time</code>; <code>PST</code>; <code>GMT-08:00</code>\n"
				+ "     <tr>\n"
				+ "         <td><code>Z</code>\n"
				+ "         <td>Time zone\n"
				+ "         <td><a href=\"#rfc822timezone\">RFC 822 time zone</a>\n"
				+ "         <td><code>-0800</code>\n" + " </table>\n" + " </blockquote>\n" + "");
		titleLayout.addComponent(info);
		titleLayout.setComponentAlignment(info, Alignment.MIDDLE_CENTER);

		final DateConversion dateConversion = VaadinSession.getCurrent().getAttribute(DateConversion.class);

		OptionGroup option = new OptionGroup("Display options");
		option.addItem(false);
		option.setItemCaption(false, "Show time in UTC/GMT");
		option.addItem(true);
		option.setItemCaption(true, "Adjust to local timezone (" + dateConversion.getClientTZname() + ")");

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

		option.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(final ValueChangeEvent event) {
				boolean value = (Boolean) event.getProperty().getValue();
				dateConversion.setAdjust(value);
				userObject.setProperty(UserObject.PROPERTY_TIME_ADJUST, String.valueOf(value));
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
		timeFormat.addValidator(new TimeFormatValidator());

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
