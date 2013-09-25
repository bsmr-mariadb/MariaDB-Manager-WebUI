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

public class ScheduledRecord {

	private String ID;
	private String state;
	private String iCal;
	private String nodeID;
	private String taskID;

	public String getID() {
		return ID;
	}

	public String getState() {
		return state;
	}

	public String getICal() {
		return iCal;
	}

	public String getNodeID() {
		return nodeID;
	}

	public String getTaskID() {
		return taskID;
	}

	public ScheduledRecord(String ID, String state, String iCal, String nodeID, String taskID) {
		this.ID = ID;
		this.state = state;
		this.iCal = iCal;
		this.nodeID = nodeID;
		this.taskID = taskID;
	}

}