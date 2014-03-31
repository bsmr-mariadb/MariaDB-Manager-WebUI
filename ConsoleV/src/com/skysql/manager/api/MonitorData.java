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
import com.skysql.manager.MonitorRecord;
import com.skysql.manager.ui.ErrorDialog;

/**
 * The Class MonitorData.
 */
public class MonitorData {

	/** Used to get the average value from the API */
	public static final String METHOD_AVG = "avg";

	/** Used to get the min and max values from the API */
	public static final String METHOD_MINMAX = "minmax";

	/** The monitor. */
	private MonitorRecord monitor;

	/** The avg points. */
	private ArrayList<Number> avgPoints;

	/** The min points. */
	private ArrayList<Number> minPoints;

	/** The max points. */
	private ArrayList<Number> maxPoints;

	/** The time stamps. */
	private ArrayList<Long> timeStamps;

	private String system, node, timeSpan, method;

	/**
	 * Gets the avg points.
	 *
	 * @return the avg points
	 */
	public ArrayList<Number> getAvgPoints() {
		return avgPoints;
	}

	/**
	 * Sets the avg points.
	 *
	 * @param avgPoints the new avg points
	 */
	protected void setAvgPoints(ArrayList<Number> avgPoints) {
		this.avgPoints = avgPoints;
	}

	/**
	 * Gets the min points.
	 *
	 * @return the min points
	 */
	public ArrayList<Number> getMinPoints() {
		return minPoints;
	}

	/**
	 * Sets the min points.
	 *
	 * @param minPoints the new min points
	 */
	protected void setMinPoints(ArrayList<Number> minPoints) {
		this.minPoints = minPoints;
	}

	/**
	 * Gets the max points.
	 *
	 * @return the max points
	 */
	public ArrayList<Number> getMaxPoints() {
		return maxPoints;
	}

	/**
	 * Sets the max points.
	 *
	 * @param maxPoints the new max points
	 */
	protected void setMaxPoints(ArrayList<Number> maxPoints) {
		this.maxPoints = maxPoints;
	}

	/**
	 * Gets the time stamps.
	 *
	 * @return the time stamps
	 */
	public ArrayList<Long> getTimeStamps() {
		return timeStamps;
	}

	/**
	 * Sets the time stamps.
	 *
	 * @param timeStamps the new time stamps
	 */
	public void setTimeStamps(ArrayList<Long> timeStamps) {
		this.timeStamps = timeStamps;
	}

	/**
	 * Gets the latest time.
	 *
	 * @return the latest time
	 */
	public Long getLatestTime() {
		return (timeStamps == null) ? null : timeStamps.get(timeStamps.size() - 1);
	}

	/**
	 * Updates the monitor data from the API.
	 *
	 * @param system the system
	 * @param node the node
	 * @param endTime the end time
	 * @param timeSpan the time span
	 * @param count the count
	 * @return true, if successful
	 */
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

	/**
	 * Instantiates a new monitor data.
	 */
	public MonitorData() {
	}

	/**
	 * Instantiates a new monitor data.
	 *
	 * @param monitor the monitor
	 * @param system the system
	 * @param node the node
	 * @param endTime the end time
	 * @param spanTime the span time
	 * @param count the count
	 * @param method the method
	 */
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

/**
 * The Class MonitorDataDeserializer.
 */
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
