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

import com.skysql.manager.ui.components.ComponentButton;

public class ClusterComponent {

	public enum CCType {
		system, node;
	}

	protected String ID;
	protected String name;
	protected CCType type;
	protected String parentID;
	protected String systemType;
	protected String state;
	protected String updated;
	protected ComponentButton button;
	protected MonitorLatest monitorLatest;
	protected String dbUsername;
	protected String dbPassword;
	protected String repUsername;
	protected String repPassword;
	protected String lastMonitored;

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

	public CCType getType() {
		return type;
	}

	public void setType(CCType type) {
		this.type = type;
	}

	public String getParentID() {
		return parentID;
	}

	public void setParentID(String parentID) {
		this.parentID = parentID;
	}

	public String getSystemType() {
		return systemType;
	}

	public void setSystemType(String systemType) {
		this.systemType = systemType;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getUpdated() {
		return updated;
	}

	public void setUpdated(String updated) {
		this.updated = updated;
	}

	public ComponentButton getButton() {
		return button;
	}

	public void setButton(ComponentButton button) {
		this.button = button;
	}

	public MonitorLatest getMonitorLatest() {
		return monitorLatest;
	}

	public void setMonitorLatest(MonitorLatest monitorLatest) {
		this.monitorLatest = monitorLatest;
	}

	public String getDBUsername() {
		return dbUsername;
	}

	public void setDBUsername(String dbUsername) {
		this.dbUsername = dbUsername;
	}

	public String getDBPassword() {
		return dbPassword;
	}

	public void setDBPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}

	public String getRepUsername() {
		return repUsername;
	}

	public void setRepUsername(String repUsername) {
		this.repUsername = repUsername;
	}

	public String getRepPassword() {
		return repPassword;
	}

	public void setRepPassword(String repPassword) {
		this.repPassword = repPassword;
	}

	public String getLastMonitored() {
		return lastMonitored;
	}

	public void setLastMonitored(String lastMonitored) {
		this.lastMonitored = lastMonitored;
	}

}