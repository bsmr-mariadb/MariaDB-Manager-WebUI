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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.skysql.consolev.MonitorRecord;

public class MonitorData {

	private MonitorRecord monitor;
	private String[][] dataPoints;

	public String[][] getDataPoints() {
		return dataPoints;
	}

	protected void setDataPoints(String[][] dataPoints) {
		this.dataPoints = dataPoints;
	}

	public boolean equals(Object ob) {
		if (!(ob instanceof MonitorData))
			return false;
		MonitorData other = (MonitorData) ob;
		if (!java.util.Arrays.deepEquals(dataPoints, other.dataPoints))
			return false;

		return true;
	}

	public boolean update(String system, String node, String time, String interval, String count) {

		MonitorData newMonitorData = new MonitorData(monitor, system, node, time, interval, count);
		if (!this.equals(newMonitorData)) {
			dataPoints = newMonitorData.getDataPoints();
			return true;
		} else {
			return false;
		}
	}

	public MonitorData() {
	}

	public MonitorData(MonitorRecord monitor, String system, String node, String time, String interval, String count) {

		this.monitor = monitor;

		String inputLine = null;
		try {
			URL url = new URI("http", "localhost", "/consoleAPI/monitorinfo.php", "monitor=" + monitor.getID() + "&system=" + system + "&node=" + node
					+ "&time=" + time + "&interval=" + interval + "&count=" + count, null).toURL();
			URLConnection sc = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(sc.getInputStream()));
			inputLine = in.readLine();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Could not get response from API");
		}

		Gson gson = AppData.getGson();
		MonitorData monitorData = gson.fromJson(inputLine, MonitorData.class);
		this.dataPoints = monitorData.dataPoints;
		monitorData = null;
	}

}

class MonitorDataDeserializer implements JsonDeserializer<MonitorData> {
	public MonitorData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		MonitorData monitorData = new MonitorData();

		JsonElement jsonElement;

		jsonElement = json.getAsJsonObject().get("monitor_data");
		if (jsonElement == null || jsonElement.isJsonNull()) {
			monitorData.setDataPoints(null);
		} else {
			JsonArray array = jsonElement.getAsJsonArray();
			int length = array.size();

			String[][] points = new String[length][2];
			for (int i = 0; i < length; i++) {
				JsonObject point = array.get(i).getAsJsonObject();
				points[i][0] = point.get("time").getAsString();
				points[i][1] = point.get("value").getAsString();
			}
			monitorData.setDataPoints(points);
		}
		return monitorData;
	}

}
