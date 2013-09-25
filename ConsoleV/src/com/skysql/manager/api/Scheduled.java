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
import java.util.LinkedHashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.skysql.manager.ScheduledRecord;
import com.skysql.manager.ui.ErrorDialog;

public class Scheduled {

	LinkedHashMap<String, ScheduledRecord> scheduledList;

	public LinkedHashMap<String, ScheduledRecord> getScheduledList() {
		return scheduledList;
	}

	public void setScheduledList(LinkedHashMap<String, ScheduledRecord> scheduledList) {
		this.scheduledList = scheduledList;
	}

	public LinkedHashMap<String, ScheduledRecord> getScheduledForNode(String nodeID) {
		LinkedHashMap<String, ScheduledRecord> scheduledForNode = new LinkedHashMap<String, ScheduledRecord>();

		for (String key : scheduledList.keySet()) {
			ScheduledRecord record = scheduledList.get(key);
			if (record.getNodeID().equals(nodeID)) {
				scheduledForNode.put(key, record);
			}
		}
		return scheduledForNode;
	}

	public Scheduled() {

	}

	public Scheduled(String system, String date) {

		APIstubs api = new APIstubs();
		// TODO: incorporate or eliminate date parameter
		if (api.get("system/" + system + "/scheduled")) {
			try {
				Scheduled scheduled = APIstubs.getGson().fromJson(api.getResult(), Scheduled.class);
				this.scheduledList = scheduled.scheduledList;
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

// {"total":"0","scheduled":null}

class ScheduledDeserializer implements JsonDeserializer<Scheduled> {
	public Scheduled deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException, NullPointerException {

		Scheduled scheduled = new Scheduled();

		JsonElement jsonElement = json.getAsJsonObject().get("scheduled");
		if (jsonElement.isJsonNull()) {
			scheduled.setScheduledList(null);
		} else {

			JsonArray array = jsonElement.getAsJsonArray();
			int length = array.size();

			LinkedHashMap<String, ScheduledRecord> scheduledList = new LinkedHashMap<String, ScheduledRecord>(length);
			for (int i = 0; i < length; i++) {
				JsonObject backupJson = array.get(i).getAsJsonObject();
				JsonElement element;
				String id = (element = backupJson.get("id")).isJsonNull() ? null : element.getAsString();
				String state = (element = backupJson.get("state")).isJsonNull() ? null : element.getAsString();
				String iCal = (element = backupJson.get("ical")).isJsonNull() ? null : element.getAsString();
				String nodeID = (element = backupJson.get("nodeid")).isJsonNull() ? null : element.getAsString();
				String taskID = (element = backupJson.get("taskid")).isJsonNull() ? null : element.getAsString();
				ScheduledRecord scheduledRecord = new ScheduledRecord(id, state, iCal, nodeID, taskID);
				scheduledList.put(id, scheduledRecord);
			}
			scheduled.setScheduledList(scheduledList);
		}
		return scheduled;

	}

}
