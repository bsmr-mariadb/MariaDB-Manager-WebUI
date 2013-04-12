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

import com.skysql.consolev.api.ClusterComponent;
import com.skysql.consolev.api.Commands;
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

public class PanelInfo extends HorizontalLayout {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private static final String NOT_AVAILABLE = "n/a";

	private ClusterComponent lastComponent;
	private Component systemGrid, nodeGrid;
	private VerticalLayout infoLayout, chartsLayout;
	private DDCssLayout chartsArray;
	private String chartTime, chartInterval = "1800", chartPoints = "15";
	private String systemLabelsStrings[] = { "Status", "Availability", "Connections", "Data Transfer", "Last Backup", "Start Date", "Last Access" };
	private String nodeLabelsStrings[] = { "Status", "Availability", "Connections", "Data Transfer", "Commands Running", "Public IP", "Private IP",
			"Instance ID", };
	private Label systemLabels[], nodeLabels[];
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
			refresh();
		}
	};

	PanelInfo() {

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
				nameField.setImmediate(true);
				nameField.setValue(nameLabel.getValue());
				nameField.setWidth("12em");
				nameField.focus();
				nameField.addValueChangeListener(new ValueChangeListener() {
					private static final long serialVersionUID = 0x4C656F6E6172646FL;

					public void valueChange(ValueChangeEvent event) {
						saveButton.click();
					}
				});
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
				lastComponent.setName(name);
				if (lastComponent instanceof NodeInfo) {
					((NodeInfo) lastComponent).saveName(name);
				} else if (lastComponent instanceof SystemInfo) {
					((SystemInfo) lastComponent).saveName(name);
				}
				VerticalLayout button = (VerticalLayout) lastComponent.getButton();
				Label label = (Label) button.getComponent(0);
				label.setValue(name);
			}
		});

		systemLabels = new Label[systemLabelsStrings.length];
		systemGrid = createCurrentInfo(systemLabels, systemLabelsStrings);
		nodeLabels = new Label[nodeLabelsStrings.length];
		nodeGrid = createCurrentInfo(nodeLabels, nodeLabelsStrings);
		infoLayout.addComponent(systemGrid);
	}

	private Component createCurrentInfo(Label[] labels, String[] values) {
		GridLayout currentGrid = new GridLayout(2, labels.length);
		currentGrid.addStyleName("currentInfo");
		currentGrid.setSpacing(true);
		currentGrid.setSizeUndefined();

		for (int i = 0; i < labels.length; i++) {
			Label label = new Label(values[i]);
			label.setSizeUndefined();
			currentGrid.addComponent(label, 0, i);
			labels[i] = new Label("");
			labels[i].setSizeUndefined();
			currentGrid.addComponent(labels[i], 1, i);
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
				Chart chart = (Chart) chartsArrayLayout.addChart();
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

		chartsArrayLayout = new ChartsLayout(false);
		chartsArrayLayout.initializeCharts();

		return chartsArrayLayout;

	}

	public void redrawMonitors() {
		DDCssLayout newChartsArray = chartsArray();
		chartsLayout.replaceComponent(chartsArray, newChartsArray);
		chartsArray = newChartsArray;
		refresh();
	}

	public void refresh() {
		ClusterComponent componentInfo = VaadinSession.getCurrent().getAttribute(ClusterComponent.class);

		if ((lastComponent != null) && (componentInfo != lastComponent)) {
			// switch out System or Node current info, if necessary
			if (componentInfo.getType() != lastComponent.getType()) {
				switch (componentInfo.getType()) {
				case system:
					infoLayout.replaceComponent(nodeGrid, systemGrid);
					break;

				case node:
					infoLayout.replaceComponent(systemGrid, nodeGrid);
					break;
				}
			}
		}

		nameLabel.setValue(componentInfo.getName());

		String value, values[];
		Label[] currentLabels;

		switch (componentInfo.getType()) {
		case system:
			SystemInfo systemInfo = (SystemInfo) componentInfo;
			currentLabels = systemLabels;
			String systemValues[] = {
					((value = systemInfo.getStatus()) != null) && (value = NodeStates.getNodeStatesDescriptions().get(value)) != null ? value : "Invalid",
					(value = systemInfo.getHealth()) != null ? value + "%" : NOT_AVAILABLE,
					(value = systemInfo.getConnections()) != null ? value : NOT_AVAILABLE,
					(value = systemInfo.getPackets()) != null ? value + " KB" : NOT_AVAILABLE,
					(value = systemInfo.getLastBackup()) != null ? value : NOT_AVAILABLE, (value = systemInfo.getStartDate()) != null ? value : NOT_AVAILABLE,
					(value = systemInfo.getLastAccess()) != null ? value : NOT_AVAILABLE };
			values = systemValues;
			break;

		case node:
			NodeInfo nodeInfo = (NodeInfo) componentInfo;
			currentLabels = nodeLabels;
			String nodeValues[] = {
					((value = nodeInfo.getStatus()) != null) && (value = NodeStates.getNodeStatesDescriptions().get(value)) != null ? value : "Invalid",
					(value = nodeInfo.getHealth()) != null ? value + "%" : NOT_AVAILABLE, (value = nodeInfo.getConnections()) != null ? value : NOT_AVAILABLE,
					(value = nodeInfo.getPackets()) != null ? value + " KB" : NOT_AVAILABLE,
					(value = nodeInfo.getCommand()) != null ? Commands.getNames().get(value) : NOT_AVAILABLE,
					(value = nodeInfo.getPublicIP()) != null ? value : NOT_AVAILABLE, (value = nodeInfo.getPrivateIP()) != null ? value : NOT_AVAILABLE,
					(value = nodeInfo.getInstanceID()) != null ? value : NOT_AVAILABLE };
			values = nodeValues;
			break;

		default:
			return;
		}

		for (int i = 0; i < values.length && i < currentLabels.length; i++) {
			value = values[i];
			if (!((String) currentLabels[i].getValue()).equalsIgnoreCase(value)) {
				currentLabels[i].setValue(value);
			}
		}

		chartsArrayLayout.refresh(chartTime, chartInterval, chartPoints);

		lastComponent = componentInfo;
	}
}
