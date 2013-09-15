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

package com.skysql.manager;

public class MonitorRecord {

	private String systemType;
	private String ID;
	private String name;
	private String description;
	private String unit;
	private String type;
	private boolean delta;
	private boolean average;
	private String chartType;
	private int interval;
	private String sql;

	public String getSystemType() {
		return systemType;
	}

	public void setSystemType(String systemType) {
		this.systemType = systemType;
	}

	public String getID() {
		return ID;
	}

	public void setID(String ID) {
		this.ID = ID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isDelta() {
		return delta;
	}

	public void setDelta(boolean delta) {
		this.delta = delta;
	}

	public boolean isAverage() {
		return average;
	}

	public void setAverage(boolean average) {
		this.average = average;
	}

	public String getChartType() {
		return chartType;
	}

	public void setChartType(String chartType) {
		this.chartType = chartType;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public MonitorRecord(String systemType) {
		this.systemType = systemType;
	}

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