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

/**
 * The Class ClusterComponent represent a component of the cluster (currently System or Node)
 */
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
	 * Gets the type.
	 *
	 * @return the type
	 */
	public CCType getType() {
		return type;
	}

	/**
	 * Sets the type.
	 *
	 * @param type the new type
	 */
	public void setType(CCType type) {
		this.type = type;
	}

	/**
	 * Gets the parent id.
	 *
	 * @return the parent id
	 */
	public String getParentID() {
		return parentID;
	}

	/**
	 * Sets the parent id.
	 *
	 * @param parentID the new parent id
	 */
	public void setParentID(String parentID) {
		this.parentID = parentID;
	}

	/**
	 * Gets the system type.
	 *
	 * @return the system type
	 */
	public String getSystemType() {
		return systemType;
	}

	/**
	 * Sets the system type.
	 *
	 * @param systemType the new system type
	 */
	public void setSystemType(String systemType) {
		this.systemType = systemType;
	}

	/**
	 * Gets the state.
	 *
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	/**
	 * Sets the state.
	 *
	 * @param state the new state
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * Gets the updated.
	 *
	 * @return the updated
	 */
	public String getUpdated() {
		return updated;
	}

	/**
	 * Sets the updated.
	 *
	 * @param updated the new updated
	 */
	public void setUpdated(String updated) {
		this.updated = updated;
	}

	/**
	 * Gets the button.
	 *
	 * @return the button
	 */
	public ComponentButton getButton() {
		return button;
	}

	/**
	 * Sets the button.
	 *
	 * @param button the new button
	 */
	public void setButton(ComponentButton button) {
		this.button = button;
	}

	/**
	 * Gets the monitor latest.
	 *
	 * @return the monitor latest
	 */
	public MonitorLatest getMonitorLatest() {
		return monitorLatest;
	}

	/**
	 * Sets the monitor latest.
	 *
	 * @param monitorLatest the new monitor latest
	 */
	public void setMonitorLatest(MonitorLatest monitorLatest) {
		this.monitorLatest = monitorLatest;
	}

	/**
	 * Gets the DB username.
	 *
	 * @return the DB username
	 */
	public String getDBUsername() {
		return dbUsername;
	}

	/**
	 * Sets the DB username.
	 *
	 * @param dbUsername the new DB username
	 */
	public void setDBUsername(String dbUsername) {
		this.dbUsername = dbUsername;
	}

	/**
	 * Gets the DB password.
	 *
	 * @return the DB password
	 */
	public String getDBPassword() {
		return dbPassword;
	}

	/**
	 * Sets the DB password.
	 *
	 * @param dbPassword the new DB password
	 */
	public void setDBPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}

	/**
	 * Gets the rep username.
	 *
	 * @return the rep username
	 */
	public String getRepUsername() {
		return repUsername;
	}

	/**
	 * Sets the rep username.
	 *
	 * @param repUsername the new rep username
	 */
	public void setRepUsername(String repUsername) {
		this.repUsername = repUsername;
	}

	/**
	 * Gets the rep password.
	 *
	 * @return the rep password
	 */
	public String getRepPassword() {
		return repPassword;
	}

	/**
	 * Sets the rep password.
	 *
	 * @param repPassword the new rep password
	 */
	public void setRepPassword(String repPassword) {
		this.repPassword = repPassword;
	}

	/**
	 * Gets the last monitored.
	 *
	 * @return the last monitored
	 */
	public String getLastMonitored() {
		return lastMonitored;
	}

	/**
	 * Sets the last monitored.
	 *
	 * @param lastMonitored the new last monitored
	 */
	public void setLastMonitored(String lastMonitored) {
		this.lastMonitored = lastMonitored;
	}

}