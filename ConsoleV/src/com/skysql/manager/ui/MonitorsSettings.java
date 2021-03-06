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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import com.skysql.manager.ClusterComponent;
import com.skysql.manager.ManagerUI;
import com.skysql.manager.MonitorRecord;
import com.skysql.manager.UserChart;
import com.skysql.manager.api.ChartProperties;
import com.skysql.manager.api.Monitors;
import com.skysql.manager.api.Monitors.EditableMonitorType;
import com.skysql.manager.api.NodeInfo;
import com.skysql.manager.api.RunSQL;
import com.skysql.manager.api.SettingsValues;
import com.skysql.manager.api.SystemInfo;
import com.skysql.manager.validators.MonitorNameValidator;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.EmptyValueException;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
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

/**
 * The Class MonitorsSettings is used for the Monitors panel of the Settings dialog.
 */
@SuppressWarnings("deprecation")
public class MonitorsSettings extends VerticalLayout implements Window.CloseListener {

	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private Window secondaryDialog;
	private SettingsDialog settingsDialog;
	private String systemID, nodeID, systemType;
	private boolean validateSQL;
	private Button editMonitor, deleteMonitor;
	private ListSelect select;
	private LinkedHashMap<String, MonitorRecord> monitorsAll;
	private FormLayout monitorLayout;
	private Label id = new Label(), name = new Label(), description = new Label(), unit = new Label(), delta = new Label(), average = new Label(),
			chartType = new Label(), interval = new Label(), sql = new Label();

	/**
	 * Instantiates a new monitors settings.
	 *
	 * @param settingsDialog the settings dialog
	 * @param systemID the system id
	 * @param systemType the system type
	 */
	MonitorsSettings(SettingsDialog settingsDialog, String systemID, String systemType) {
		this.settingsDialog = settingsDialog;
		this.systemID = systemID;
		this.systemType = systemType;

		addStyleName("monitorsTab");
		setSizeFull();
		setSpacing(true);
		setMargin(true);

		HorizontalLayout selectLayout = new HorizontalLayout();
		addComponent(selectLayout);
		selectLayout.setSizeFull();
		selectLayout.setSpacing(true);

		Monitors.reloadMonitors();
		monitorsAll = Monitors.getMonitorsList(systemType);

		select = new ListSelect("Monitors");
		select.setImmediate(true);
		for (MonitorRecord monitor : monitorsAll.values()) {
			String id = monitor.getID();
			select.addItem(id);
			select.setItemCaption(id, monitor.getName());
		}
		select.setNullSelectionAllowed(false);
		selectLayout.addComponent(select);
		select.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {
				String monitorID = (String) event.getProperty().getValue();
				displayMonitorRecord(monitorID);
				MonitorRecord monitor = monitorsAll.get(monitorID);
				if (monitor != null) {
					String monitorType = monitor.getType();

					for (Monitors.EditableMonitorType editable : EditableMonitorType.values()) {
						if (editable.name().equals(monitorType)) {
							editMonitor.setEnabled(true);
							break;
						}
					}
				}
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
					MonitorRecord monitor = monitorsAll.get(monitorID);
					String monitorType = monitor.getType();
					for (Monitors.EditableMonitorType editable : EditableMonitorType.values()) {
						if (editable.name().equals(monitorType)) {
							editMonitor(monitor);
							break;
						}
					}
				}
			}
		});

		monitorLayout = new FormLayout();
		selectLayout.addComponent(monitorLayout);
		selectLayout.setExpandRatio(monitorLayout, 1.0f);
		monitorLayout.setSpacing(false);

		id.setCaption("ID:");
		monitorLayout.addComponent(id);
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
		sql.setCaption("Statement:");
		monitorLayout.addComponent(sql);

		HorizontalLayout selectButtons = new HorizontalLayout();
		selectButtons.setSizeFull();
		addComponent(selectButtons);
		selectButtons.setSpacing(true);

		Button addMonitor = new Button("Add...");
		selectButtons.addComponent(addMonitor);
		selectButtons.setComponentAlignment(addMonitor, Alignment.MIDDLE_LEFT);
		addMonitor.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				addMonitor();
			}
		});

		deleteMonitor = new Button("Delete");
		deleteMonitor.setEnabled(false);
		selectButtons.addComponent(deleteMonitor);
		selectButtons.setComponentAlignment(deleteMonitor, Alignment.MIDDLE_LEFT);
		deleteMonitor.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				String monitorID = (String) select.getValue();
				if (monitorID != null) {
					deleteMonitor(monitorsAll.get(monitorID));
				}
			}
		});

		editMonitor = new Button("Edit...");
		editMonitor.setEnabled(false);
		selectButtons.addComponent(editMonitor);
		selectButtons.setComponentAlignment(editMonitor, Alignment.MIDDLE_CENTER);
		selectButtons.setExpandRatio(editMonitor, 1.0f);
		editMonitor.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				String monitorID = (String) select.getValue();
				if (monitorID != null) {
					editMonitor(monitorsAll.get(monitorID));
				}
			}
		});

	}

	/* (non-Javadoc)
	 * @see com.vaadin.ui.Window.CloseListener#windowClose(com.vaadin.ui.Window.CloseEvent)
	 */
	public void windowClose(CloseEvent e) {

	}

	/**
	 * Display monitor record.
	 *
	 * @param monitorId the monitor id
	 */
	private void displayMonitorRecord(String monitorId) {

		if (monitorId != null) {
			MonitorRecord monitor = monitorsAll.get(monitorId);

			id.setValue(monitorId);
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
			editMonitor.setEnabled(true);
		} else {
			id.setValue(null);
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
			editMonitor.setEnabled(false);
		}

	}

	/** The monitor. */
	private MonitorRecord monitor;

	/**
	 * Delete monitor.
	 *
	 * @param monitor the monitor
	 */
	public void deleteMonitor(final MonitorRecord monitor) {

		this.monitor = monitor;

		ChartProperties chartProperties = getSession().getAttribute(ChartProperties.class);
		List<String> chartNames = chartProperties.findChartsforMonitor(monitor.getSystemType(), monitor.getID());

		String msg = "Deleting this monitor will permanently remove it from the system"
				+ (chartNames.isEmpty() ? "." : " and the following charts: " + chartNames.toString())
				+ ". Data collected in the past will be unavailable from the API but will still be retained in its internal database.";

		secondaryDialog = new WarningWindow("Delete: " + monitor.getName(), msg, "Delete", deleteListener);
		UI.getCurrent().addWindow(secondaryDialog);

	}

	/** The delete listener. */
	private Button.ClickListener deleteListener = new Button.ClickListener() {
		private static final long serialVersionUID = 0x4C656F6E6172646FL;

		public void buttonClick(ClickEvent event) {
			if (Monitors.deleteMonitor(monitor)) {
				select.removeItem(monitor.getID());
				monitorsAll = Monitors.getMonitorsList(systemType);
				ChartProperties chartProperties = getSession().getAttribute(ChartProperties.class);
				chartProperties.setDirty(true);
				settingsDialog.setRefresh(true);
				secondaryDialog.close();
			}

		}
	};

	/**
	 * Edits the monitor.
	 *
	 * @param monitor the monitor
	 */
	public void editMonitor(MonitorRecord monitor) {
		monitorForm(monitor, "Edit Monitor: " + monitor.getName(), "Edit SQL Monitor for Nodes and System", "Save Changes");
	}

	/**
	 * Adds the monitor.
	 */
	public void addMonitor() {
		MonitorRecord monitor = new MonitorRecord(systemType);
		monitorForm(monitor, "Add Monitor", "Add a new SQL Monitor for Nodes and System", "Add Monitor");
	}

	/**
	 * Monitor form.
	 *
	 * @param monitor the monitor
	 * @param title the title
	 * @param description the description
	 * @param button the button
	 */
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

		secondaryDialog = new ModalWindow(title, null);
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
		monitorName.setImmediate(true);
		monitorName.addValidator(new MonitorNameValidator(monitor.getName()));

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
		OverviewPanel overviewPanel = getSession().getAttribute(OverviewPanel.class);
		ArrayList<NodeInfo> nodes = overviewPanel.getNodes();

		if (nodes == null || nodes.isEmpty()) {
			SystemInfo systemInfo = VaadinSession.getCurrent().getAttribute(SystemInfo.class);
			String systemID = systemInfo.getCurrentID();
			String systemType = systemInfo.getCurrentSystem().getSystemType();
			if (systemID.equals(SystemInfo.SYSTEM_ROOT)) {
				ClusterComponent clusterComponent = VaadinSession.getCurrent().getAttribute(ClusterComponent.class);
				systemID = clusterComponent.getID();
				systemType = clusterComponent.getSystemType();
			}
			nodes = new ArrayList<NodeInfo>();
			for (String nodeID : systemInfo.getSystemRecord(systemID).getNodes()) {
				NodeInfo nodeInfo = new NodeInfo(systemID, systemType, nodeID);
				nodes.add(nodeInfo);
			}

		}

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

		Collection<?> validIntervals = monitorInterval.getItemIds();
		if (validIntervals.contains(monitor.getInterval())) {
			monitorInterval.select(monitor.getInterval());
		} else {
			SystemInfo systemInfo = getSession().getAttribute(SystemInfo.class);
			String defaultInterval = systemInfo.getSystemRecord(systemID).getProperties().get(SystemInfo.PROPERTY_DEFAULTMONITORINTERVAL);
			if (defaultInterval != null && validIntervals.contains(Integer.parseInt(defaultInterval))) {
				monitorInterval.select(Integer.parseInt(defaultInterval));
			} else if (!validIntervals.isEmpty()) {
				monitorInterval.select(validIntervals.toArray()[0]);
			} else {
				new ErrorDialog(null, "No set of permissible monitor intervals found");
			}

			monitorInterval.setNullSelectionAllowed(false);
			form.addField("monitorInterval", monitorInterval);
		}

		for (UserChart.ChartType type : UserChart.ChartType.values()) {
			monitorChartType.addItem(type.name());
		}
		monitorChartType.select(monitor.getChartType() == null ? UserChart.ChartType.values()[0] : monitor.getChartType());
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
					String ID;
					if ((ID = monitor.getID()) == null) {
						if (Monitors.setMonitor(monitor)) {
							ID = monitor.getID();
							select.addItem(ID);
							select.select(ID);
							Monitors.reloadMonitors();
							monitorsAll = Monitors.getMonitorsList(systemType);
						}
					} else {
						Monitors.setMonitor(monitor);
						ChartProperties chartProperties = getSession().getAttribute(ChartProperties.class);
						chartProperties.setDirty(true);
						settingsDialog.setRefresh(true);
					}

					if (ID != null) {
						select.setItemCaption(ID, monitor.getName());
						displayMonitorRecord(ID);
						secondaryDialog.close();
					}

				} catch (EmptyValueException e) {
					return;
				} catch (InvalidValueException e) {
					return;
				} catch (Exception e) {
					ManagerUI.error(e.getMessage());
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

	/**
	 * The Class SQLValidator.
	 */
	class SQLValidator implements Validator {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 0x4C656F6E6172646FL;

		/** The error. */
		private String error;

		/**
		 * Checks if is valid.
		 *
		 * @param value the value
		 * @return true, if is valid
		 */
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
		/* (non-Javadoc)
		 * @see com.vaadin.data.Validator#validate(java.lang.Object)
		 */
		public void validate(Object value) throws InvalidValueException {
			if (!isValid(value)) {
				throw new InvalidValueException(error);
			} else {

			}
		}
	}

}
