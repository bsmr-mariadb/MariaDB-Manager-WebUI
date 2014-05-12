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
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

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

/**
 * The Class Schedule.
 */
public class Schedule {

	private LinkedHashMap<String, ScheduleRecord> scheduleList;
	private String error;

	/**
	 * Gets the schedule list.
	 *
	 * @return the schedule list
	 */
	public LinkedHashMap<String, ScheduleRecord> getScheduleList() {
		return scheduleList;
	}

	/**
	 * Sets the schedule list.
	 *
	 * @param scheduleList the schedule list
	 */
	public void setScheduleList(LinkedHashMap<String, ScheduleRecord> scheduleList) {
		this.scheduleList = scheduleList;
	}

	/**
	 * Gets the schedule list for a node.
	 *
	 * @param nodeID the node id
	 * @return the schedule list for node
	 */
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

	/**
	 * Instantiates a new schedule.
	 */
	public Schedule() {

	}

	/**
	 * Instantiates a new schedule.
	 *
	 * @param systemID the system id
	 * @param nodeID the node id
	 * @param userID the user id
	 * @param command the command
	 * @param params the params
	 * @param state the state
	 * @param ical the ical
	 */
	public Schedule(String systemID, String nodeID, String userID, String command, Map<String, String> params, String state, String ical) {

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

	/**
	 * Instantiates a new schedule.
	 *
	 * @param system the system
	 * @param date the date
	 */
	public Schedule(String system, String date) {

		APIrestful api = new APIrestful();
		// TODO: incorporate or eliminate date parameter
		if (api.get("schedule" + (date != null ? "/" + date : ""), "?systemid=" + system)) {
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

	/**
	 * Delete a schedule from the API.
	 *
	 * @param id the id
	 * @return true, if successful
	 */
	public static synchronized boolean delete(String id) {

		APIrestful api = new APIrestful();
		if (api.delete("schedule/" + id)) {
			return true;
		}

		return false;
	}

	/**
	 * Update a schedule in the API.
	 *
	 * @param scheduleID the schedule id
	 * @param iCal the i cal
	 * @return true, if successful
	 */
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
// {"total":"3","schedules":[{"scheduleid":"1","systemid":"1","nodeid":"1","username":"admin","command":"backup","parameters":{"type":"1"},"icalentry":"BEGIN:VEVENT\r\nDTSTAMP:20140509T151034Z\r\nDTSTART:20140509T151205Z\r\nDTEND:20140509T151205Z\r\nSUMMARY:Backup\r\nDESCRIPTION:New backup event\r\nEND:VEVENT","nextstart":"Fri, 09 May 2014 15:12:05 +0000","atjobnumber":"0","created":"Fri, 09 May 2014 15:10:34 +0000","updated":"Fri, 09 May 2014 15:10:34 +0000"},{"scheduleid":"2","systemid":"1","nodeid":"1","username":"admin","command":"backup","parameters":{"type":"1"},"icalentry":"BEGIN:VEVENT\r\nDTSTAMP:20140509T151205Z\r\nDTSTART:20140509T151555Z\r\nDTEND:20140509T151555Z\r\nSUMMARY:Backup\r\nDESCRIPTION:New backup event\r\nEND:VEVENT","nextstart":"Fri, 09 May 2014 15:15:55 +0000","atjobnumber":"0","created":"Fri, 09 May 2014 15:12:05 +0000","updated":"Fri, 09 May 2014 15:12:05 +0000"},{"scheduleid":"3","systemid":"1","nodeid":"3","username":"admin","command":"backup","parameters":{"type":"1"},"icalentry":"BEGIN:VEVENT\r\nDTSTAMP:20140509T151348Z\r\nDTSTART:20140509T151538Z\r\nDTEND:20140509T151538Z\r\nSUMMARY:Backup\r\nDESCRIPTION:New backup event\r\nEND:VEVENT","nextstart":"Fri, 09 May 2014 15:15:38 +0000","atjobnumber":"0","created":"Fri, 09 May 2014 15:13:48 +0000","updated":"Fri, 09 May 2014 15:13:48 +0000"}]}

/**
 * The Class ScheduleDeserializer.
 */
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
			String params = ((element = scheduleObject.get("parameters")).isJsonNull()) ? null : element.getAsJsonObject().toString()
					.replaceAll("[\\{\\}\"]*", "");
			String iCal = (element = scheduleObject.get("icalentry")).isJsonNull() ? null : element.getAsString();
			String nextStart = (element = scheduleObject.get("nextstart")).isJsonNull() ? null : element.getAsString();
			String created = (element = scheduleObject.get("created")).isJsonNull() ? null : element.getAsString();
			String updated = (element = scheduleObject.get("updated")).isJsonNull() ? null : element.getAsString();
			String state = (element = scheduleObject.get("state")) == null || element.isJsonNull() ? null : element.getAsString();
			ScheduleRecord scheduleRecord = new ScheduleRecord(id, command, systemID, nodeID, userID, params, iCal, nextStart, created, updated, state);
			scheduleList.put(id, scheduleRecord);
		}

		return schedule;

	}
}
