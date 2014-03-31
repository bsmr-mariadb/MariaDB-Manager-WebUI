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
import java.util.LinkedHashMap;

import com.skysql.manager.MonitorRecord;
import com.skysql.manager.api.Monitors;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * The Class MonitorsLayout is used to build the left hand-side of the Monitors to Chart mappings dialog.
 */
public class MonitorsLayout extends VerticalLayout {

	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private int componentIndex;
	private ChartPreviewLayout chartPreviewLayout;
	private LinkedHashMap<String, MonitorRecord> availableMonitors;
	private ArrayList<String> selectedMonitorIDs;
	private ArrayList<ComboBox> selectMonitorList;

	/** The monitor select listener. */
	private ValueChangeListener monitorSelectListener = new ValueChangeListener() {
		private static final long serialVersionUID = 0x4C656F6E6172646FL;

		public void valueChange(ValueChangeEvent event) {

			refreshMonitors();
		}
	};

	/**
	 * Instantiates a new monitors layout.
	 *
	 * @param selectedMonitorIDs the selected monitor IDs
	 */
	public MonitorsLayout(ArrayList<String> selectedMonitorIDs) {
		this.selectedMonitorIDs = selectedMonitorIDs;

		addStyleName("MonitorsLayout");
		setSpacing(true);
		setMargin(true);

		Monitors.reloadMonitors();
		availableMonitors = Monitors.getMonitorsList();
		initializeMonitors();

	}

	/**
	 * Initialize monitors list.
	 */
	public void initializeMonitors() {

		final Label monitorsLabel = new Label("Select Monitors");
		monitorsLabel.setStyleName("dialogLabel");
		addComponent(monitorsLabel);

		componentIndex = getComponentCount(); // where the monitors start
		selectMonitorList = new ArrayList<ComboBox>();
		for (String monitorID : selectedMonitorIDs) {
			addComponent(addRow(monitorID));
		}

		final Button addButton = new Button("Add");
		addComponent(addButton);
		addButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				Component newRow = addRow(null);
				replaceComponent(addButton, newRow);
				addComponent(addButton);
			}
		});

	}

	/**
	 * Adds the chart preview.
	 *
	 * @param chartPreviewLayout the chart preview layout
	 */
	public void addChartPreview(ChartPreviewLayout chartPreviewLayout) {
		this.chartPreviewLayout = chartPreviewLayout;
	}

	/**
	 * Refresh monitors.
	 */
	private void refreshMonitors() {

		selectedMonitorIDs.clear();
		for (ComboBox select : selectMonitorList) {
			String monitorID = (String) select.getValue();
			selectedMonitorIDs.add(monitorID);
		}
		chartPreviewLayout.refreshUserChart();
		chartPreviewLayout.refreshChart();

	}

	/**
	 * Adds a new monitor row.
	 *
	 * @param monitorID the monitor id
	 * @return the component
	 */
	private Component addRow(String monitorID) {
		HorizontalLayout row = new HorizontalLayout();
		ComboBox selectMonitor = new ComboBox();
		for (MonitorRecord availMonitor : availableMonitors.values()) {
			selectMonitor.addItem(availMonitor.getID());
			selectMonitor.setItemCaption(availMonitor.getID(), availMonitor.getName());
		}
		row.addComponent(selectMonitor);
		selectMonitorList.add(selectMonitor);

		selectMonitor.setValue(monitorID);
		selectMonitor.setNullSelectionAllowed(false);
		selectMonitor.setImmediate(true);
		selectMonitor.addValueChangeListener(monitorSelectListener);

		if (getComponentCount() > componentIndex) {
			Button deleteButton = new Button("X");
			row.addComponent(deleteButton);
			deleteButton.setData(selectMonitor);
			deleteButton.addClickListener(new Button.ClickListener() {
				private static final long serialVersionUID = 0x4C656F6E6172646FL;

				public void buttonClick(ClickEvent event) {
					// remove combobox from list
					Button button = event.getButton();
					ComboBox select = (ComboBox) button.getData();
					selectMonitorList.remove(select);

					// remove layout from dialog
					int layoutIndex = getComponentIndex(button.getParent());
					HorizontalLayout layout = (HorizontalLayout) getComponent(layoutIndex);
					removeComponent(layout);

					refreshMonitors();
				}
			});
		}

		focus();

		return (row);

	}
}
