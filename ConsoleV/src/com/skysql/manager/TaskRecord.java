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

public class TaskRecord {

	private String id;
	private String node;
	private String command;
	private String params;
	private String steps;
	private String pid;
	private String privateIP;
	private String index;
	private String state;
	private String finished;
	private String userID;
	private String start;
	private String end;
	private String error;

	public String getID() {
		return id;
	}

	public String getNode() {
		return node;
	}

	public String getCommand() {
		return command;
	}

	public String getParams() {
		return params;
	}

	public String getSteps() {
		return steps;
	}

	public String getPID() {
		return pid;
	}

	public String getPrivateIP() {
		return privateIP;
	}

	public String getIndex() {
		return index;
	}

	public String getState() {
		return state;
	}

	public String getFinished() {
		return finished;
	}

	public String getUserID() {
		return userID;
	}

	public String getStart() {
		return start;
	}

	public String getEnd() {
		return end;
	}

	public String getError() {
		return error;
	}

	public TaskRecord(String id, String node, String command, String params, String steps, String pid, String privateIP, String index, String state,
			String finished, String userID, String start, String end, String error) {
		this.id = id;
		this.node = node;
		this.command = command;
		this.params = params;
		this.steps = steps;
		this.pid = pid;
		this.privateIP = privateIP;
		this.index = index;
		this.state = state;
		this.finished = finished;
		this.userID = userID;
		this.start = start;
		this.end = end;
		this.error = error;
	}

}
