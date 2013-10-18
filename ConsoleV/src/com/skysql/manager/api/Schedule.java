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
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.skysql.manager.ScheduleRecord;
import com.skysql.manager.ui.ErrorDialog;

public class Schedule {

	LinkedHashMap<String, ScheduleRecord> scheduleList;
	String error;

	public LinkedHashMap<String, ScheduleRecord> getScheduleList() {
		return scheduleList;
	}

	public void setScheduleList(LinkedHashMap<String, ScheduleRecord> scheduleList) {
		this.scheduleList = scheduleList;
	}

	public LinkedHashMap<String, ScheduleRecord> getScheduleForNode(String nodeID) {
		LinkedHashMap<String, ScheduleRecord> scheduleForNode = new LinkedHashMap<String, ScheduleRecord>();

		for (String key : scheduleList.keySet()) {
			ScheduleRecord record = scheduleList.get(key);
			if (record.getNodeID().equals(nodeID)) {
				scheduleForNode.put(key, record);
			}
		}
		return scheduleForNode;
	}

	public Schedule() {

	}

	public Schedule(String systemID, String nodeID, String userID, String command, String params, String state, String ical) {

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
			regParam.append("&icalentry=" + URLEncoder.encode(ical, "UTF-8"));

			success = api.post("command/" + command, regParam.toString());

		} catch (UnsupportedEncodingException e) {
			new ErrorDialog(e, "Error encoding API request");
			throw new RuntimeException("Error encoding API request");
		}

		if (success) {
			try {
				Schedule schedule = APIrestful.getGson().fromJson(api.getResult(), Schedule.class);
				this.scheduleList = schedule.scheduleList;

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

	public Schedule(String system, String date) {

		APIrestful api = new APIrestful();
		// TODO: incorporate or eliminate date parameter
		if (api.get("schedule", "?systemid=" + system)) {
			try {
				Schedule schedule = APIrestful.getGson().fromJson(api.getResult(), Schedule.class);
				this.scheduleList = schedule.scheduleList;
			} catch (NullPointerException e) {
				new ErrorDialog(e, "API did not return expected result for:" + api.errorString());
				throw new RuntimeException("API response");
			} catch (JsonParseException e) {
				new ErrorDialog(e, "JSON parse error in API results for:" + api.errorString());
				throw new RuntimeException("API response");
			}
		}
	}

	public static synchronized boolean delete(String id) {

		APIrestful api = new APIrestful();
		if (api.delete("schedule/" + id)) {
			return true;
		}

		return false;
	}

	public static synchronized boolean update(String scheduleID, String iCal) {

		boolean success = false;
		try {
			APIrestful api = new APIrestful();
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("icalentry", iCal);
			success = api.put("schedule/" + scheduleID, jsonParam.toString());
		} catch (JSONException e) {
			new ErrorDialog(e, "Error encoding API request");
			throw new RuntimeException("Error encoding API request");
		}

		return success;
	}

}

// {"total":"0","scheduled":null}
// {"schedule":{"scheduleid":"3","command":"backup","systemid":"1","nodeid":"1","username":"admin","level":0,"parameters":"Full","icalentry":"BEGIN:VEVENT\r\nDTSTAMP:20131016T131512Z\r\nDTSTART:20131016T180000Z\r\nDTEND:20131016T190000Z\r\nSUMMARY:Backup\r\nRRULE:FREQ=WEEKLY\r\nEND:VEVENT","nextstart":"Wed, 16 Oct 2013 18:00:00 +0000","atjobnumber":"2","created":"","updated":"Wed, 16 Oct 2013 13:15:12 +0000","state":"scheduled"},"warnings":["Caching directory \/usr\/local\/skysql\/cache\/api is not writeable, cannot write cache, please check existence, permissions, SELinux"]}

class ScheduleDeserializer implements JsonDeserializer<Schedule> {
	public Schedule deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException, NullPointerException {

		Schedule schedule = new Schedule();

		JsonArray array = null;

		int length = 0;
		if (json.getAsJsonObject().has("schedules")) {
			array = json.getAsJsonObject().get("schedules").getAsJsonArray();
			length = array.size();
		} else if (json.getAsJsonObject().has("schedule")) {
			length = 1;
		} else {
			return null;
		}

		LinkedHashMap<String, ScheduleRecord> scheduleList = new LinkedHashMap<String, ScheduleRecord>(length);
		schedule.setScheduleList(scheduleList);

		for (int i = 0; i < length; i++) {
			JsonObject scheduleObject = (array != null) ? array.get(i).getAsJsonObject() : json.getAsJsonObject().get("schedule").getAsJsonObject();
			JsonElement element;

			String id = (element = scheduleObject.get("scheduleid")).isJsonNull() ? null : element.getAsString();
			String command = (element = scheduleObject.get("command")).isJsonNull() ? null : element.getAsString();
			String systemID = (element = scheduleObject.get("systemid")).isJsonNull() ? null : element.getAsString();
			String nodeID = (element = scheduleObject.get("nodeid")).isJsonNull() ? null : element.getAsString();
			String userID = (element = scheduleObject.get("username")).isJsonNull() ? null : element.getAsString();
			String parameters = (element = scheduleObject.get("parameters")).isJsonNull() ? null : element.getAsString();
			String iCal = (element = scheduleObject.get("icalentry")).isJsonNull() ? null : element.getAsString();
			String nextStart = (element = scheduleObject.get("nextstart")).isJsonNull() ? null : element.getAsString();
			String created = (element = scheduleObject.get("created")).isJsonNull() ? null : element.getAsString();
			String updated = (element = scheduleObject.get("updated")).isJsonNull() ? null : element.getAsString();
			String state = (element = scheduleObject.get("state")) == null || element.isJsonNull() ? null : element.getAsString();
			ScheduleRecord scheduleRecord = new ScheduleRecord(id, command, systemID, nodeID, userID, parameters, iCal, nextStart, created, updated, state);
			scheduleList.put(id, scheduleRecord);
		}

		return schedule;

	}
}
