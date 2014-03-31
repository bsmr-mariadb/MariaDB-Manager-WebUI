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

import java.io.Serializable;
import java.util.ArrayList;

/**
 * The Class ChartMappings defines what a chart contains.
 */
public class ChartMappings implements Serializable {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private String name;
	private String description;
	private String unit;
	private String type;
	private int points;
	private ArrayList<String> monitorIDs;

	/**
	 * Instantiates a new chart mappings.
	 *
	 * @param name the chart name
	 * @param description the chart description
	 * @param unit the chart unit of measurement
	 * @param type the chart type
	 * @param points how many data points to display
	 * @param monitorIDs the IDs of all monitors in the chart
	 */
	public ChartMappings(String name, String description, String unit, String type, int points, ArrayList<String> monitorIDs) {
		this.name = name;
		this.description = description;
		this.unit = unit;
		this.type = type;
		this.points = points;
		this.monitorIDs = monitorIDs;
	}

	/**
	 * Instantiates a new chart mappings.
	 *
	 * @param oldUserChart the old user chart
	 */
	public ChartMappings(ChartMappings oldUserChart) {
		this.name = oldUserChart.name;
		this.description = oldUserChart.description;
		this.unit = oldUserChart.unit;
		this.type = oldUserChart.type;
		this.points = oldUserChart.points;
		this.monitorIDs = oldUserChart.monitorIDs;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the unit.
	 *
	 * @return the unit
	 */
	public String getUnit() {
		return unit;
	}

	/**
	 * Sets the unit.
	 *
	 * @param unit the new unit
	 */
	public void setUnit(String unit) {
		this.unit = unit;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type.
	 *
	 * @param type the new type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Gets the IDs of all monitor in the chart.
	 *
	 * @return the monitor IDs
	 */
	public ArrayList<String> getMonitorIDs() {
		return monitorIDs;
	}

	/**
	 * Sets the IDs for all monitor in the chart.
	 *
	 * @param monitorIDs the new monitor IDs
	 */
	public void setMonitorIDs(ArrayList<String> monitorIDs) {
		this.monitorIDs = monitorIDs;
	}

	/**
	 * Delete monitor id.
	 *
	 * @param monitorID the monitor id
	 * @return true, if successful
	 */
	public boolean deleteMonitorID(String monitorID) {
		return monitorIDs.remove(monitorID);
	}

	/**
	 * Gets the number of data points for the chart.
	 *
	 * @return the points
	 */
	public int getPoints() {
		return points;
	}

	/**
	 * Sets the number of data points for the chart.
	 *
	 * @param points the new points
	 */
	public void setPoints(int points) {
		this.points = points;
	}

}
