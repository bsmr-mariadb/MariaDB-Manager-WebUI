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
			TaskInfo taskInfo = APIrestful.getGson().fromJson(api.getResult(), TaskInfo.class);
			if (taskInfo != null) {
				this.tasksList = taskInfo.tasksList;
			}
		}

	}
}

class TaskInfoDeserializer implements JsonDeserializer<TaskInfo> {
	public TaskInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		TaskInfo taskInfo = new TaskInfo();

		JsonElement jsonElement = json.getAsJsonObject().get("task");
		if (jsonElement == null || jsonElement.isJsonNull()) {
			taskInfo.setTasksList(null);
		} else {
			JsonArray array = jsonElement.getAsJsonArray();
			int length = array.size();

			ArrayList<TaskRecord> tasksList = new ArrayList<TaskRecord>(length);
			for (int i = 0; i < length; i++) {
				JsonObject backupJson = array.get(i).getAsJsonObject();
				JsonElement element;
				String id = (element = backupJson.get("id")).isJsonNull() ? null : element.getAsString();
				String node = (element = backupJson.get("node")).isJsonNull() ? null : element.getAsString();
				String command = (element = backupJson.get("command")).isJsonNull() ? null : element.getAsString();
				String params = (element = backupJson.get("params")).isJsonNull() ? null : element.getAsString();
				String index = (element = backupJson.get("stepindex")).isJsonNull() ? null : element.getAsString();
				String status = (element = backupJson.get("status")).isJsonNull() ? null : element.getAsString();
				String user = (element = backupJson.get("user")).isJsonNull() ? null : element.getAsString();
				String start = (element = backupJson.get("start")).isJsonNull() ? null : element.getAsString();
				String end = (element = backupJson.get("end")).isJsonNull() ? null : element.getAsString();
				TaskRecord taskRecord = new TaskRecord(id, node, command, params, index, status, user, start, end);
				tasksList.add(taskRecord);
			}
			taskInfo.setTasksList(tasksList);
		}

		return taskInfo;

	}

}
