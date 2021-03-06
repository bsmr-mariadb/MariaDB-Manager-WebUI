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

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.skysql.manager.TaskRecord;

/***
{"total":"2","tasks":[
{"taskid":"1","systemid":"42","nodeid":"4","privateip":"3.5.7.9","username":"fred","command":"restart","parameters":"","steps":"stop,start","started":"Mon, 02 Sep 2013 09:07:05 +0100","pid":"32494","completed":"Mon, 02 Sep 2013 09:07:05 +0100","stepindex":"0","state":"running"},
{"taskid":"2","systemid":"42","nodeid":"4","privateip":"3.5.7.9","username":"fred","command":"restart","parameters":"","steps":"stop,start","started":"Mon, 02 Sep 2013 09:33:27 +0100","pid":"2136","completed":"Mon, 02 Sep 2013 09:33:27 +0100","stepindex":"0","state":"running"}]}
***/

class TaskInfoDeserializer implements JsonDeserializer<TaskInfo> {

	/* (non-Javadoc)
	 * @see com.google.gson.JsonDeserializer#deserialize(com.google.gson.JsonElement, java.lang.reflect.Type, com.google.gson.JsonDeserializationContext)
	 */
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

			String id = (element = taskObject.get("taskid")).isJsonNull() ? null : element.getAsString();
			//String systemid = (element = taskObject.get("systemid")).isJsonNull() ? null : element.getAsString();
			String node = (element = taskObject.get("nodeid")).isJsonNull() ? null : element.getAsString();
			String privateIP = (element = taskObject.get("privateip")).isJsonNull() ? null : element.getAsString();
			String user = (element = taskObject.get("username")).isJsonNull() ? null : element.getAsString();
			String command = (element = taskObject.get("command")).isJsonNull() ? null : element.getAsString();
			String params = ((element = taskObject.get("parameters")).isJsonNull()) ? null : element.getAsJsonObject().toString().replaceAll("[\\{\\}\"]*", "");
			String steps = (element = taskObject.get("steps")).isJsonNull() ? null : element.getAsString();
			String start = (element = taskObject.get("started")).isJsonNull() ? null : element.getAsString();
			String pid = (element = taskObject.get("pid")).isJsonNull() ? null : element.getAsString();
			String end = (element = taskObject.get("completed")).isJsonNull() ? null : element.getAsString();
			String index = (element = taskObject.get("stepindex")).isJsonNull() ? null : element.getAsString();
			String state = (element = taskObject.get("state")).isJsonNull() ? null : element.getAsString();
			//String finished = (element = taskObject.get("finished")).isJsonNull() ? null : element.getAsString();
			String error = (element = taskObject.get("errormessage")).isJsonNull() ? null : element.getAsString();
			TaskRecord taskRecord = new TaskRecord(id, node, command, params, steps, pid, privateIP, index, state, null, user, start, end, error);
			tasksList.add(taskRecord);
		}

		return taskInfo;

	}
}
