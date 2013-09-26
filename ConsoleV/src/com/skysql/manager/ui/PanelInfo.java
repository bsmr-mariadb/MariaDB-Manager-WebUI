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

package com.skysql.manager.ui;

import java.util.LinkedHashMap;

import org.vaadin.jouni.animator.AnimatorProxy;
import org.vaadin.jouni.animator.AnimatorProxy.AnimationEvent;
import org.vaadin.jouni.animator.AnimatorProxy.AnimationListener;
import org.vaadin.jouni.animator.shared.AnimType;

import com.skysql.manager.ClusterComponent;
import com.skysql.manager.DateConversion;
import com.skysql.manager.SystemRecord;
import com.skysql.manager.api.ChartProperties;
import com.skysql.manager.api.Monitors;
import com.skysql.manager.api.Monitors.MonitorNames;
import com.skysql.manager.api.NodeInfo;
import com.skysql.manager.ui.components.ChartControls;
import com.skysql.manager.ui.components.ChartsLayout;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;

import fi.jasoft.dragdroplayouts.DDCssLayout;
import fi.jasoft.dragdroplayouts.client.ui.LayoutDragMode;

@SuppressWarnings("serial")
public class PanelInfo extends HorizontalSplitPanel {

	private static final String NOT_AVAILABLE = "n/a";
	private static final int PANEL_SPLIT_X = 305;

	private ClusterComponent lastComponent;
	private Component systemGrid, nodeGrid;
	private VerticalLayout infoLayout, chartsLayout;
	private DDCssLayout chartsArray;
	private String chartTime, chartInterval = "1800";
	private String systemLabelsStrings[] = { "State", "Availability", "Connections", "Traffic", "Last Backup", "Start Date", "Last Access" };
	private String nodeLabelsStrings[] = { "State", "Availability", "Connections", "Traffic", "Commands Running", "Public IP", "Private IP", "Instance ID", };
	private Label systemLabels[], nodeLabels[];
	private ChartControls chartControls;
	private ChartsLayout chartsArrayLayout;
	private Panel chartsPanel;
	private Label nameLabel;
	private ChartProperties chartProperties;
	private boolean isExpanded = false;

	private ValueChangeListener chartIntervalListener = new ValueChangeListener() {

		public void valueChange(ValueChangeEvent event) {
			Integer value = (Integer) (event.getProperty()).getValue();
			chartProperties.setTimeSpan(value);
			chartInterval = Integer.toString(value);
			refresh();
		}
	};

	private ValueChangeListener chartThemeListener = new ValueChangeListener() {
		private static final long serialVersionUID = 0x4C656F6E6172646FL;

		public void valueChange(ValueChangeEvent event) {
			String themeName = (String) event.getProperty().getValue();
			chartProperties.setTheme(themeName);
		}

	};

	PanelInfo() {

		addStyleName("infoTab");
		setSizeFull();
		setSplitPosition(PANEL_SPLIT_X, Unit.PIXELS);

		createInfoLayout();
		createChartsLayout();

	}

	private void createInfoLayout() {

		infoLayout = new VerticalLayout();
		infoLayout.addStyleName("infoLayout");
		infoLayout.setWidth(PANEL_SPLIT_X, Unit.PIXELS);
		infoLayout.setSpacing(true);
		addComponent(infoLayout);

		final HorizontalLayout headerLayout = new HorizontalLayout();
		headerLayout.addStyleName("panelHeaderLayout");
		headerLayout.setWidth("100%");
		headerLayout.setSpacing(true);
		headerLayout.setMargin(new MarginInfo(false, true, false, true));
		infoLayout.addComponent(headerLayout);

		nameLabel = new Label();
		nameLabel.addStyleName("nameLabel");
		nameLabel.setSizeUndefined();
		headerLayout.addComponent(nameLabel);
		headerLayout.setComponentAlignment(nameLabel, Alignment.MIDDLE_LEFT);

		final Button editButton = new Button("Edit");
		editButton.setEnabled(false);
		headerLayout.addComponent(editButton);
		headerLayout.setComponentAlignment(editButton, Alignment.MIDDLE_RIGHT);

		// this will become the editing function of what properties are displayed in the info layout
		/***
		final Button saveButton = new Button("Done");
		editButton.addClickListener(new Button.ClickListener() {

			public void buttonClick(ClickEvent event) {
				nameField = new TextField();
				nameField.setImmediate(true);
				nameField.setValue(nameLabel.getValue());
				nameField.setWidth("12em");
				nameField.focus();
				nameField.addValueChangeListener(new ValueChangeListener() {

					public void valueChange(ValueChangeEvent event) {
						saveButton.click();
					}
				});
				headerLayout.replaceComponent(nameLabel, nameField);
				headerLayout.setComponentAlignment(nameField, Alignment.MIDDLE_LEFT);
				headerLayout.replaceComponent(editButton, saveButton);
				headerLayout.setComponentAlignment(saveButton, Alignment.MIDDLE_RIGHT);
			}
		});

		saveButton.addClickListener(new Button.ClickListener() {

			public void buttonClick(ClickEvent event) {
				String name = nameField.getValue();
				nameLabel.setValue(name);
				headerLayout.replaceComponent(nameField, nameLabel);
				headerLayout.setComponentAlignment(nameLabel, Alignment.MIDDLE_LEFT);
				headerLayout.replaceComponent(saveButton, editButton);
				headerLayout.setComponentAlignment(editButton, Alignment.MIDDLE_RIGHT);
				lastComponent.setName(name);
				if (lastComponent instanceof NodeInfo) {
					//((NodeInfo) lastComponent).saveName(name);
				} else if (lastComponent instanceof SystemRecord) {
					//systemInfo.saveName(name);
				}
				ComponentButton componentButton = lastComponent.getButton();
				componentButton.setName(name);
			}
		});
		 ***/

		systemLabels = new Label[systemLabelsStrings.length];
		systemGrid = createCurrentInfo(systemLabels, systemLabelsStrings);
		nodeLabels = new Label[nodeLabelsStrings.length];
		nodeGrid = createCurrentInfo(nodeLabels, nodeLabelsStrings);
	}

	public void setComponentName(String name) {
		nameLabel.setValue(name);
	}

	private Component createCurrentInfo(Label[] labels, String[] values) {
		GridLayout currentGrid = new GridLayout(2, labels.length);
		currentGrid.addStyleName("currentInfo");
		currentGrid.setSpacing(true);
		currentGrid.setMargin(new MarginInfo(false, false, false, true));
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
		addComponent(chartsLayout);

		final HorizontalLayout chartsHeaderLayout = new HorizontalLayout();
		chartsHeaderLayout.setStyleName("panelHeaderLayout");
		chartsHeaderLayout.setWidth("100%");
		chartsHeaderLayout.setSpacing(true);
		chartsHeaderLayout.setMargin(new MarginInfo(false, true, false, true));
		chartsLayout.addComponent(chartsHeaderLayout);

		chartControls = new ChartControls();
		chartControls.addIntervalSelectionListener(chartIntervalListener);
		chartControls.addThemeSelectionListener(chartThemeListener);
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

			public void buttonClick(ClickEvent event) {
				new ChartsDialog(chartsArrayLayout, null);
			}
		});

		final Button editButton = new Button("Edit");
		editButton.setDescription("Enter Editing mode");
		final Button saveButton = new Button("Done");
		saveButton.setDescription("Exit Editing mode");
		buttonsLayout.addComponent(editButton);
		editButton.addClickListener(new Button.ClickListener() {

			public void buttonClick(ClickEvent event) {
				buttonsLayout.replaceComponent(editButton, saveButton);
				chartsArrayLayout.setDragMode(LayoutDragMode.CLONE);
				chartsArrayLayout.setEditable(true);
				chartsHeaderLayout.setStyleName("panelHeaderLayout-editable");
				editMonitorsButton.setVisible(true);
				addChartButton.setVisible(true);
				OverviewPanel overviewPanel = getSession().getAttribute(OverviewPanel.class);
				overviewPanel.setEnabled(false);

			}
		});

		saveButton.addClickListener(new Button.ClickListener() {

			public void buttonClick(ClickEvent event) {
				buttonsLayout.replaceComponent(saveButton, editButton);
				chartsArrayLayout.setDragMode(LayoutDragMode.NONE);
				chartsArrayLayout.setEditable(false);
				chartsHeaderLayout.setStyleName("panelHeaderLayout");
				editMonitorsButton.setVisible(false);
				addChartButton.setVisible(false);
				OverviewPanel overviewPanel = getSession().getAttribute(OverviewPanel.class);
				overviewPanel.setEnabled(true);
			}
		});

		final Button expandButton = new NativeButton();
		expandButton.setStyleName("expandButton");
		expandButton.setDescription("Expand/Reduce viewing area");
		buttonsLayout.addComponent(expandButton);
		buttonsLayout.setComponentAlignment(expandButton, Alignment.MIDDLE_CENTER);
		expandButton.addClickListener(new Button.ClickListener() {

			public void buttonClick(ClickEvent event) {
				isExpanded = !isExpanded;

				AnimatorProxy proxy = getSession().getAttribute(AnimatorProxy.class);
				proxy.addListener(new AnimationListener() {
					public void onAnimation(AnimationEvent event) {
						Component component = event.getComponent();
						component.setVisible(isExpanded ? false : true);
					}
				});
				//				OverviewPanel overviewPanel = getSession().getAttribute(OverviewPanel.class);
				//				if (!isExpanded) {
				//					overviewPanel.setVisible(isExpanded ? false : true);
				//				}
				//				proxy.animate(overviewPanel, isExpanded ? AnimType.ROLL_UP_CLOSE : AnimType.ROLL_DOWN_OPEN).setDuration(500).setDelay(100);
				//
				//				TopPanel topPanel = getSession().getAttribute(TopPanel.class);
				//				if (!isExpanded) {
				//					topPanel.setVisible(isExpanded ? false : true);
				//				}
				//				proxy.animate(topPanel, isExpanded ? AnimType.ROLL_UP_CLOSE : AnimType.ROLL_DOWN_OPEN).setDuration(500).setDelay(100);

				VerticalLayout topMid = getSession().getAttribute(VerticalLayout.class);
				if (!isExpanded) {
					topMid.setVisible(isExpanded ? false : true);
				}
				proxy.animate(topMid, isExpanded ? AnimType.ROLL_UP_CLOSE : AnimType.ROLL_DOWN_OPEN).setDuration(500).setDelay(100);

				expandButton.setStyleName(isExpanded ? "contractButton" : "expandButton");
			}
		});

		chartsPanel = new Panel();
		chartsPanel.setSizeFull();
		chartsPanel.addStyleName(Runo.PANEL_LIGHT);
		chartsLayout.addComponent(chartsPanel);
		chartsLayout.setExpandRatio(chartsPanel, 1.0f);

	}

	public void refresh() {
		final ClusterComponent componentInfo = getSession().getAttribute(ClusterComponent.class);
		if (componentInfo == null) {
			return;
		}

		if (componentInfo != lastComponent) {

			// if we have not attached a component yet...
			if (lastComponent == null) {
				switch (componentInfo.getType()) {
				case system:
					infoLayout.addComponent(systemGrid);
					break;

				case node:
					infoLayout.addComponent(nodeGrid);
					break;
				}
			} else {

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

			// call to make Monitors set "current" systemType set of Monitors... to be changed
			Monitors.getMonitorsList(componentInfo.getSystemType());

			// zero out existing display in order to eliminate false user readings if new data is slow to be retrieved
			if (chartsArrayLayout != null) {
				chartsArrayLayout.hideCharts();
			}

			// fetch user-based chart properties once per session
			chartProperties = getSession().getAttribute(ChartProperties.class);
			if (chartProperties == null) {
				chartProperties = new ChartProperties(null);
				getSession().setAttribute(ChartProperties.class, chartProperties);
			}
			chartControls.selectInterval(chartProperties.getTimeSpan());
			chartControls.selectTheme(chartProperties.getTheme());

			chartsArrayLayout = new ChartsLayout(false);
			chartsArrayLayout.initializeCharts(chartProperties, componentInfo.getSystemType());
			chartsArray = chartsArrayLayout;
			chartsPanel.setContent(chartsArray);

		}

		nameLabel.setValue(componentInfo.getName());

		String value, values[];
		Label[] currentLabels;
		LinkedHashMap<String, String> monitorLatest;
		switch (componentInfo.getType()) {
		case system:
			SystemRecord systemRecord = (SystemRecord) componentInfo;
			currentLabels = systemLabels;
			monitorLatest = systemRecord.getMonitorLatest().getData();
			String systemValues[] = { (value = systemRecord.getState()) != null ? value : NOT_AVAILABLE,
					(value = monitorLatest.get(MonitorNames.availability.toString())) != null ? value + "%" : NOT_AVAILABLE,
					(value = monitorLatest.get(MonitorNames.connections.toString())) != null ? value : NOT_AVAILABLE,
					(value = monitorLatest.get(MonitorNames.traffic.toString())) != null ? value + " KB" : NOT_AVAILABLE,
					(value = systemRecord.getLastBackup()) != null ? DateConversion.adjust(value) : NOT_AVAILABLE,
					(value = systemRecord.getStartDate()) != null ? DateConversion.adjust(value) : NOT_AVAILABLE,
					(value = systemRecord.getLastAccess()) != null ? DateConversion.adjust(value) : NOT_AVAILABLE };
			values = systemValues;
			break;

		case node:
			NodeInfo nodeInfo = (NodeInfo) componentInfo;
			currentLabels = nodeLabels;
			monitorLatest = nodeInfo.getMonitorLatest().getData();
			String nodeValues[] = { (value = nodeInfo.getState()) != null ? value : NOT_AVAILABLE,
					(value = monitorLatest.get(MonitorNames.availability.toString())) != null ? value + "%" : NOT_AVAILABLE,
					(value = monitorLatest.get(MonitorNames.connections.toString())) != null ? value : NOT_AVAILABLE,
					(value = monitorLatest.get(MonitorNames.traffic.toString())) != null ? value + " KB" : NOT_AVAILABLE,
					(value = nodeInfo.getCommand()) != null ? value : NOT_AVAILABLE, (value = nodeInfo.getPublicIP()) != null ? value : NOT_AVAILABLE,
					(value = nodeInfo.getPrivateIP()) != null ? value : NOT_AVAILABLE, (value = nodeInfo.getInstanceID()) != null ? value : NOT_AVAILABLE };
			values = nodeValues;
			break;

		default:
			return;
		}

		for (int i = 0; i < values.length && i < currentLabels.length; i++) {
			value = values[i];
			if (!((String) currentLabels[i].getValue()).equals(value)) {
				currentLabels[i].setValue(value);
			}
		}

		chartsArrayLayout.refresh(chartTime, chartInterval);

		lastComponent = componentInfo;
	}
}
