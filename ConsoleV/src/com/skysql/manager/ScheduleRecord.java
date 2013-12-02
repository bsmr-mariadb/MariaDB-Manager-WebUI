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
 * Copyright 2012-2014 SkySQL Ab
 */

package com.skysql.manager;

public class ScheduleRecord {

	private String id;
	private String command;
	private String systemID;
	private String nodeID;
	private String userID;
	private String params;
	private String iCal;
	private String nextStart;
	private String created;
	private String updated;
	private String state;

	public String getID() {
		return id;
	}

	public String getCommand() {
		return command;
	}

	public String getSystemID() {
		return systemID;
	}

	public String getNodeID() {
		return nodeID;
	}

	public String getUserID() {
		return userID;
	}

	public String getParams() {
		return params;
	}

	public String getICal() {
		return iCal;
	}

	public void setICal(String iCal) {
		this.iCal = iCal;
	}

	public String getNextStart() {
		return nextStart;
	}

	public String getCreated() {
		return created;
	}

	public String getUpdated() {
		return updated;
	}

	public String getState() {
		return state;
	}

	//				ScheduleRecord scheduleRecord = new ScheduleRecord(id, command, systemID, nodeID, userID, parameters, iCal, nextStart, created, updated, state);

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
