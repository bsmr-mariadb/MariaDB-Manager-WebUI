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

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.skysql.manager.TaskRecord;
import com.skysql.manager.ui.ErrorDialog;

public class TaskInfo {

	ArrayList<TaskRecord> tasksList;

	public ArrayList<TaskRecord> getTasksList() {
		return tasksList;
	}

	public void setTasksList(ArrayList<TaskRecord> tasksList) {
		this.tasksList = tasksList;
	}

	public TaskInfo() {
	}

	public TaskInfo(String taskID, String node) {

		APIrestful api = new APIrestful();
		boolean success = false;
		if (taskID != null) {
			success = api.get("task/" + taskID);
		} else if (node != null) {
			success = api.get("task", "?nodeid=" + node);
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
		}

	}
}

class TaskInfoDeserializer implements JsonDeserializer<TaskInfo> {
	public TaskInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException, NullPointerException {
		TaskInfo taskInfo = new TaskInfo();

		JsonArray array = null;

		int length = 0;
		if (json.getAsJsonObject().has("tasks")) {
			array = json.getAsJsonObject().get("tasks").getAsJsonArray();
			length = array.size();
		} else if (json.getAsJsonObject().has("task")) {
			length = 1;
		} else {
			return null;
		}

		ArrayList<TaskRecord> tasksList = new ArrayList<TaskRecord>(length);
		taskInfo.setTasksList(tasksList);

		for (int i = 0; i < length; i++) {
			JsonObject taskObject = (array != null) ? array.get(i).getAsJsonObject() : json.getAsJsonObject().get("task").getAsJsonObject();
			JsonElement element;

			String id = (element = taskObject.get("id")).isJsonNull() ? null : element.getAsString();
			String node = (element = taskObject.get("nodeid")).isJsonNull() ? null : element.getAsString();
			String command = (element = taskObject.get("command")).isJsonNull() ? null : element.getAsString();
			String params = ((element = taskObject.get("parameters")) == null || element.isJsonNull()) ? null : element.getAsString();
			String index = (element = taskObject.get("stepindex")).isJsonNull() ? null : element.getAsString();
			String status = (element = taskObject.get("state")).isJsonNull() ? null : element.getAsString();
			String user = (element = taskObject.get("username")).isJsonNull() ? null : element.getAsString();
			String start = (element = taskObject.get("start")).isJsonNull() ? null : element.getAsString();
			String end = (element = taskObject.get("completed")).isJsonNull() ? null : element.getAsString();
			TaskRecord taskRecord = new TaskRecord(id, node, command, params, index, status, user, start, end);
			tasksList.add(taskRecord);
		}

		return taskInfo;

	}

}
