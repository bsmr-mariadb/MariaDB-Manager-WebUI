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

/**
 * The Class MonitorRecord.
 */
public class MonitorRecord {

	/** The system type as returned by the API ("galera", etc). */
	private String systemType;

	/** The monitor key as returned by the API, not to be confused with the API's monitor ID which we don't use */
	private String ID;

	/** The monitor name as visible to the user. */
	private String name;

	/** The description of the monitor as provided by the user. */
	private String description;

	/** The unit of measurement for the data returned by the monitor, used as the Y axis label. */
	private String unit;

	/** The monitor type ("SQL", etc.) */
	private String type;

	/** True if this monitor records delta. */
	private boolean delta;

	/** True if this monitor is an average. */
	private boolean average;

	/** The chart type (line, area, etc.) as per Vaadin Charts */
	private String chartType;

	/** The number of seconds in each time interval, as per API for Monitor Data */
	private int interval;

	/** The sql statement for the monitor. */
	private String sql;

	/**
	 * Gets the system type.
	 *
	 * @return the system type
	 */
	public String getSystemType() {
		return systemType;
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getID() {
		return ID;
	}

	/**
	 * Sets the id.
	 *
	 * @param ID the new id
	 */
	public void setID(String ID) {
		this.ID = ID;
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
	 * Checks if is delta.
	 *
	 * @return true, if is delta
	 */
	public boolean isDelta() {
		return delta;
	}

	/**
	 * Sets the delta.
	 *
	 * @param delta the new delta
	 */
	public void setDelta(boolean delta) {
		this.delta = delta;
	}

	/**
	 * Checks if is average.
	 *
	 * @return true, if is average
	 */
	public boolean isAverage() {
		return average;
	}

	/**
	 * Sets the average.
	 *
	 * @param average the new average
	 */
	public void setAverage(boolean average) {
		this.average = average;
	}

	/**
	 * Gets the chart type.
	 *
	 * @return the chart type
	 */
	public String getChartType() {
		return chartType;
	}

	/**
	 * Sets the chart type.
	 *
	 * @param chartType the new chart type
	 */
	public void setChartType(String chartType) {
		this.chartType = chartType;
	}

	/**
	 * Gets the interval.
	 *
	 * @return the interval
	 */
	public int getInterval() {
		return interval;
	}

	/**
	 * Sets the interval.
	 *
	 * @param interval the new interval
	 */
	public void setInterval(int interval) {
		this.interval = interval;
	}

	/**
	 * Gets the sql.
	 *
	 * @return the sql
	 */
	public String getSql() {
		return sql;
	}

	/**
	 * Sets the sql.
	 *
	 * @param sql the new sql
	 */
	public void setSql(String sql) {
		this.sql = sql;
	}

	/**
	 * Instantiates a new monitor record.
	 *
	 * @param systemType the system type
	 */
	public MonitorRecord(String systemType) {
		this.systemType = systemType;
	}

	/**
	 * Instantiates a new monitor record.
	 *
	 * @param systemType the system type
	 * @param ID the id
	 * @param name the name
	 * @param description the description
	 * @param unit the unit
	 * @param type the type
	 * @param delta the delta
	 * @param average the average
	 * @param chartType the chart type
	 * @param interval the interval
	 * @param sql the sql
	 */
	public MonitorRecord(String systemType, String ID, String name, String description, String unit, String type, boolean delta, boolean average,
			String chartType, int interval, String sql) {
		this.systemType = systemType;
		this.ID = ID;
		this.name = name;
		this.description = description;
		this.unit = unit;
		this.type = type;
		this.delta = delta;
		this.average = average;
		this.chartType = chartType;
		this.interval = interval;
		this.sql = sql;

	}

}