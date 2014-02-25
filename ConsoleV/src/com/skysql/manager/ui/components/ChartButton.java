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

package com.skysql.manager.ui.components;

import com.skysql.manager.UserChart;
import com.skysql.manager.ui.ChartsDialog;
import com.skysql.manager.ui.TimelineDialog;
import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.Axis;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.Marker;
import com.vaadin.addon.charts.model.PlotOptionsLine;
import com.vaadin.addon.charts.model.Title;
import com.vaadin.addon.charts.model.VerticalAlign;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.event.MouseEvents;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class ChartButton extends CustomComponent {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private VerticalLayout layout;
	private Embedded editButton, deleteButton;
	private Label draggable;
	private Chart chart;
	private ChartButton thisButton;
	private ChartsLayout chartsLayout;
	private boolean isEditable;

	public ChartButton(UserChart userChart) {
		thisButton = this;

		setSizeUndefined();
		addStyleName("chartButton");

		layout = new VerticalLayout();
		layout.setSizeUndefined();

		layout.addLayoutClickListener(new LayoutClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void layoutClick(LayoutClickEvent event) {

				Component child;
				if (event.isDoubleClick() && (child = event.getChildComponent()) != null && (child instanceof Chart)) {
					if (isEditable) {
						new ChartsDialog(chartsLayout, thisButton);
					} else {
						new TimelineDialog((UserChart) getData());
					}
				}
			}
		});

		chart = new Chart();
		chart.addStyleName("chartObject");
		chart.setHeight("240px");
		chart.setWidth("400px");

		setData(userChart);

		Configuration configuration = new Configuration();
		ChartType chartType = ChartType.LINE; // default to LINE
		if (UserChart.ChartType.LineChart.name().equals(userChart.getType())) {
			chartType = ChartType.LINE;
		} else if (UserChart.ChartType.AreaChart.name().equals(userChart.getType())) {
			chartType = ChartType.AREARANGE;
		}

		configuration.getChart().setType(chartType);
		configuration.getTitle().setText(userChart.getName());
		configuration.getSubTitle().setText(userChart.getDescription());
		configuration.getCredits().setEnabled(false);

		Axis yAxis = configuration.getyAxis();
		String unit = userChart.getUnit();
		yAxis.setTitle(new Title(unit == null ? "" : unit));
		yAxis.getTitle().setVerticalAlign(VerticalAlign.HIGH);

		PlotOptionsLine plotOptions = new PlotOptionsLine();
		plotOptions.setMarker(new Marker(false));
		configuration.setPlotOptions(plotOptions);

		layout.addComponent(chart);

		// TODO: can we hold off on this?
		chart.drawChart(configuration);

		setCompositionRoot(layout);
	}

	public Chart getChart() {
		return chart;
	}

	public void setChartsLayout(ChartsLayout chartsLayout) {
		this.chartsLayout = chartsLayout;
	}

	public void setEditable(boolean editable) {
		isEditable = editable;

		if (editable) {
			chart.setEnabled(false);

			addStyleName("draggable");

			draggable = new Label("Click and drag to reorder.\n  Double-click to edit.", ContentMode.PREFORMATTED);
			draggable.addStyleName("draggableLabel");
			layout.addComponent(draggable);

			editButton = new Embedded(null, new ThemeResource("img/edit.png"));
			editButton.addStyleName("editChart");
			editButton.setDescription("Edit Chart");
			editButton.setData(this);
			layout.addComponent(editButton);
			editButton.addClickListener(new MouseEvents.ClickListener() {
				private static final long serialVersionUID = 0x4C656F6E6172646FL;

				public void click(ClickEvent event) {
					new ChartsDialog(chartsLayout, thisButton);
				}
			});

			deleteButton = new Embedded(null, new ThemeResource("img/delete.png"));
			deleteButton.addStyleName("deleteChart");
			deleteButton.setDescription("Delete Chart");
			deleteButton.setData(this);
			layout.addComponent(deleteButton);
			deleteButton.addClickListener(new MouseEvents.ClickListener() {
				private static final long serialVersionUID = 0x4C656F6E6172646FL;

				public void click(ClickEvent event) {
					chartsLayout.deleteChart(thisButton);
				}
			});

		} else {
			chart.setEnabled(true);

			setStyleName(getStyleName().replace("draggable", ""));

			if (layout != null) {
				if (draggable != null) {
					layout.removeComponent(draggable);
				}
				if (editButton != null) {
					layout.removeComponent(editButton);
				}
				if (deleteButton != null) {
					layout.removeComponent(deleteButton);
				}
			}
			draggable = null;
			editButton = null;
			deleteButton = null;

		}
	}

}
