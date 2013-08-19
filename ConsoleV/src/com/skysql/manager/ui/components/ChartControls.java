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

package com.skysql.manager.ui.components;

import com.skysql.manager.api.ChartProperties;
import com.vaadin.addon.charts.ChartOptions;
import com.vaadin.addon.charts.model.style.Theme;
import com.vaadin.addon.charts.themes.GrayTheme;
import com.vaadin.addon.charts.themes.GridTheme;
import com.vaadin.addon.charts.themes.HighChartsDefaultTheme;
import com.vaadin.addon.charts.themes.SkiesTheme;
import com.vaadin.addon.charts.themes.VaadinTheme;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;

public class ChartControls extends HorizontalLayout {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private static final Integer INTERVAL_5_MIN = 5 * 60;
	private static final Integer INTERVAL_10_MIN = INTERVAL_5_MIN * 2;
	private static final Integer INTERVAL_30_MIN = INTERVAL_10_MIN * 3;
	private static final Integer INTERVAL_1_HOUR = INTERVAL_30_MIN * 2;
	private static final Integer INTERVAL_6_HOURS = INTERVAL_1_HOUR * 6;
	private static final Integer INTERVAL_12_HOURS = INTERVAL_1_HOUR * 12;
	private static final Integer INTERVAL_1_DAY = INTERVAL_1_HOUR * 24;
	private static final Integer INTERVAL_1_WEEK = INTERVAL_1_DAY * 7;
	private static final Integer INTERVAL_1_MONTH = INTERVAL_1_DAY * 31;
	public static final Integer DEFAULT_INTERVAL = INTERVAL_30_MIN;

	private enum Themes {
		Vaadin, Skies, Grid, Gray, Highcharts;
	}

	public static final String DEFAULT_THEME = Themes.Vaadin.name();

	private NativeSelect selectInterval, selectTheme;
	private ChartProperties chartProperties;

	public ChartControls(ChartProperties chartProps) {
		chartProperties = chartProps;

		setSpacing(true);

		final FormLayout form1 = new FormLayout();
		form1.setSpacing(false);
		form1.setMargin(false);
		addComponent(form1);

		selectInterval = new NativeSelect("Charts Time Span");
		selectInterval.setImmediate(true);

		selectInterval.addItem(INTERVAL_5_MIN);
		selectInterval.addItem(INTERVAL_10_MIN);
		selectInterval.addItem(INTERVAL_30_MIN);
		selectInterval.addItem(INTERVAL_1_HOUR);
		selectInterval.addItem(INTERVAL_6_HOURS);
		selectInterval.addItem(INTERVAL_12_HOURS);
		selectInterval.addItem(INTERVAL_1_DAY);
		selectInterval.addItem(INTERVAL_1_WEEK);
		selectInterval.addItem(INTERVAL_1_MONTH);

		selectInterval.setItemCaption(INTERVAL_5_MIN, "5 Minutes");
		selectInterval.setItemCaption(INTERVAL_10_MIN, "10 Minutes");
		selectInterval.setItemCaption(INTERVAL_30_MIN, "30 Minutes");
		selectInterval.setItemCaption(INTERVAL_1_HOUR, "1 Hour");
		selectInterval.setItemCaption(INTERVAL_6_HOURS, "6 Hours");
		selectInterval.setItemCaption(INTERVAL_12_HOURS, "12 Hours");
		selectInterval.setItemCaption(INTERVAL_1_DAY, "1 Day");
		selectInterval.setItemCaption(INTERVAL_1_WEEK, "1 Week");
		selectInterval.setItemCaption(INTERVAL_1_MONTH, "1 Month");

		selectInterval.select(chartProperties.getTimeSpan());
		selectInterval.setNullSelectionAllowed(false);
		form1.addComponent(selectInterval);

		FormLayout form2 = new FormLayout();
		form2.setSpacing(false);
		form2.setMargin(false);
		addComponent(form2);

		selectTheme = new NativeSelect("Theme");
		selectTheme.setImmediate(true);

		for (Themes theme : Themes.values()) {
			selectTheme.addItem(theme.name());
		}
		String themeName = chartProperties.getTheme();
		if (!Themes.Vaadin.name().equals(themeName)) {
			setTheme(themeName);
		}
		selectTheme.select(themeName);
		selectTheme.setNullSelectionAllowed(false);
		form2.addComponent(selectTheme);

		selectTheme.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {
				String themeName = (String) event.getProperty().getValue();
				setTheme(themeName);
				chartProperties.setTheme(themeName);
			}

		});

	}

	private void setTheme(String themeName) {

		Theme theme = null;
		switch (Themes.valueOf(themeName)) {
		case Vaadin:
			theme = new VaadinTheme();
			break;

		case Skies:
			theme = new SkiesTheme();
			break;

		case Grid:
			theme = new GridTheme();
			break;

		case Gray:
			theme = new GrayTheme();
			break;

		case Highcharts:
			theme = new HighChartsDefaultTheme();
			break;
		}
		ChartOptions.get().setTheme(theme);

	}

	public void addIntervalSelectionListener(ValueChangeListener listener) {
		selectInterval.addValueChangeListener(listener);
	}

}
