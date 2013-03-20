package com.skysql.consolev.ui;

import java.util.ArrayList;

import com.skysql.consolev.MonitorRecord;
import com.skysql.consolev.SessionData;
import com.skysql.consolev.api.AppData;
import com.skysql.consolev.api.Commands;
import com.skysql.consolev.api.Monitors;
import com.skysql.consolev.api.NodeInfo;
import com.skysql.consolev.api.NodeStates;
import com.skysql.consolev.api.SystemInfo;
import com.skysql.consolev.ui.components.ChartControls;
import com.skysql.consolev.ui.components.ChartsLayout;
import com.vaadin.addon.charts.Chart;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;

import fi.jasoft.dragdroplayouts.DDCssLayout;
import fi.jasoft.dragdroplayouts.client.ui.LayoutDragMode;

public class PanelNodeInfo extends HorizontalLayout {

	private static final String NOT_AVAILABLE = "n/a";
	private static final String PROPERTY_MONITORS = "MONITORS";
	private static final String SYSTEM_NODEID = "0";

	private NodeInfo nodeInfo, lastNodeInfo;
	private Component systemGrid, nodeGrid;
	private Label systemValues[], nodeValues[];
	private VerticalLayout infoLayout, chartsLayout;
	private DDCssLayout chartsArray;
	private String chartTime, chartInterval = "1800", chartPoints = "15";
	private ArrayList<MonitorRecord> monitorsList;
	private String nodeLabels[] = { "Status", "Availability", "Connections", "Data Transfer", "Commands Running", "Public IP", "Private IP", "Instance ID", };
	private String systemLabels[] = { "Status", "Availability", "Connections", "Data Transfer", "Last Backup", "Start Date", "Last Access" };
	private ChartControls chartControls;
	private ChartsLayout chartsArrayLayout;
	private Label nameLabel;
	private TextField nameField;

	private ValueChangeListener chartIntervalListener = new ValueChangeListener() {
		private static final long serialVersionUID = 0x4C656F6E6172646FL;

		public void valueChange(ValueChangeEvent event) {

			Integer value = (Integer) (event.getProperty()).getValue();
			// value = value / Integer.parseInt(chartPoints);
			chartInterval = Integer.toString(value);
			refresh(nodeInfo);
		}
	};

	PanelNodeInfo() {
		// AppData.setPanelNodeInfo(this);
		setSizeFull();
		addStyleName("infoTab");

		createInfoLayout();
		createChartsLayout();

	}

	private void createInfoLayout() {

		infoLayout = new VerticalLayout();
		infoLayout.addStyleName("infoLayout");
		infoLayout.setWidth("300px");
		infoLayout.setSpacing(true);
		infoLayout.setMargin(true);
		addComponent(infoLayout);

		final HorizontalLayout nameLayout = new HorizontalLayout();
		nameLayout.addStyleName("panelHeaderLayout");
		nameLayout.setWidth("100%");
		nameLayout.setSpacing(true);
		nameLayout.setMargin(true);
		infoLayout.addComponent(nameLayout);

		nameLabel = new Label();
		nameLabel.addStyleName("nameLabel");
		nameLabel.setSizeUndefined();
		nameLayout.addComponent(nameLabel);
		nameLayout.setComponentAlignment(nameLabel, Alignment.MIDDLE_LEFT);

		final Button editButton = new Button("Edit");
		final Button saveButton = new Button("Done");

		nameLayout.addComponent(editButton);
		nameLayout.setComponentAlignment(editButton, Alignment.MIDDLE_RIGHT);
		editButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				nameField = new TextField();
				nameField.setValue(nameLabel.getValue());
				nameField.setWidth("12em");
				nameField.focus();
				nameLayout.replaceComponent(nameLabel, nameField);
				nameLayout.setComponentAlignment(nameField, Alignment.MIDDLE_LEFT);
				nameLayout.replaceComponent(editButton, saveButton);
				nameLayout.setComponentAlignment(saveButton, Alignment.MIDDLE_RIGHT);
			}
		});

		saveButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				String name = nameField.getValue();
				nameLabel.setValue(name);
				nameLayout.replaceComponent(nameField, nameLabel);
				nameLayout.setComponentAlignment(nameLabel, Alignment.MIDDLE_LEFT);
				nameLayout.replaceComponent(saveButton, editButton);
				nameLayout.setComponentAlignment(editButton, Alignment.MIDDLE_RIGHT);
				lastNodeInfo.setName(name);
				VerticalLayout button = (VerticalLayout) lastNodeInfo.getButton();
				Label label = (Label) button.getComponent(0);
				label.setValue(name);
			}
		});

		systemValues = new Label[systemLabels.length];
		systemGrid = createCurrentInfo(systemLabels, systemValues);
		nodeValues = new Label[nodeLabels.length];
		nodeGrid = createCurrentInfo(nodeLabels, nodeValues);
		infoLayout.addComponent(systemGrid);
	}

	private Component createCurrentInfo(String[] currentLabels, Label[] currentValues) {
		GridLayout currentGrid = new GridLayout(2, currentLabels.length);
		currentGrid.addStyleName("currentInfo");
		currentGrid.setSpacing(true);
		currentGrid.setSizeUndefined();

		for (int i = 0; i < currentLabels.length; i++) {
			Label label = new Label(currentLabels[i]);
			label.setSizeUndefined();
			currentGrid.addComponent(label, 0, i);
			currentValues[i] = new Label("");
			currentValues[i].setSizeUndefined();
			currentGrid.addComponent(currentValues[i], 1, i);
		}

		return (currentGrid);
	}

	private void createChartsLayout() {
		chartsLayout = new VerticalLayout();
		chartsLayout.addStyleName("chartsLayout");
		chartsLayout.setHeight("100%");
		chartsLayout.setSpacing(true);
		chartsLayout.setMargin(true);
		addComponent(chartsLayout);
		setExpandRatio(chartsLayout, 1.0f);

		final HorizontalLayout chartsHeaderLayout = new HorizontalLayout();
		chartsHeaderLayout.addStyleName("panelHeaderLayout");
		chartsHeaderLayout.setWidth("100%");
		chartsHeaderLayout.setSpacing(true);
		chartsHeaderLayout.setMargin(true);
		chartsLayout.addComponent(chartsHeaderLayout);

		chartControls = new ChartControls();
		chartControls.addIntervalSelectionListener(chartIntervalListener);
		chartsHeaderLayout.addComponent(chartControls);
		chartsHeaderLayout.setComponentAlignment(chartControls, Alignment.MIDDLE_LEFT);

		final HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		chartsHeaderLayout.addComponent(buttonsLayout);
		chartsHeaderLayout.setComponentAlignment(buttonsLayout, Alignment.MIDDLE_RIGHT);

		SettingsDialog settingsDialog = new SettingsDialog("Edit Monitors...", "Monitors");
		final Button editMonitorsButton = settingsDialog.getButton();
		editMonitorsButton.setVisible(false);
		buttonsLayout.addComponent(editMonitorsButton);

		final Button addChartButton = new Button("Add Chart...");
		addChartButton.setVisible(false);
		buttonsLayout.addComponent(addChartButton);
		addChartButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				Chart chart = (Chart) chartsArrayLayout.addChart(monitorsList.get(0));
				new ChartsDialog(chartsArrayLayout, chart);
			}
		});

		final Button editButton = new Button("Edit");
		final Button saveButton = new Button("Done");
		buttonsLayout.addComponent(editButton);
		editButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				buttonsLayout.replaceComponent(editButton, saveButton);
				chartsArrayLayout.setDragMode(LayoutDragMode.CLONE);
				chartsArrayLayout.disableCharts();
				editMonitorsButton.setVisible(true);
				addChartButton.setVisible(true);
			}
		});

		saveButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				buttonsLayout.replaceComponent(saveButton, editButton);
				chartsArrayLayout.setDragMode(LayoutDragMode.NONE);
				chartsArrayLayout.enableCharts();
				editMonitorsButton.setVisible(false);
				addChartButton.setVisible(false);
			}
		});

		Panel panel = new Panel();
		panel.setSizeFull();
		panel.addStyleName(Runo.PANEL_LIGHT);
		chartsLayout.addComponent(panel);
		chartsLayout.setExpandRatio(panel, 1.0f);

		chartsArray = chartsArray();
		panel.setContent(chartsArray);

	}

	public DDCssLayout chartsArray() {

		// Setup array of charts objects
		SessionData sessionData = VaadinSession.getCurrent().getAttribute(SessionData.class);
		String propertyMonitors = sessionData.getUserLogin().getProperty(PROPERTY_MONITORS);

		Monitors monitors = new Monitors(null);
		monitorsList = monitors.getMonitorsList(propertyMonitors);

		chartsArrayLayout = new ChartsLayout(false);
		chartsArrayLayout.initializeCharts(monitorsList);

		return chartsArrayLayout;

	}

	public void redrawMonitors() {
		DDCssLayout newChartsArray = chartsArray();
		chartsLayout.replaceComponent(chartsArray, newChartsArray);
		chartsArray = newChartsArray;
		refresh(nodeInfo);
	}

	public void refresh(NodeInfo nodeInfo) {
		this.nodeInfo = nodeInfo;

		// switch out System or Node current info , if necessary
		String newNodeID = nodeInfo.getNodeID();
		String oldNodeID = (lastNodeInfo != null) ? lastNodeInfo.getNodeID() : null;

		if ((oldNodeID != null) && !oldNodeID.equalsIgnoreCase(newNodeID)
				&& (newNodeID.equalsIgnoreCase(SYSTEM_NODEID) || oldNodeID.equalsIgnoreCase(SYSTEM_NODEID))) {
			if (newNodeID.equalsIgnoreCase(SYSTEM_NODEID)) {
				infoLayout.replaceComponent(nodeGrid, systemGrid);
			} else {
				infoLayout.replaceComponent(systemGrid, nodeGrid);
			}
		}

		String value, values[];
		Label[] currentValues;
		if (newNodeID.equalsIgnoreCase(SYSTEM_NODEID)) {
			currentValues = systemValues;
			SystemInfo systemInfo = new SystemInfo(AppData.getGson());
			nameLabel.setValue(systemInfo.getSystemName());
			String localvalues[] = {
					((value = nodeInfo.getStatus()) != null) && (value = NodeStates.getNodeStatesDescriptions().get(value)) != null ? value : "Invalid",
					(value = nodeInfo.getHealth()) != null ? value + "%" : NOT_AVAILABLE, (value = nodeInfo.getConnections()) != null ? value : NOT_AVAILABLE,
					(value = nodeInfo.getPackets()) != null ? value + " KB" : NOT_AVAILABLE,
					(value = systemInfo.getLastBackup()) != null ? value : NOT_AVAILABLE, (value = systemInfo.getStartDate()) != null ? value : NOT_AVAILABLE,
					(value = systemInfo.getLastAccess()) != null ? value : NOT_AVAILABLE };
			values = localvalues;
		} else {
			currentValues = nodeValues;
			nameLabel.setValue(nodeInfo.getName());
			String localvalues[] = {
					((value = nodeInfo.getStatus()) != null) && (value = NodeStates.getNodeStatesDescriptions().get(value)) != null ? value : "Invalid",
					(value = nodeInfo.getHealth()) != null ? value + "%" : NOT_AVAILABLE, (value = nodeInfo.getConnections()) != null ? value : NOT_AVAILABLE,
					(value = nodeInfo.getPackets()) != null ? value + " KB" : NOT_AVAILABLE,
					(value = nodeInfo.getCommand()) != null ? Commands.getNames().get(value) : NOT_AVAILABLE,
					(value = nodeInfo.getPublicIP()) != null ? value : NOT_AVAILABLE, (value = nodeInfo.getPrivateIP()) != null ? value : NOT_AVAILABLE,
					(value = nodeInfo.getInstanceID()) != null ? value : NOT_AVAILABLE };
			values = localvalues;
		}

		for (int i = 0; i < values.length && i < currentValues.length; i++) {
			value = values[i];
			if (!((String) currentValues[i].getValue()).equalsIgnoreCase(value)) {
				currentValues[i].setValue(value);
			}
		}

		chartsArrayLayout.refresh(nodeInfo, chartTime, chartInterval, chartPoints);

		lastNodeInfo = nodeInfo;
	}

}
