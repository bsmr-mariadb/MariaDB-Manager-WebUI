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

import org.vaadin.jouni.animator.AnimatorProxy;
import org.vaadin.jouni.animator.AnimatorProxy.AnimationEvent;
import org.vaadin.jouni.animator.AnimatorProxy.AnimationListener;
import org.vaadin.jouni.animator.shared.AnimType;

import com.skysql.manager.ClusterComponent;
import com.skysql.manager.SystemRecord;
import com.skysql.manager.api.ChartProperties;
import com.skysql.manager.api.Commands;
import com.skysql.manager.api.NodeInfo;
import com.skysql.manager.api.NodeStates;
import com.skysql.manager.ui.components.ChartControls;
import com.skysql.manager.ui.components.ChartsLayout;
import com.skysql.manager.ui.components.ComponentButton;
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
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;

import fi.jasoft.dragdroplayouts.DDCssLayout;
import fi.jasoft.dragdroplayouts.client.ui.LayoutDragMode;

@SuppressWarnings("serial")
public class PanelInfo extends HorizontalSplitPanel {

	private static final String NOT_AVAILABLE = "n/a";

	private ClusterComponent lastComponent;
	private Component systemGrid, nodeGrid;
	private VerticalLayout infoLayout, chartsLayout;
	private DDCssLayout chartsArray;
	private String chartTime, chartInterval = "1800";
	private String systemLabelsStrings[] = { "Status", "Availability", "Connections", "Data Transfer", "Last Backup", "Start Date", "Last Access" };
	private String nodeLabelsStrings[] = { "Status", "Availability", "Connections", "Data Transfer", "Commands Running", "Public IP", "Private IP",
			"Instance ID", };
	private Label systemLabels[], nodeLabels[];
	private ChartControls chartControls;
	private ChartsLayout chartsArrayLayout;
	private Label nameLabel;
	private TextField nameField;
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

	PanelInfo() {

		setSizeFull();
		addStyleName("infoTab");

		setSplitPosition(300, Unit.PIXELS);
		createInfoLayout();
		createChartsLayout();

	}

	private void createInfoLayout() {

		infoLayout = new VerticalLayout();
		infoLayout.addStyleName("infoLayout");
		infoLayout.setWidth("300px");
		infoLayout.setSpacing(true);
		addComponent(infoLayout);

		final HorizontalLayout nameLayout = new HorizontalLayout();
		nameLayout.addStyleName("panelHeaderLayout");
		nameLayout.setWidth("100%");
		nameLayout.setSpacing(true);
		nameLayout.setMargin(new MarginInfo(false, true, false, true));
		infoLayout.addComponent(nameLayout);

		nameLabel = new Label();
		nameLabel.addStyleName("nameLabel");
		nameLabel.setSizeUndefined();
		nameLayout.addComponent(nameLabel);
		nameLayout.setComponentAlignment(nameLabel, Alignment.MIDDLE_LEFT);

		final Button editButton = new Button("Edit");
		// TODO: this will become the editing function of what properties are displayed in the info layout - now the system/node name can be edited from the respective dialogs in the Navigation/Overview panel
		editButton.setEnabled(false);
		final Button saveButton = new Button("Done");

		nameLayout.addComponent(editButton);
		nameLayout.setComponentAlignment(editButton, Alignment.MIDDLE_RIGHT);
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
				nameLayout.replaceComponent(nameLabel, nameField);
				nameLayout.setComponentAlignment(nameField, Alignment.MIDDLE_LEFT);
				nameLayout.replaceComponent(editButton, saveButton);
				nameLayout.setComponentAlignment(saveButton, Alignment.MIDDLE_RIGHT);
			}
		});

		saveButton.addClickListener(new Button.ClickListener() {

			public void buttonClick(ClickEvent event) {
				String name = nameField.getValue();
				nameLabel.setValue(name);
				nameLayout.replaceComponent(nameField, nameLabel);
				nameLayout.setComponentAlignment(nameLabel, Alignment.MIDDLE_LEFT);
				nameLayout.replaceComponent(saveButton, editButton);
				nameLayout.setComponentAlignment(editButton, Alignment.MIDDLE_RIGHT);
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
		currentGrid.setMargin(new MarginInfo(false, true, false, true));
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
		chartProperties = new ChartProperties(null, null);

		chartsLayout = new VerticalLayout();
		chartsLayout.addStyleName("chartsLayout");
		chartsLayout.setHeight("100%");
		chartsLayout.setSpacing(true);
		addComponent(chartsLayout);

		final HorizontalLayout chartsHeaderLayout = new HorizontalLayout();
		chartsHeaderLayout.addStyleName("panelHeaderLayout");
		chartsHeaderLayout.setWidth("100%");
		chartsHeaderLayout.setSpacing(true);
		chartsHeaderLayout.setMargin(new MarginInfo(false, true, false, true));
		chartsLayout.addComponent(chartsHeaderLayout);

		chartControls = new ChartControls(chartProperties);
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

			public void buttonClick(ClickEvent event) {
				new ChartsDialog(chartsArrayLayout, null);
			}
		});

		final Button editButton = new Button("Edit");
		final Button saveButton = new Button("Done");
		buttonsLayout.addComponent(editButton);
		editButton.addClickListener(new Button.ClickListener() {

			public void buttonClick(ClickEvent event) {
				buttonsLayout.replaceComponent(editButton, saveButton);
				chartsArrayLayout.setDragMode(LayoutDragMode.CLONE);
				chartsArrayLayout.setEditable(true);
				editMonitorsButton.setVisible(true);
				addChartButton.setVisible(true);
			}
		});

		saveButton.addClickListener(new Button.ClickListener() {

			public void buttonClick(ClickEvent event) {
				buttonsLayout.replaceComponent(saveButton, editButton);
				chartsArrayLayout.setDragMode(LayoutDragMode.NONE);
				chartsArrayLayout.setEditable(false);
				editMonitorsButton.setVisible(false);
				addChartButton.setVisible(false);
			}
		});

		final Button expandButton = new NativeButton();
		expandButton.setStyleName("expandButton");
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

				expandButton.setStyleName(isExpanded ? "shrinkButton" : "expandButton");
			}
		});

		Panel panel = new Panel();
		panel.setSizeFull();
		panel.addStyleName(Runo.PANEL_LIGHT);
		chartsLayout.addComponent(panel);
		chartsLayout.setExpandRatio(panel, 1.0f);

		chartsArray = chartsArray(chartProperties);
		panel.setContent(chartsArray);

	}

	public DDCssLayout chartsArray(ChartProperties chartProperties) {

		chartsArrayLayout = new ChartsLayout(false);
		chartsArrayLayout.initializeCharts(chartProperties);

		return chartsArrayLayout;

	}

	public void refresh() {
		ClusterComponent componentInfo = getSession().getAttribute(ClusterComponent.class);
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

			// zero out existing display in order to eliminate false user readings if new data is slow to be retireved
			chartsArrayLayout.hideCharts();
		}

		nameLabel.setValue(componentInfo.getName());

		String value, values[];
		Label[] currentLabels;

		switch (componentInfo.getType()) {
		case system:
			SystemRecord systemRecord = (SystemRecord) componentInfo;
			currentLabels = systemLabels;
			String systemValues[] = { (value = systemRecord.getStatus()) != null ? NodeStates.getDescription(value) : NOT_AVAILABLE,
					(value = systemRecord.getHealth()) != null ? value + "%" : NOT_AVAILABLE,
					(value = systemRecord.getConnections()) != null ? value : NOT_AVAILABLE,
					(value = systemRecord.getPackets()) != null ? value + " KB" : NOT_AVAILABLE,
					(value = systemRecord.getLastBackup()) != null ? value : NOT_AVAILABLE,
					(value = systemRecord.getStartDate()) != null ? value : NOT_AVAILABLE,
					(value = systemRecord.getLastAccess()) != null ? value : NOT_AVAILABLE };
			values = systemValues;
			break;

		case node:
			NodeInfo nodeInfo = (NodeInfo) componentInfo;
			currentLabels = nodeLabels;
			String nodeValues[] = { (value = nodeInfo.getStatus()) != null ? NodeStates.getDescription(value) : NOT_AVAILABLE,
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
			if (!((String) currentLabels[i].getValue()).equals(value)) {
				currentLabels[i].setValue(value);
			}
		}

		chartsArrayLayout.refresh(chartTime, chartInterval);

		lastComponent = componentInfo;
	}
}
