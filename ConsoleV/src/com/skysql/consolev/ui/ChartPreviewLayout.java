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

import com.skysql.consolev.api.UserChart;
import com.skysql.consolev.ui.components.ChartsLayout;
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
	private String chartName;

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

		TextField chartName = new TextField("Title");
		chartName.setImmediate(true);
		chartName.setValue(userChart.getName());
		formLayout.addComponent(chartName);
		chartName.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {

				String chartName = (String) (event.getProperty()).getValue();
				userChart.setName(chartName);
				refresh();

			}
		});

		TextField chartDescription = new TextField("Description");
		chartDescription.setWidth("25em");
		chartDescription.setImmediate(true);
		chartDescription.setValue(userChart.getDescription());
		formLayout.addComponent(chartDescription);
		chartDescription.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {

				String value = (String) (event.getProperty()).getValue();
				userChart.setDescription(value);
				refresh();

			}
		});

		TextField chartUnit = new TextField("Unit");
		chartUnit.setImmediate(true);
		chartUnit.setValue(userChart.getUnit());
		formLayout.addComponent(chartUnit);
		chartUnit.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {

				String value = (String) (event.getProperty()).getValue();
				userChart.setUnit(value);
				refresh();

			}
		});

		// formLayout = new FormLayout();
		// chartInfo.addComponent(formLayout);
		NativeSelect chartSelectType = new NativeSelect("Type");
		chartSelectType.setImmediate(true);
		for (String type : UserChart.chartTypes()) {
			chartSelectType.addItem(type);
		}
		chartSelectType.setNullSelectionAllowed(false);
		chartSelectType.setValue(userChart.getType());
		formLayout.addComponent(chartSelectType);
		chartSelectType.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {

				String value = (String) (event.getProperty()).getValue();
				userChart.setType(value);
				refresh();

			}
		});

		NativeSelect selectCount = new NativeSelect("Points");
		selectCount.setImmediate(true);
		for (int points : UserChart.chartPoints()) {
			selectCount.addItem(points);
			selectCount.setItemCaption(points, String.valueOf(points));
		}
		selectCount.setNullSelectionAllowed(false);
		selectCount.setValue(userChart.getPoints());
		formLayout.addComponent(selectCount);
		selectCount.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {

				int points = (Integer) (event.getProperty()).getValue();
				userChart.setPoints(points);
				refresh();

			}
		});

		chartLayout = drawChart();
		addComponent(chartLayout);

	}

	private ChartsLayout drawChart() {
		ChartsLayout newChartsLayout = new ChartsLayout(true);
		newChartsLayout.addStyleName("chartPreview");
		userChart.clearMonitorData();
		newChartsLayout.initializeChart(userChart);
		newChartsLayout.refresh(chartTime, chartInterval, String.valueOf(userChart.getPoints()));
		return newChartsLayout;
	}

	public void refresh() {
		ChartsLayout newChartLayout = drawChart();
		replaceComponent(chartLayout, newChartLayout);
		chartLayout = newChartLayout;
	}
}
