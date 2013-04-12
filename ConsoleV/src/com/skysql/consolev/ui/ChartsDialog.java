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

import java.util.ArrayList;

import com.skysql.consolev.api.UserChart;
import com.skysql.consolev.ui.components.ChartsLayout;
import com.vaadin.addon.charts.Chart;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;

public class ChartsDialog implements Window.CloseListener {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	Window dialogWindow;
	Button openButton;
	Button closebutton;
	UserChart newUserChart;
	Chart chart;
	ChartsLayout chartsLayout;

	public ChartsDialog(ChartsLayout chartsLayout, Chart chart) {

		this.chart = chart;
		this.chartsLayout = chartsLayout;

		dialogWindow = new ChartWindow("Monitors to Chart mapping");
		dialogWindow.addCloseListener(this);
		HorizontalLayout windowLayout = new HorizontalLayout();
		dialogWindow.setContent(windowLayout);

		UI.getCurrent().addWindow(dialogWindow);

		UserChart originalUserChart = (UserChart) chart.getData();
		newUserChart = new UserChart(originalUserChart);

		ArrayList<String> monitorIDs = newUserChart.getMonitorIDs();
		MonitorsLayout monitorsLayout = new MonitorsLayout(monitorIDs);
		windowLayout.addComponent(monitorsLayout);

		VerticalLayout separator = new VerticalLayout();
		separator.setSizeFull();
		Embedded rightArrow = new Embedded(null, new ThemeResource("img/right_arrow.png"));
		separator.addComponent(rightArrow);
		separator.setComponentAlignment(rightArrow, Alignment.MIDDLE_CENTER);
		windowLayout.addComponent(separator);

		ChartPreviewLayout chartPreviewLayout = new ChartPreviewLayout(newUserChart);
		windowLayout.addComponent(chartPreviewLayout);
		monitorsLayout.addChartPreview(chartPreviewLayout);
	}

	/** Save data when window is closed */
	public void windowClose(CloseEvent e) {
		chartsLayout.replaceChart(chart, newUserChart);
		chartsLayout.refresh();
		dialogWindow.close();
	}
}

class ChartWindow extends Window {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	public ChartWindow(String caption) {
		setModal(true);
		setWidth("775px");
		center();
		setCaption(caption);
		VerticalLayout layout = new VerticalLayout();
		setContent(layout);
		layout.setSpacing(true);
		layout.setMargin(true);
	}
}
