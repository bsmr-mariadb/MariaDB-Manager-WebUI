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

import com.skysql.manager.MonitorRecord;
import com.skysql.manager.UserChart;
import com.skysql.manager.api.Monitors;
import com.skysql.manager.ui.components.ChartButton;
import com.skysql.manager.ui.components.ChartsLayout;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class ChartPreviewLayout extends VerticalLayout {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private String chartTime, chartInterval = "1800";
	private ChartsLayout chartLayout;
	private UserChart userChart;
	private TextField chartName, chartDescription, chartUnit;
	private NativeSelect chartSelectType, selectCount;
	private boolean blockRefresh = false;

	public ChartPreviewLayout(final UserChart userChart) {
		this.userChart = userChart;

		addStyleName("ChartPreviewLayout");
		setSpacing(true);
		setMargin(true);

		final Label monitorsLabel = new Label("Display as Chart");
		monitorsLabel.setStyleName("dialogLabel");
		addComponent(monitorsLabel);
		setComponentAlignment(monitorsLabel, Alignment.TOP_CENTER);

		HorizontalLayout chartInfo = new HorizontalLayout();
		chartInfo.setSpacing(true);
		addComponent(chartInfo);
		setComponentAlignment(chartInfo, Alignment.MIDDLE_CENTER);

		FormLayout formLayout = new FormLayout();
		chartInfo.addComponent(formLayout);

		chartName = new TextField("Title");
		chartName.setImmediate(true);
		formLayout.addComponent(chartName);

		chartDescription = new TextField("Description");
		chartDescription.setWidth("25em");
		chartDescription.setImmediate(true);
		formLayout.addComponent(chartDescription);

		chartUnit = new TextField("Unit");
		chartUnit.setImmediate(true);
		formLayout.addComponent(chartUnit);

		chartSelectType = new NativeSelect("Type");
		chartSelectType.setImmediate(true);
		for (String type : UserChart.chartTypes()) {
			chartSelectType.addItem(type);
		}
		chartSelectType.setNullSelectionAllowed(false);
		formLayout.addComponent(chartSelectType);

		selectCount = new NativeSelect("Points");
		selectCount.setImmediate(true);
		for (int points : UserChart.chartPoints()) {
			selectCount.addItem(points);
			selectCount.setItemCaption(points, String.valueOf(points));
		}
		selectCount.setNullSelectionAllowed(false);
		formLayout.addComponent(selectCount);

		updateChartInfo(userChart.getName(), userChart.getDescription(), userChart.getUnit(), userChart.getType(), userChart.getPoints());

		chartName.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {

				String chartName = (String) (event.getProperty()).getValue();
				userChart.setName(chartName);
				refreshChart();

			}
		});

		chartDescription.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {

				String value = (String) (event.getProperty()).getValue();
				userChart.setDescription(value);
				refreshChart();

			}
		});

		chartUnit.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {

				String value = (String) (event.getProperty()).getValue();
				userChart.setUnit(value);
				refreshChart();

			}
		});

		chartSelectType.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {

				String value = (String) (event.getProperty()).getValue();
				userChart.setType(value);
				refreshChart();

			}
		});

		selectCount.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {

				int points = (Integer) (event.getProperty()).getValue();
				userChart.setPoints(points);
				refreshChart();

			}
		});

		chartLayout = drawChart();
		addComponent(chartLayout);

	}

	private void updateChartInfo(String name, String description, String unit, String type, int points) {
		blockRefresh = true;

		chartName.setValue(name != null ? name : "");
		userChart.setName(name);

		chartDescription.setValue(description != null ? description : "");
		userChart.setDescription(description);

		chartUnit.setValue(unit != null ? unit : "");
		userChart.setUnit(unit);

		chartSelectType.setValue(type);

		selectCount.setValue(points);

		blockRefresh = false;

	}

	private ChartsLayout drawChart() {
		ChartsLayout newChartsLayout = new ChartsLayout(true);
		newChartsLayout.addStyleName("chartPreview");
		userChart.clearMonitorData();
		ChartButton newChartButton = new ChartButton(userChart);
		newChartButton.setChartsLayout(newChartsLayout);
		newChartsLayout.addComponent(newChartButton);
		newChartsLayout.refresh(chartTime, chartInterval);
		return newChartsLayout;
	}

	public void refreshChart() {
		if (blockRefresh == false) {
			ChartsLayout newChartLayout = drawChart();
			replaceComponent(chartLayout, newChartLayout);
			chartLayout = newChartLayout;
		}
	}

	public void refreshUserChart() {
		if (userChart.getMonitorIDs().size() == 1) {
			// update chart fields from selection of Monitor only when it's the first and only monitor mapped to chart
			String monitorID = userChart.getMonitorIDs().get(0);
			MonitorRecord monitor = Monitors.getMonitor(monitorID);
			updateChartInfo(monitor.getName(), monitor.getDescription(), monitor.getUnit(), monitor.getChartType(), userChart.getPoints());
		}
	}
}