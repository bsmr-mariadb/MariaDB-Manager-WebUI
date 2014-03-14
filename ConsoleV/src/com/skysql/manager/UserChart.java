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

package com.skysql.manager;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The Class UserChart.
 */
public class UserChart extends ChartMappings {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	/** Constants for the number of data points in a chart */
	public static final Integer COUNT_15 = 15;
	public static final Integer COUNT_30 = 30;
	public static final Integer COUNT_45 = 45;
	public static final Integer COUNT_60 = 60;

	/**
	 * Chart points.
	 *
	 * @return the integer[]
	 */
	public static Integer[] chartPoints() {
		Integer[] array = { COUNT_15, COUNT_30, COUNT_45, COUNT_60 };
		return array;
	}

	/**
	 * The Enum ChartType.
	 */
	public enum ChartType {

		LineChart, AreaChart;
	}

	/** The Constant DEFAULT_CHARTTYPE. */
	public static final ChartType DEFAULT_CHARTTYPE = ChartType.LineChart;

	/** The monitors data. */
	private HashMap<String, Object> monitorsData;

	/**
	 * Instantiates a new user chart.
	 *
	 * @param name the name
	 * @param description the description
	 * @param unit the unit
	 * @param type the type
	 * @param points the points
	 * @param monitorIDs the monitor IDs
	 */
	public UserChart(String name, String description, String unit, String type, int points, ArrayList<String> monitorIDs) {
		super(name, description, unit, type, points, monitorIDs);
		this.monitorsData = new HashMap<String, Object>();
	}

	/**
	 * Instantiates a new user chart.
	 *
	 * @param oldUserChart the old user chart
	 */
	public UserChart(UserChart oldUserChart) {
		super(oldUserChart);
		this.monitorsData = oldUserChart.monitorsData;
	}

	/**
	 * Instantiates a new user chart.
	 *
	 * @param chartMappings the chart mappings
	 */
	public UserChart(ChartMappings chartMappings) {
		super(chartMappings);
		this.monitorsData = new HashMap<String, Object>();
	}

	/**
	 * Gets the monitor data.
	 *
	 * @param id the id
	 * @return the monitor data
	 */
	public Object getMonitorData(String id) {
		return monitorsData.get(id);
	}

	/**
	 * Sets the monitor data.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public void setMonitorData(String key, Object value) {
		this.monitorsData.put(key, value);
	}

	/**
	 * Clear monitor data.
	 */
	public void clearMonitorData() {
		this.monitorsData = new HashMap<String, Object>();
	}

}
