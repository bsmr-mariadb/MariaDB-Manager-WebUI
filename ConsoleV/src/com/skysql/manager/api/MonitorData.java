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
import com.skysql.manager.MonitorRecord;
import com.skysql.manager.ui.ErrorDialog;

public class MonitorData {

	public static final String METHOD_AVG = "avg";
	public static final String METHOD_MINMAX = "minmax";

	private MonitorRecord monitor;
	private ArrayList<Number> avgPoints;
	private ArrayList<Number> minPoints;
	private ArrayList<Number> maxPoints;
	private ArrayList<Long> timeStamps;
	private String system, node, timeSpan, method;

	public ArrayList<Number> getAvgPoints() {
		return avgPoints;
	}

	protected void setAvgPoints(ArrayList<Number> avgPoints) {
		this.avgPoints = avgPoints;
	}

	public ArrayList<Number> getMinPoints() {
		return minPoints;
	}

	protected void setMinPoints(ArrayList<Number> minPoints) {
		this.minPoints = minPoints;
	}

	public ArrayList<Number> getMaxPoints() {
		return maxPoints;
	}

	protected void setMaxPoints(ArrayList<Number> maxPoints) {
		this.maxPoints = maxPoints;
	}

	public ArrayList<Long> getTimeStamps() {
		return timeStamps;
	}

	public void setTimeStamps(ArrayList<Long> timeStamps) {
		this.timeStamps = timeStamps;
	}

	public Long getLatestTime() {
		return (timeStamps == null) ? null : timeStamps.get(timeStamps.size() - 1);
	}

	public boolean update(String system, String node, String endTime, String timeSpan, int count) {

		MonitorData newMonitorData;
		if ((system != null && !system.equals(this.system)) || (node != null && !node.equals(this.node)) || (!timeSpan.equals(this.timeSpan))) {
			this.system = system;
			this.node = node;
			this.timeSpan = timeSpan;
			avgPoints = null;
			minPoints = null;
			maxPoints = null;
			timeStamps = null;

			newMonitorData = new MonitorData(monitor, system, node, endTime, timeSpan, count, method);
			if (newMonitorData != null) {
				avgPoints = newMonitorData.avgPoints;
				minPoints = newMonitorData.minPoints;
				maxPoints = newMonitorData.maxPoints;
				timeStamps = newMonitorData.timeStamps;
			}

			return true;

		} else {
			newMonitorData = new MonitorData(monitor, system, node, endTime, timeSpan, count, method);
			if (newMonitorData != null) {
				if ((newMonitorData.avgPoints != null && !newMonitorData.avgPoints.equals(avgPoints))
						|| (newMonitorData.minPoints != null && !newMonitorData.minPoints.equals(minPoints))
						|| (newMonitorData.maxPoints != null && !newMonitorData.maxPoints.equals(maxPoints))
						|| (newMonitorData.timeStamps != null && !newMonitorData.timeStamps.equals(timeStamps))) {
					avgPoints = newMonitorData.avgPoints;
					minPoints = newMonitorData.minPoints;
					maxPoints = newMonitorData.maxPoints;
					timeStamps = newMonitorData.timeStamps;
					return true;
				}
			}
			return false;
		}

	}

	public MonitorData() {
	}

	public MonitorData(MonitorRecord monitor, String system, String node, String endTime, String spanTime, int count, String method) {
		this.monitor = monitor;
		this.system = system;
		this.node = node;
		this.method = method;

		Long timeEnd = (endTime == null) ? null : Long.valueOf(endTime);
		Long interval = Long.valueOf(spanTime) / Long.valueOf(count);

		APIrestful api = new APIrestful();
		String uri = "system/" + system + (node.equals(SystemInfo.SYSTEM_NODEID) ? "" : "/node/" + node) + "/monitor/" + monitor.getID() + "/data";
		String params = "?finish=" + String.valueOf(timeEnd) + "&interval=" + String.valueOf(interval) + "&count=" + count + "&method=" + method;

		if (api.get(uri, params)) {
			try {
				MonitorData monitorData = APIrestful.getGson().fromJson(api.getResult(), MonitorData.class);
				avgPoints = monitorData.avgPoints;
				minPoints = monitorData.minPoints;
				maxPoints = monitorData.maxPoints;
				timeStamps = monitorData.timeStamps;
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

class MonitorDataDeserializer implements JsonDeserializer<MonitorData> {
	public MonitorData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException, NullPointerException {
		MonitorData monitorData = new MonitorData();

		JsonElement jsonElement = json.getAsJsonObject().get("monitor_data");
		if (jsonElement.isJsonNull() || jsonElement.isJsonArray()) {
			return monitorData;
		}

		JsonObject jsonObject = jsonElement.getAsJsonObject();

		if (jsonObject.has("timestamp")) {
			JsonElement jsonTime = jsonElement.getAsJsonObject().get("timestamp");
			if (jsonTime != null && !jsonTime.isJsonNull()) {
				JsonArray array = jsonTime.getAsJsonArray();
				int length = array.size();
				ArrayList<Long> timeStamps = new ArrayList<Long>(length);
				for (int i = 0; i < length; i++) {
					Long timeStamp = array.get(i).getAsLong();
					timeStamps.add(timeStamp);
				}
				monitorData.setTimeStamps(timeStamps);
			}
		}

		if (jsonObject.has("value")) {
			JsonElement jsonValue = jsonElement.getAsJsonObject().get("value");
			if (jsonValue != null && !jsonValue.isJsonNull()) {
				JsonArray array = jsonValue.getAsJsonArray();
				int length = array.size();
				ArrayList<Number> avgPoints = new ArrayList<Number>(length);
				for (int i = 0; i < length; i++) {
					Double dataPoint = array.get(i).getAsDouble();
					avgPoints.add(sanitize(dataPoint));
				}
				monitorData.setAvgPoints(avgPoints);
			}
		}

		if (jsonObject.has("min")) {
			JsonElement jsonValue = jsonElement.getAsJsonObject().get("min");
			if (jsonValue != null && !jsonValue.isJsonNull()) {
				JsonArray array = jsonValue.getAsJsonArray();
				int length = array.size();
				ArrayList<Number> minPoints = new ArrayList<Number>(length);
				for (int i = 0; i < length; i++) {
					Double dataPoint = array.get(i).getAsDouble();
					minPoints.add(sanitize(dataPoint));
				}
				monitorData.setMinPoints(minPoints);
			}
		}

		if (jsonObject.has("max")) {
			JsonElement jsonValue = jsonElement.getAsJsonObject().get("max");
			if (jsonValue != null && !jsonValue.isJsonNull()) {
				JsonArray array = jsonValue.getAsJsonArray();
				int length = array.size();
				ArrayList<Number> maxPoints = new ArrayList<Number>(length);
				for (int i = 0; i < length; i++) {
					Double dataPoint = array.get(i).getAsDouble();
					maxPoints.add(sanitize(dataPoint));
				}
				monitorData.setMaxPoints(maxPoints);
			}
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
