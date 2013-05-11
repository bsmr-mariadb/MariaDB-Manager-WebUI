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

package com.skysql.consolev.api;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.skysql.consolev.ConsoleUI;
import com.skysql.consolev.MonitorRecord;
import com.skysql.consolev.api.AppData.Debug;

public class MonitorDataLatest {

	private MonitorRecord monitor;
	private Number latestValue;
	private Long latestTimeStamp;

	public Number getLatestValue() {
		return latestValue;
	}

	protected void setLatestValue(Number latestValue) {
		this.latestValue = latestValue;
	}

	public Long getLatestTimeStamp() {
		return latestTimeStamp;
	}

	public void setLatestTimeStamp(Long latestTimeStamp) {
		this.latestTimeStamp = latestTimeStamp;
	}

	public boolean update(String system, String node) {

		boolean modified = false;
		MonitorDataLatest newMonitorData = new MonitorDataLatest(monitor, system, node);
		if (newMonitorData.latestValue != latestValue) {
			modified = true;
			latestValue = newMonitorData.latestValue;
		}
		latestTimeStamp = newMonitorData.latestTimeStamp;

		return modified;

	}

	public MonitorDataLatest() {
	}

	public MonitorDataLatest(MonitorRecord monitor, String system, String node) {

		this.monitor = monitor;

		APIrestful api = new APIrestful();
		String uri = "system/" + system + (node.equals(SystemInfo.SYSTEM_NODEID) ? "" : "/node/" + node) + "/monitor/" + monitor.getID() + "/latest";
		if (Debug.ON) {
			ConsoleUI.log(uri);
		}

		if (api.get(uri)) {
			MonitorDataLatest monitorData = AppData.getGson().fromJson(api.getResult(), MonitorDataLatest.class);
			this.latestValue = monitorData.latestValue;
			this.latestTimeStamp = monitorData.latestTimeStamp;
			if (Debug.ON) {
				ConsoleUI.log("latestValue: " + (this.latestValue != null ? this.latestValue.toString() : "null"));
			}
		}
	}
}

class MonitorDataLatestDeserializer implements JsonDeserializer<MonitorDataLatest> {
	public MonitorDataLatest deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		MonitorDataLatest monitorData = new MonitorDataLatest();

		JsonElement jsonElement = json.getAsJsonObject().get("latest");
		if (jsonElement != null && !jsonElement.isJsonNull()) {
			monitorData.setLatestValue(sanitize(jsonElement.getAsDouble()));
			return monitorData;
		}

		return monitorData;

	}

	private Double sanitize(Double value) {

		String strValue = String.valueOf(value);
		if (value % 1.0 > 0) {
			int index = strValue.indexOf(".");
			int strlen = strValue.length();
			if (value >= 100.0 || value <= -100.0) {
				strValue = strValue.substring(0, index);
				value = Double.valueOf(strValue);
			} else if (value >= 10.0 || value <= -10.0) {
				strValue = strValue.substring(0, (index + 2) >= strlen ? strlen : index + 2);
				value = Double.valueOf(strValue);
			} else {
				strValue = strValue.substring(0, (index + 3) >= strlen ? strlen : index + 3);
				value = Double.valueOf(strValue);
			}
		}

		return value;
	}
}
