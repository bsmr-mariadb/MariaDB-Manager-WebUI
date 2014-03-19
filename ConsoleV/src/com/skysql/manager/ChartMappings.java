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

public class ChartMappings implements Serializable {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private String name;
	private String description;
	private String unit;
	private String type;
	private int points;
	private ArrayList<String> monitorIDs;

	public ChartMappings(String name, String description, String unit, String type, int points, ArrayList<String> monitorIDs) {
		this.name = name;
		this.description = description;
		this.unit = unit;
		this.type = type;
		this.points = points;
		this.monitorIDs = monitorIDs;
	}

	public ChartMappings(ChartMappings oldUserChart) {
		this.name = oldUserChart.name;
		this.description = oldUserChart.description;
		this.unit = oldUserChart.unit;
		this.type = oldUserChart.type;
		this.points = oldUserChart.points;
		this.monitorIDs = oldUserChart.monitorIDs;
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

	public ArrayList<String> getMonitorIDs() {
		return monitorIDs;
	}

	public void setMonitorIDs(ArrayList<String> monitorIDs) {
		this.monitorIDs = monitorIDs;
	}

	public boolean deleteMonitorID(String monitorID) {
		return monitorIDs.remove(monitorID);
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

}
