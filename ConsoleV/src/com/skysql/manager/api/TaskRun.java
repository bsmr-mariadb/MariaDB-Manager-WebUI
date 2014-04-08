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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;

import com.google.gson.JsonParseException;
import com.skysql.manager.TaskRecord;
import com.skysql.manager.ui.ErrorDialog;

/**
 * The Class TaskRun.
 */
public class TaskRun {

	private TaskRecord taskRecord;
	private String error;

	/**
	 * Gets the task record.
	 *
	 * @return the task record
	 */
	public TaskRecord getTaskRecord() {
		return taskRecord;
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
	 * Sets the task record.
	 *
	 * @param taskRecord the new task record
	 */
	protected void setTaskRecord(TaskRecord taskRecord) {
		this.taskRecord = taskRecord;
	}

	/**
	 * Instantiates a new task run.
	 */
	public TaskRun() {
	}

	/**
	 * Creates (runs) a task (command) in the API.
	 *
	 * @param systemID the system id
	 * @param nodeID the node id
	 * @param userID the user id
	 * @param command the command
	 * @param params the params map
	 * @param state the state
	 */
	public TaskRun(String systemID, String nodeID, String userID, String command, Map<String, String> params, String state) {

		APIrestful api = new APIrestful();

		boolean success = false;
		try {
			StringBuffer regParam = new StringBuffer();
			regParam.append("systemid=" + URLEncoder.encode(systemID, "UTF-8"));
			regParam.append("&nodeid=" + URLEncoder.encode(nodeID, "UTF-8"));
			regParam.append("&username=" + URLEncoder.encode(userID, "UTF-8"));
			if (state != null) {
				regParam.append("&state=" + URLEncoder.encode(state, "UTF-8"));
			}
			if (params != null) {
				for (Map.Entry<String, String> entry : params.entrySet()) {
					regParam.append("&" + entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
				}
			}
			success = api.post("command/" + command, regParam.toString());

		} catch (UnsupportedEncodingException e) {
			new ErrorDialog(e, "Error encoding API request");
			throw new RuntimeException("Error encoding API request");
		}

		if (success) {
			try {
				TaskInfo taskInfo = APIrestful.getGson().fromJson(api.getResult(), TaskInfo.class);
				ArrayList<TaskRecord> tasksList = taskInfo.getTasksList();
				if (tasksList != null && !tasksList.isEmpty()) {
					this.taskRecord = tasksList.get(0);
				}
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

	/**
	 * Delete a task from the API.
	 *
	 * @param ID the id
	 * @return true, if successful
	 */
	public static boolean delete(String ID) {

		APIrestful api = new APIrestful();
		if (api.delete("task/" + ID)) {
			WriteResponse writeResponse = APIrestful.getGson().fromJson(api.getResult(), WriteResponse.class);
			if (writeResponse != null && writeResponse.getDeleteCount() > 0) {
				return true;
			}
		}
		return false;
	}

}
