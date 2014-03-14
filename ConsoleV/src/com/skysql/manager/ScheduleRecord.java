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
 * The Class ScheduleRecord.
 */
public class ScheduleRecord {

	/** The id. */
	private String id;

	/** The command. */
	private String command;

	/** The system id. */
	private String systemID;

	/** The node id. */
	private String nodeID;

	/** The user id. */
	private String userID;

	/** The params. */
	private String params;

	/** The iCal. */
	private String iCal;

	/** The next scheduled time. */
	private String nextStart;

	/** The time when the schedule was created. */
	private String created;

	/** The time when the schedule was last updated. */
	private String updated;

	/** The state. */
	private String state;

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getID() {
		return id;
	}

	/**
	 * Gets the command.
	 *
	 * @return the command
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * Gets the system id.
	 *
	 * @return the system id
	 */
	public String getSystemID() {
		return systemID;
	}

	/**
	 * Gets the node id.
	 *
	 * @return the node id
	 */
	public String getNodeID() {
		return nodeID;
	}

	/**
	 * Gets the user id.
	 *
	 * @return the user id
	 */
	public String getUserID() {
		return userID;
	}

	/**
	 * Gets the params.
	 *
	 * @return the params
	 */
	public String getParams() {
		return params;
	}

	/**
	 * Gets the i cal.
	 *
	 * @return the i cal
	 */
	public String getICal() {
		return iCal;
	}

	/**
	 * Sets the i cal.
	 *
	 * @param iCal the new i cal
	 */
	public void setICal(String iCal) {
		this.iCal = iCal;
	}

	/**
	 * Gets the next start.
	 *
	 * @return the next start
	 */
	public String getNextStart() {
		return nextStart;
	}

	/**
	 * Gets the created.
	 *
	 * @return the created
	 */
	public String getCreated() {
		return created;
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
	 * Gets the state.
	 *
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	/**
	 * Instantiates a new schedule record.
	 *
	 * @param id the id
	 * @param command the command
	 * @param systemID the system id
	 * @param nodeID the node id
	 * @param userID the user id
	 * @param params the params
	 * @param iCal the i cal
	 * @param nextStart the next start
	 * @param created the created
	 * @param updated the updated
	 * @param state the state
	 */
	public ScheduleRecord(String id, String command, String systemID, String nodeID, String userID, String params, String iCal, String nextStart,
			String created, String updated, String state) {
		this.id = id;
		this.command = command;
		this.systemID = systemID;
		this.nodeID = nodeID;
		this.userID = userID;
		this.params = params;
		this.iCal = iCal;
		this.nextStart = nextStart;
		this.created = created;
		this.updated = updated;
		this.state = state;
	}

}
