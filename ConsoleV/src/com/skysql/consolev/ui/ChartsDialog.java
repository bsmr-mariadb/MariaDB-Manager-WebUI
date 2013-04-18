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

import java.util.ArrayList;

import com.skysql.consolev.api.UserChart;
import com.skysql.consolev.ui.components.ChartsLayout;
import com.vaadin.addon.charts.Chart;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
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
	final Chart chart;
	final ChartsLayout chartsLayout;
	final boolean isCreate;

	public ChartsDialog(final ChartsLayout chartsLayout, final Chart chart, final boolean isCreate) {

		this.chart = chart;
		this.chartsLayout = chartsLayout;
		this.isCreate = isCreate;

		dialogWindow = new ChartWindow("Monitors to Chart mapping");
		dialogWindow.addCloseListener(this);

		HorizontalLayout wrapper = new HorizontalLayout();
		//wrapper.setWidth("100%");
		wrapper.setMargin(true);

		UI.getCurrent().addWindow(dialogWindow);

		UserChart originalUserChart = (UserChart) chart.getData();
		newUserChart = new UserChart(originalUserChart);

		ArrayList<String> monitorIDs = newUserChart.getMonitorIDs();
		MonitorsLayout monitorsLayout = new MonitorsLayout(monitorIDs);
		wrapper.addComponent(monitorsLayout);

		VerticalLayout separator = new VerticalLayout();
		separator.setSizeFull();
		Embedded rightArrow = new Embedded(null, new ThemeResource("img/right_arrow.png"));
		separator.addComponent(rightArrow);
		separator.setComponentAlignment(rightArrow, Alignment.MIDDLE_CENTER);
		wrapper.addComponent(separator);

		ChartPreviewLayout chartPreviewLayout = new ChartPreviewLayout(newUserChart);
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
				windowClose(null);
			}
		});

		Button okButton = new Button("Save Chart");
		okButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				String monitorID = null;
				try {
					chartsLayout.replaceChart(chart, newUserChart);
					chartsLayout.refresh();

				} catch (Exception e) {
					return;
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

	public void windowClose(CloseEvent e) {
		if (isCreate) {
			UserChart userChart = (UserChart) chart.getData();
			Button deleteButton = userChart.getDeleteButton();
			chartsLayout.removeComponent(deleteButton);
			chartsLayout.removeComponent(chart);
		}
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
