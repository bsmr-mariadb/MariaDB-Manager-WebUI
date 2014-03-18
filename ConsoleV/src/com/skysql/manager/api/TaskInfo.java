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

package com.skysql.manager.api;

import java.util.ArrayList;

import com.google.gson.JsonParseException;
import com.skysql.manager.TaskRecord;
import com.skysql.manager.ui.ErrorDialog;

/**
 * The Class TaskInfo.
 */
public class TaskInfo {

	private ArrayList<TaskRecord> tasksList;
	private String error;

	/**
	 * Gets the tasks list.
	 *
	 * @return the tasks list
	 */
	public ArrayList<TaskRecord> getTasksList() {
		return tasksList;
	}

	/**
	 * Gets the error.
	 *
	 * @return the error
	 */
	public String getError() {
		return error;
	}

	/**
	 * Sets the tasks list.
	 *
	 * @param tasksList the new tasks list
	 */
	public void setTasksList(ArrayList<TaskRecord> tasksList) {
		this.tasksList = tasksList;
	}

	/**
	 * Instantiates a new task info.
	 */
	public TaskInfo() {
	}

	/**
	 * Instantiates a new task info by reading it from the API.
	 *
	 * @param taskID the task id
	 * @param systemID the system id
	 * @param nodeID the node id
	 */
	public TaskInfo(String taskID, String systemID, String nodeID) {

		APIrestful api = new APIrestful();
		boolean success = false;
		if (taskID != null) {
			success = api.get("task/" + taskID);
		} else if (systemID != null && nodeID != null) {
			success = api.get("task", "?systemid=" + systemID + "&nodeid=" + nodeID + "&limit=1000");
		}

		if (success) {
			try {
				TaskInfo taskInfo = APIrestful.getGson().fromJson(api.getResult(), TaskInfo.class);
				this.tasksList = taskInfo.tasksList;
			} catch (NullPointerException e) {
				new ErrorDialog(e, "API did not return expected result for:" + api.errorString());
				throw new RuntimeException("API response");
			} catch (JsonParseException e) {
				new ErrorDialog(e, "JSON parse error in API results for:" + api.errorString());
				throw new RuntimeException("API response");
			}
		} else {
			error = api.getErrors();
		}

	}
}
