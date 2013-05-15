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
import java.net.URLEncoder;
import java.util.LinkedHashMap;

import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.skysql.manager.MonitorRecord;

public class Monitors {

	public static final String MONITOR_CAPACITY = "5";

	private static LinkedHashMap<String, MonitorRecord> monitorsList;

	public static LinkedHashMap<String, MonitorRecord> getMonitorsList() {
		if (monitorsList == null) {
			reloadMonitors();
		}
		return monitorsList;
	}

	public static MonitorRecord getMonitor(String ID) {
		if (monitorsList == null) {
			reloadMonitors();
		}

		if (monitorsList != null) {
			return monitorsList.get(ID);
		} else {
			return null;
		}
	}

	public Monitors() {

	}

	public synchronized static void reloadMonitors() {

		APIrestful api = new APIrestful();
		if (api.get("monitorclass")) {
			Monitors monitors = APIrestful.getGson().fromJson(api.getResult(), Monitors.class);
			Monitors.monitorsList = monitors.monitorsList;
		}

	}

	public synchronized static String setMonitor(MonitorRecord monitor) {

		APIrestful api = new APIrestful();

		try {
			boolean success;
			if (monitor.getID() != null) {

				JSONObject jsonParam = new JSONObject();
				jsonParam.put("name", monitor.getName());
				jsonParam.put("description", monitor.getDescription());
				jsonParam.put("sql", monitor.getSql());
				jsonParam.put("unit", monitor.getUnit());
				jsonParam.put("delta", monitor.isDelta() ? "1" : "0");
				jsonParam.put("systemaverage", monitor.isAverage() ? "1" : "0");
				jsonParam.put("monitortype", "SQL");

				// type needs to indicate int/float/string etc. instead of LineChart/AreaChart
				jsonParam.put("type", monitor.getChartType());
				int interval = monitor.getInterval();
				jsonParam.put("interval", String.valueOf(interval));

				success = api.put("monitorclass/" + monitor.getID(), jsonParam.toString());
			} else {

				StringBuffer regParam = new StringBuffer();
				regParam.append("name=" + URLEncoder.encode(monitor.getName(), "UTF-8"));
				regParam.append("&description=" + URLEncoder.encode(monitor.getDescription(), "UTF-8"));
				regParam.append("&sql=" + URLEncoder.encode(monitor.getSql(), "UTF-8"));
				regParam.append("&unit=" + URLEncoder.encode(monitor.getUnit(), "UTF-8"));
				regParam.append("&delta=" + (monitor.isDelta() ? "1" : "0"));
				regParam.append("&systemaverage=" + (monitor.isAverage() ? "1" : "0"));
				regParam.append("&monitortype=" + "SQL");

				// type needs to indicate int/float/string etc. instead of LineChart/AreaChart
				regParam.append("&type=" + URLEncoder.encode(monitor.getChartType(), "UTF-8"));
				int interval = monitor.getInterval();
				regParam.append("&interval=" + String.valueOf(interval));

				success = api.post("monitorclass", regParam.toString());
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error encoding API request");
		}

		WriteResponse writeResponse = APIrestful.getGson().fromJson(api.getResult(), WriteResponse.class);
		if (writeResponse != null && !writeResponse.getInsertKey().isEmpty()) {
			String monitorID = writeResponse.getInsertKey();
			monitor.setID(monitorID);
			monitorsList.put(monitorID, monitor);
			return monitorID;
		} else if (writeResponse != null && writeResponse.getUpdateCount() > 0) {
			return monitor.getID();
		} else {
			return null;
		}

	}

	public synchronized static boolean deleteMonitor(MonitorRecord monitor) {

		APIrestful api = new APIrestful();
		if (api.delete("monitorclass/" + monitor.getID())) {
			WriteResponse writeResponse = APIrestful.getGson().fromJson(api.getResult(), WriteResponse.class);
			if (writeResponse != null && writeResponse.getDeleteCount() > 0) {
				monitorsList.remove(monitor.getID());
				return true;
			}
		}
		return false;
	}

	protected void setMonitorsList(LinkedHashMap<String, MonitorRecord> monitorsList) {
		Monitors.monitorsList = monitorsList;
	}

}

class MonitorsDeserializer implements JsonDeserializer<Monitors> {
	public Monitors deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		Monitors monitors = new Monitors();

		JsonElement jsonElement = json.getAsJsonObject().get("monitorclasses");
		if (jsonElement == null || jsonElement.isJsonNull()) {
			monitors.setMonitorsList(null);
		} else {
			JsonArray array = jsonElement.getAsJsonArray();
			int length = array.size();

			LinkedHashMap<String, MonitorRecord> monitorsList = new LinkedHashMap<String, MonitorRecord>(length);
			for (int i = 0; i < length; i++) {
				JsonObject jsonObject = array.get(i).getAsJsonObject();
				JsonElement element;
				String id = (element = jsonObject.get("id")).isJsonNull() ? null : element.getAsString();
				String name = (element = jsonObject.get("name")).isJsonNull() ? null : element.getAsString();
				String description = (element = jsonObject.get("description")).isJsonNull() ? null : element.getAsString();
				String unit = (element = jsonObject.get("unit")).isJsonNull() ? null : element.getAsString();
				String type = (element = jsonObject.get("monitortype")).isJsonNull() ? null : element.getAsString();
				boolean delta = (element = jsonObject.get("delta")).isJsonNull() ? false : element.getAsBoolean();
				boolean average = (element = jsonObject.get("systemaverage")).isJsonNull() ? false : element.getAsBoolean();
				String chartType = (element = jsonObject.get("type")).isJsonNull() ? null : element.getAsString();
				String intervalString = (element = jsonObject.get("interval")).isJsonNull() ? null : element.getAsString();
				int interval = (intervalString != null && !intervalString.isEmpty()) ? Integer.valueOf(intervalString) : 0;
				String sql = (element = jsonObject.get("sql")).isJsonNull() ? null : element.getAsString();
				MonitorRecord monitorRecord = new MonitorRecord(id, name, description, unit, type, delta, average, chartType, interval, sql);
				monitorsList.put(id, monitorRecord);
			}
			monitors.setMonitorsList(monitorsList);

		}

		return monitors;
	}
}
