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

package com.skysql.manager.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import com.google.gson.JsonParseException;
import com.skysql.manager.TaskRecord;
import com.skysql.manager.ui.ErrorDialog;

public class TaskRun {
	private TaskRecord taskRecord;
	private String error;

	public TaskRecord getTaskRecord() {
		return taskRecord;
	}

	public String getError() {
		return error;
	}

	protected void setTaskRecord(TaskRecord taskRecord) {
		this.taskRecord = taskRecord;
	}

	public TaskRun() {
	}

	public TaskRun(String systemID, String nodeID, String userID, String command, String params, String state) {

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
				regParam.append("&parameters=" + URLEncoder.encode(params, "UTF-8"));
			}
			success = api.post("command/" + command, regParam.toString());

		} catch (UnsupportedEncodingException e) {
			new ErrorDialog(e, "Error encoding API request");
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
}
