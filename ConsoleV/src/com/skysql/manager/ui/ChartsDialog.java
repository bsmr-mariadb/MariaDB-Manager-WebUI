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

import com.skysql.java.Logging;
import com.skysql.manager.ClusterComponent;
import com.skysql.manager.MonitorRecord;
import com.skysql.manager.UserChart;
import com.skysql.manager.api.Monitors;
import com.skysql.manager.ui.components.ChartButton;
import com.skysql.manager.ui.components.ChartsLayout;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * The Class ChartsDialog.
 */
public class ChartsDialog {

	Window dialogWindow;
	Button openButton;
	Button closebutton;
	UserChart newUserChart;
	final ChartButton chartButton;
	final ChartsLayout chartsLayout;

	/**
	 * Instantiates a new charts dialog.
	 *
	 * @param chartsLayout the charts layout
	 * @param chartButton the chart button
	 */
	public ChartsDialog(final ChartsLayout chartsLayout, final ChartButton chartButton) {

		this.chartButton = chartButton;
		this.chartsLayout = chartsLayout;

		dialogWindow = new ModalWindow("Monitors to Chart mapping", "775px");

		HorizontalLayout wrapper = new HorizontalLayout();
		//wrapper.setWidth("100%");
		wrapper.setMargin(true);

		UI.getCurrent().addWindow(dialogWindow);

		newUserChart = (chartButton != null) ? new UserChart((UserChart) chartButton.getData()) : newUserChart();

		ArrayList<String> monitorIDs = newUserChart.getMonitorIDs();
		MonitorsLayout monitorsLayout = new MonitorsLayout(monitorIDs);
		wrapper.addComponent(monitorsLayout);

		VerticalLayout separator = new VerticalLayout();
		separator.setSizeFull();
		Embedded rightArrow = new Embedded(null, new ThemeResource("img/right_arrow.png"));
		separator.addComponent(rightArrow);
		separator.setComponentAlignment(rightArrow, Alignment.MIDDLE_CENTER);
		wrapper.addComponent(separator);

		ChartPreviewLayout chartPreviewLayout = new ChartPreviewLayout(newUserChart, chartsLayout.getTime(), chartsLayout.getInterval());
		wrapper.addComponent(chartPreviewLayout);
		monitorsLayout.addChartPreview(chartPreviewLayout);

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
				dialogWindow.close();
			}
		});

		Button okButton = new Button(chartButton != null ? "Save Changes" : "Add Chart");
		okButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				try {
					ChartButton newChartButton = new ChartButton(newUserChart);
					newChartButton.setChartsLayout(chartsLayout);
					newChartButton.setEditable(true);
					if (chartButton != null) {
						chartsLayout.replaceComponent(chartButton, newChartButton);
					} else {
						chartsLayout.addComponent(newChartButton);
					}

				} catch (Exception e) {
//					e.printStackTrace();
					Logging.error(e.getMessage());
				}

				dialogWindow.close();
			}
		});
		buttonsBar.addComponent(okButton);
		buttonsBar.setComponentAlignment(okButton, Alignment.MIDDLE_RIGHT);

		VerticalLayout windowLayout = (VerticalLayout) dialogWindow.getContent();
		windowLayout.setSpacing(false);
		windowLayout.setMargin(false);
		windowLayout.addComponent(wrapper);
		windowLayout.addComponent(buttonsBar);

	}

	/**
	 * New user chart.
	 *
	 * @return the user chart
	 */
	public UserChart newUserChart() {

		ClusterComponent clusterComponent = VaadinSession.getCurrent().getAttribute(ClusterComponent.class);

		MonitorRecord monitor = (MonitorRecord) Monitors.getMonitorsList(clusterComponent.getSystemType()).values().toArray()[0];
		ArrayList<String> monitorsForChart = new ArrayList<String>();
		monitorsForChart.add(monitor.getID());
		String chartType = monitor.getChartType();
		if (chartType == null) {
			chartType = UserChart.DEFAULT_CHARTTYPE.name();
		}
		UserChart userChart = new UserChart(monitor.getName(), monitor.getDescription(), monitor.getUnit(), chartType, UserChart.COUNT_15, monitorsForChart);

		return (userChart);

	}

}
