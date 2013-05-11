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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

import com.skysql.consolev.MonitorRecord;
import com.skysql.consolev.api.Monitors;
import com.skysql.consolev.api.NodeInfo;
import com.skysql.consolev.api.RunSQL;
import com.skysql.consolev.api.SettingsValues;
import com.skysql.consolev.api.SystemInfo;
import com.skysql.consolev.api.UserChart;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.EmptyValueException;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Form;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;

@SuppressWarnings("deprecation")
public class MonitorsSettings implements Window.CloseListener {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private Window secondaryDialog;
	private String systemID, nodeID;
	private boolean validateSQL;
	private Button deleteMonitor;
	private ListSelect select;
	private LinkedHashMap<String, MonitorRecord> monitorsAll;
	private FormLayout monitorLayout;
	private Label name = new Label(), description = new Label(), unit = new Label(), delta = new Label(), average = new Label(), chartType = new Label(),
			interval = new Label(), sql = new Label();

	MonitorsSettings(final HorizontalLayout monitorTab) {

		monitorTab.addStyleName("monitorsTab");
		monitorTab.setSpacing(true);

		VerticalLayout selectLayout = new VerticalLayout();
		monitorTab.addComponent(selectLayout);
		selectLayout.setWidth("200px");
		selectLayout.setSpacing(true);
		selectLayout.setMargin(true);

		SystemInfo systemInfo = VaadinSession.getCurrent().getAttribute(SystemInfo.class);
		systemID = systemInfo.getID();

		Monitors.reloadMonitors();
		monitorsAll = Monitors.getMonitorsList();

		select = new ListSelect("Monitors");
		select.setImmediate(true);
		for (MonitorRecord monitor : monitorsAll.values()) {
			String id = monitor.getID();
			select.addItem(id);
			select.setItemCaption(id, monitor.getName());
		}
		select.setNullSelectionAllowed(false);
		select.setWidth("12em");
		selectLayout.addComponent(select);
		select.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {
				String id = (String) event.getProperty().getValue();
				displayMonitorRecord(id);
			}
		});

		selectLayout.addLayoutClickListener(new LayoutClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void layoutClick(LayoutClickEvent event) {

				Component child;
				if (event.isDoubleClick() && (child = event.getChildComponent()) != null && (child instanceof ListSelect)) {
					// Get the child component which was double-clicked
					ListSelect select = (ListSelect) child;
					String monitorID = (String) select.getValue();
					if (monitorID != null) {
						editMonitor(monitorsAll.get(monitorID));
					}
				}
			}
		});

		HorizontalLayout selectButtons = new HorizontalLayout();
		selectLayout.addComponent(selectButtons);
		selectButtons.setSpacing(true);

		Button addMonitor = new Button("New...");
		selectButtons.addComponent(addMonitor);
		addMonitor.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				addMonitor();
			}
		});

		deleteMonitor = new Button("Delete");
		deleteMonitor.setEnabled(false);
		selectButtons.addComponent(deleteMonitor);
		deleteMonitor.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				String monitorID = (String) select.getValue();
				if (monitorID != null) {
					deleteMonitor(monitorsAll.get(monitorID));
				}
			}
		});

		monitorLayout = new FormLayout();
		monitorTab.addComponent(monitorLayout);
		monitorTab.setExpandRatio(monitorLayout, 1.0f);
		monitorLayout.setSpacing(false);

		name.setCaption("Name:");
		monitorLayout.addComponent(name);
		description.setCaption("Description:");
		monitorLayout.addComponent(description);
		unit.setCaption("Unit:");
		monitorLayout.addComponent(unit);
		// type.setCaption("Type:");
		// monitorLayout.addComponent(type);
		delta.setCaption("Is Delta:");
		monitorLayout.addComponent(delta);
		average.setCaption("Is Average:");
		monitorLayout.addComponent(average);
		chartType.setCaption("Chart Type:");
		monitorLayout.addComponent(chartType);
		interval.setCaption("Interval:");
		monitorLayout.addComponent(interval);
		sql.setCaption("SQL:");
		monitorLayout.addComponent(sql);

	}

	public void windowClose(CloseEvent e) {

	}

	private void displayMonitorRecord(String id) {

		if (id != null) {
			MonitorRecord monitor = monitorsAll.get(id);
			name.setValue(monitor.getName());
			description.setValue(monitor.getDescription());
			unit.setValue(monitor.getUnit());
			// type.setValue(monitor.getType());
			sql.setValue(monitor.getSql());
			delta.setValue(String.valueOf(monitor.isDelta()));
			average.setValue(String.valueOf(monitor.isAverage()));
			interval.setValue(String.valueOf(monitor.getInterval()));
			chartType.setValue(monitor.getChartType());
			deleteMonitor.setEnabled(true);
		} else {
			name.setValue(null);
			description.setValue(null);
			unit.setValue(null);
			// type.setValue(monitor.getType());
			sql.setValue(null);
			delta.setValue(null);
			average.setValue(null);
			interval.setValue(null);
			chartType.setValue(null);
			deleteMonitor.setEnabled(false);
		}

	}

	public void deleteMonitor(final MonitorRecord monitor) {
		secondaryDialog = new DialogWindow("Delete Monitor: " + monitor.getName());
		UI.getCurrent().addWindow(secondaryDialog);
		secondaryDialog.addCloseListener(this);

		HorizontalLayout wrapper = new HorizontalLayout();
		wrapper.setWidth("100%");
		wrapper.setMargin(true);
		VerticalLayout iconLayout = new VerticalLayout();
		iconLayout.setWidth("100px");
		wrapper.addComponent(iconLayout);
		Embedded image = new Embedded(null, new ThemeResource("img/warning.png"));
		iconLayout.addComponent(image);
		VerticalLayout textLayout = new VerticalLayout();
		textLayout.setSizeFull();
		wrapper.addComponent(textLayout);
		wrapper.setExpandRatio(textLayout, 1.0f);
		Label label = new Label("WARNING: if you delete this monitor, all its related data will be deleted as well.");
		label.addStyleName("warning");
		textLayout.addComponent(label);
		textLayout.setComponentAlignment(label, Alignment.MIDDLE_CENTER);

		HorizontalLayout buttonsBar = new HorizontalLayout();
		buttonsBar.setStyleName("buttonsBar");
		buttonsBar.setSizeFull();
		buttonsBar.setSpacing(true);
		buttonsBar.setMargin(true);
		buttonsBar.setHeight("49px");

		Label filler = new Label();
		buttonsBar.addComponent(filler);
		buttonsBar.setExpandRatio(filler, 1.0f);

		Button cancelButton = new Button("Cancel");
		buttonsBar.addComponent(cancelButton);
		buttonsBar.setComponentAlignment(cancelButton, Alignment.MIDDLE_RIGHT);

		cancelButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				secondaryDialog.close();
			}
		});

		Button okButton = new Button("Delete Monitor");
		okButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				try {
					if (Monitors.deleteMonitor(monitor)) {
						select.removeItem(monitor.getID());
						monitorsAll = Monitors.getMonitorsList();
						displayMonitorRecord(null);
						secondaryDialog.close();
					}

				} catch (Exception e) {
					e.printStackTrace();
					return;
				}

			}
		});
		buttonsBar.addComponent(okButton);
		buttonsBar.setComponentAlignment(okButton, Alignment.MIDDLE_RIGHT);

		VerticalLayout windowLayout = (VerticalLayout) secondaryDialog.getContent();
		windowLayout.setSpacing(false);
		windowLayout.setMargin(false);
		windowLayout.addComponent(wrapper);
		windowLayout.addComponent(buttonsBar);

	}

	public void editMonitor(MonitorRecord monitor) {
		monitorForm(monitor, "Edit Monitor: " + monitor.getName(), "Edit SQL Monitor for Nodes and System", "Save Changes");
	}

	public void addMonitor() {
		MonitorRecord monitor = new MonitorRecord();
		monitorForm(monitor, "Create New Monitor", "Create a new SQL Monitor for Nodes and System", "Create Monitor");
	}

	public void monitorForm(final MonitorRecord monitor, String title, String description, String button) {
		final TextField monitorName = new TextField("Monitor Name");
		final TextField monitorDescription = new TextField("Description");
		final TextField monitorUnit = new TextField("Measurement Unit");
		final TextArea monitorSQL = new TextArea("SQL Statement");
		final CheckBox monitorDelta = new CheckBox("Is Delta");
		final CheckBox monitorAverage = new CheckBox("Is Average");
		final NativeSelect validationTarget = new NativeSelect("Validate SQL on");
		final NativeSelect monitorInterval = new NativeSelect("Sampling interval");
		final NativeSelect monitorChartType = new NativeSelect("Default display");

		secondaryDialog = new DialogWindow(title);
		UI.getCurrent().addWindow(secondaryDialog);
		secondaryDialog.addCloseListener(this);

		final VerticalLayout formContainer = new VerticalLayout();
		formContainer.setMargin(new MarginInfo(true, true, false, true));
		formContainer.setSpacing(false);

		final Form form = new Form();
		formContainer.addComponent(form);
		form.setImmediate(false);
		form.setFooter(null);
		form.setDescription(description);

		String value;
		if ((value = monitor.getName()) != null) {
			monitorName.setValue(value);
		}
		form.addField("monitorName", monitorName);
		form.getField("monitorName").setRequired(true);
		form.getField("monitorName").setRequiredError("Monitor Name is missing");
		monitorName.focus();

		if ((value = monitor.getDescription()) != null) {
			monitorDescription.setValue(value);
		}
		monitorDescription.setWidth("24em");
		form.addField("monitorDescription", monitorDescription);

		if ((value = monitor.getUnit()) != null) {
			monitorUnit.setValue(value);
		}
		form.addField("monitorUnit", monitorUnit);

		if ((value = monitor.getSql()) != null) {
			monitorSQL.setValue(value);
		}
		monitorSQL.setWidth("24em");
		monitorSQL.addValidator(new SQLValidator());
		form.addField("monitorSQL", monitorSQL);

		final String noValidation = "None - Skip Validation";
		validationTarget.setImmediate(true);
		validationTarget.setNullSelectionAllowed(false);
		validationTarget.addItem(noValidation);
		validationTarget.select(noValidation);
		OverviewPanel overviewPanel = VaadinSession.getCurrent().getAttribute(OverviewPanel.class);
		ArrayList<NodeInfo> nodes = overviewPanel.getNodes();
		for (NodeInfo node : nodes) {
			validationTarget.addItem(node.getID());
			validationTarget.setItemCaption(node.getID(), node.getName());
		}
		validationTarget.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {
				nodeID = (String) event.getProperty().getValue();
				validateSQL = nodeID.equals(noValidation) ? false : true;
			}

		});
		form.addField("validationTarget", validationTarget);

		monitorDelta.setValue(monitor.isDelta());
		form.addField("monitorDelta", monitorDelta);

		monitorAverage.setValue(monitor.isAverage());
		form.addField("monitorAverage", monitorAverage);

		SettingsValues intervalValues = new SettingsValues(SettingsValues.SETTINGS_MONITOR_INTERVAL);
		String[] intervals = intervalValues.getValues();
		for (String interval : intervals) {
			monitorInterval.addItem(Integer.parseInt(interval));
		}

		Collection validIntervals = monitorInterval.getItemIds();
		if (validIntervals.contains(monitor.getInterval())) {
			monitorInterval.select(monitor.getInterval());
		} else {
			SystemInfo systemInfo = VaadinSession.getCurrent().getAttribute(SystemInfo.class);
			String defaultInterval = systemInfo.getProperties().get(SystemInfo.PROPERTY_DEFAULTMONITORINTERVAL);
			if (defaultInterval != null && validIntervals.contains(Integer.parseInt(defaultInterval))) {
				monitorInterval.select(Integer.parseInt(defaultInterval));
			} else if (!validIntervals.isEmpty()) {
				monitorInterval.select(validIntervals.toArray()[0]);
			} else {
				throw new RuntimeException("No set of permissible monitor intervals found");
			}

			monitorInterval.setNullSelectionAllowed(false);
			form.addField("monitorInterval", monitorInterval);
		}

		for (String chartType : UserChart.chartTypes()) {
			monitorChartType.addItem(chartType);
		}
		monitorChartType.select(monitor.getChartType() == null ? UserChart.chartTypes()[0] : monitor.getChartType());
		monitorChartType.setNullSelectionAllowed(false);
		form.addField("monitorChartType", monitorChartType);

		HorizontalLayout buttonsBar = new HorizontalLayout();
		buttonsBar.setStyleName("buttonsBar");
		buttonsBar.setSizeFull();
		buttonsBar.setSpacing(true);
		buttonsBar.setMargin(true);
		buttonsBar.setHeight("49px");

		Label filler = new Label();
		buttonsBar.addComponent(filler);
		buttonsBar.setExpandRatio(filler, 1.0f);

		Button cancelButton = new Button("Cancel");
		buttonsBar.addComponent(cancelButton);
		buttonsBar.setComponentAlignment(cancelButton, Alignment.MIDDLE_RIGHT);

		cancelButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				form.discard();
				secondaryDialog.close();
			}
		});

		Button okButton = new Button(button);
		okButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				String monitorID = null;
				try {
					form.setComponentError(null);
					form.commit();
					monitor.setName(monitorName.getValue());
					monitor.setDescription(monitorDescription.getValue());
					monitor.setUnit(monitorUnit.getValue());
					monitor.setSql(monitorSQL.getValue());
					monitor.setDelta(monitorDelta.getValue());
					monitor.setAverage(monitorAverage.getValue());
					monitor.setInterval((Integer) monitorInterval.getValue());
					monitor.setChartType((String) monitorChartType.getValue());
					if ((monitorID = monitor.getID()) == null) {
						monitorID = Monitors.setMonitor(monitor);
						if (monitorID != null) {
							select.addItem(monitorID);
							select.select(monitorID);
							monitorsAll = Monitors.getMonitorsList();
						}
					} else {
						Monitors.setMonitor(monitor);
					}

					if (monitorID != null) {
						select.setItemCaption(monitorID, monitor.getName());
						displayMonitorRecord(monitorID);
						secondaryDialog.close();
					}

				} catch (EmptyValueException e) {
					return;
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}

			}
		});
		buttonsBar.addComponent(okButton);
		buttonsBar.setComponentAlignment(okButton, Alignment.MIDDLE_RIGHT);

		VerticalLayout windowLayout = (VerticalLayout) secondaryDialog.getContent();
		windowLayout.setSpacing(false);
		windowLayout.setMargin(false);
		windowLayout.addComponent(formContainer);
		windowLayout.addComponent(buttonsBar);

	}

	class SQLValidator implements Validator {
		private static final long serialVersionUID = 0x4C656F6E6172646FL;
		private String error;

		public boolean isValid(Object value) {
			// ignore an empty field
			if (!validateSQL || value == null || (value != null && value.toString().isEmpty())) {
				return true;
			}

			RunSQL runSQL = new RunSQL((String) value, systemID, nodeID);
			error = runSQL.getErrors();

			return (runSQL.getSuccess());
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
