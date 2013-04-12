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
 * Copyright SkySQL Ab
 */

package com.skysql.consolev;

public class TaskRecord {

	private String id;
	private String node;
	private String command;
	private String params;
	private String index;
	private String status;
	private String userID;
	private String start;
	private String end;

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

	public String getIndex() {
		return index;
	}

	public String getStatus() {
		return status;
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

	public TaskRecord(String id, String node, String command, String params, String index, String status, String userID, String start, String end) {
		this.id = id;
		this.node = node;
		this.command = command;
		this.params = params;
		this.index = index;
		this.status = status;
		this.userID = userID;
		this.start = start;
		this.end = end;
	}

}
