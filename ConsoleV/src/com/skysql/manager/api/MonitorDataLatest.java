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

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.skysql.manager.ManagerUI;
import com.skysql.manager.MonitorRecord;
import com.skysql.manager.ui.ErrorDialog;

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
		ManagerUI.log(uri);

		if (api.get(uri)) {
			try {
				MonitorDataLatest monitorData = APIrestful.getGson().fromJson(api.getResult(), MonitorDataLatest.class);
				this.latestValue = monitorData.latestValue;
				this.latestTimeStamp = monitorData.latestTimeStamp;
				ManagerUI.log("latestValue: " + (this.latestValue != null ? this.latestValue.toString() : "null"));
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

class MonitorDataLatestDeserializer implements JsonDeserializer<MonitorDataLatest> {
	public MonitorDataLatest deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException, NullPointerException {
		MonitorDataLatest monitorData = new MonitorDataLatest();

		JsonElement jsonElement = json.getAsJsonObject().get("latest");
		if (!jsonElement.isJsonNull()) {
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
