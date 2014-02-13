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
 * Copyright 2012-2014 SkySQL Ab
 */

package com.skysql.manager.api;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.skysql.java.Logging;
import com.skysql.manager.MonitorRecord;
import com.skysql.manager.ui.ErrorDialog;
import com.vaadin.addon.timeline.Timeline;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;

public class MonitorDataRaw {

	private MonitorRecord monitor;
	private ArrayList<Double> dataPoints;
	private ArrayList<Long> timeStamps;
	private String system, node;
	private Long timeEnd;

	public ArrayList<Double> getDataPoints() {
		return dataPoints;
	}

	public void setDataPoints(ArrayList<Double> dataPoints) {
		this.dataPoints = dataPoints;
	}

	public ArrayList<Long> getTimeStamps() {
		return timeStamps;
	}

	public void setTimeStamps(ArrayList<Long> timeStamps) {
		this.timeStamps = timeStamps;
	}

	public Long getLatestTime() {
		return (timeStamps == null || timeStamps.size() == 0) ? null : timeStamps.get(timeStamps.size() - 1);
	}

	public boolean update(String system, String node, String time, String interval) {

		MonitorDataRaw newMonitorData;
		if ((system != null && system.equals(this.system)) || (node != null && node.equals(this.node))) {
			this.system = system;
			this.node = node;
			newMonitorData = new MonitorDataRaw(monitor, system, node, timeEnd, interval);
			if (newMonitorData != null && newMonitorData.getDataPoints() != null) {
				dataPoints = newMonitorData.dataPoints;
				timeStamps = newMonitorData.timeStamps;
				return true;
			} else {
				return false;
			}
		} else {
			newMonitorData = new MonitorDataRaw(monitor, system, node, timeEnd + 1, interval);
		}

		return false;
	}

	public void fillDataSource(IndexedContainer container) {

		Calendar cal = new GregorianCalendar();

		for (int i = 0; i < dataPoints.size(); i++) {
			// Create a point in time
			cal.setTimeInMillis(timeStamps.get(i) * 1000L);

			Item item = container.addItem(cal.getTime());
			if (item == null) {
//				System.err.println("point in time is null");
				Logging.error("point in time is null");
			} else {
				item.getItemProperty(Timeline.PropertyId.TIMESTAMP).setValue(cal.getTime());
				double value = dataPoints.get(i);
				item.getItemProperty(Timeline.PropertyId.VALUE).setValue((float) value);
			}
		}
	}

	public MonitorDataRaw() {
	}

	public MonitorDataRaw(MonitorRecord monitor, String system, String node, Long timeEnd, String interval) {

		if (timeEnd == null) {
			timeEnd = System.currentTimeMillis() / 1000;
		}
		Long timeStart = timeEnd - Long.valueOf(interval);

		this.monitor = monitor;
		this.system = system;
		this.node = node;
		this.timeEnd = timeEnd;

		APIrestful api = new APIrestful();
		String uri = "system/" + system + (node.equals(SystemInfo.SYSTEM_NODEID) ? "" : "/node/" + node) + "/monitor/" + monitor.getID() + "/rawdata";
		String params = "?start=" + (timeStart != null ? String.valueOf(timeStart) : "") + "&finish=" + String.valueOf(timeEnd);

		if (api.get(uri, params)) {
			try {
				MonitorDataRaw monitorData = APIrestful.getGson().fromJson(api.getResult(), MonitorDataRaw.class);
				this.dataPoints = monitorData.dataPoints;
				this.timeStamps = monitorData.timeStamps;
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

class MonitorDataRawDeserializer implements JsonDeserializer<MonitorDataRaw> {
	public MonitorDataRaw deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException, NullPointerException {
		MonitorDataRaw monitorData = new MonitorDataRaw();

		JsonElement jsonElement = json.getAsJsonObject().get("monitor_rawdata");
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
				ArrayList<Double> dataPoints = new ArrayList<Double>(length);
				for (int i = 0; i < length; i++) {
					Double dataPoint = array.get(i).getAsDouble();
					dataPoints.add(dataPoint);
				}
				monitorData.setDataPoints(dataPoints);
			}
		}

		return monitorData;
	}

}
