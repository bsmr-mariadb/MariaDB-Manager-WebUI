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
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.skysql.consolev.MonitorRecord;
import com.vaadin.addon.timeline.Timeline;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;

public class MonitorData2 {

	protected static int INDEX_TIME = 0;
	protected static int INDEX_VALUE = 1;

	private MonitorRecord monitor;
	private String system;
	private String node;
	private String time;
	private String interval;
	private String[][] dataPoints;

	public String[][] getDataPoints() {
		return dataPoints;
	}

	public void setDataPoints(String[][] dataPoints) {
		this.dataPoints = dataPoints;
	}

	public String getLatestTime() {
		return (dataPoints == null) ? null : dataPoints[dataPoints.length - 1][INDEX_TIME];
	}

	public boolean equals(Object ob) {
		if (!(ob instanceof MonitorData2))
			return false;
		MonitorData2 other = (MonitorData2) ob;
		if (!java.util.Arrays.deepEquals(dataPoints, other.dataPoints))
			return false;

		return true;
	}

	public boolean update(String system, String node, String time, String interval) {

		this.system = system;
		this.node = node;
		this.time = time;
		this.interval = interval;

		MonitorData2 newMonitorData = new MonitorData2(monitor, system, node, time, interval);
		if (!this.equals(newMonitorData)) {
			dataPoints = newMonitorData.getDataPoints();
			return true;
		} else {
			return false;
		}
	}

	public void fillDataSource(IndexedContainer container) {
		Calendar cal = Calendar.getInstance();
		// Calendar cal = new GregorianCalendar();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		for (int i = 0; i < dataPoints.length; i++) {
			try {
				cal.setTime(sdf.parse(dataPoints[i][INDEX_TIME]));

				// Create a point in time
				Item item = container.addItem(cal.getTime());
				// Set the timestamp property
				item.getItemProperty(Timeline.PropertyId.TIMESTAMP).setValue(cal.getTime());
				// Set the value property
				item.getItemProperty(Timeline.PropertyId.VALUE).setValue(Float.valueOf(dataPoints[i][INDEX_VALUE]));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public MonitorData2() {
	}

	public MonitorData2(MonitorRecord monitor, String system, String node, String time, String interval) {

		this.monitor = monitor;
		this.system = system;
		this.node = node;
		this.time = time;
		this.interval = interval;

		String inputLine = null;
		try {
			URL url = new URI("http", "localhost", "/consoleAPI/monitorinfo2.php", "monitor=" + monitor.getID() + "&system=" + system + "&node=" + node
					+ "&time=" + time + "&interval=" + interval, null).toURL();
			// System.out.println(url.toString());
			URLConnection sc = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(sc.getInputStream()));
			inputLine = in.readLine();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Could not get response from API");
		}

		Gson gson = AppData.getGson();
		MonitorData2 monitorData = gson.fromJson(inputLine, MonitorData2.class);
		this.dataPoints = monitorData.dataPoints;
		monitorData = null;
	}

}

class MonitorDataDeserializer2 implements JsonDeserializer<MonitorData2> {
	public MonitorData2 deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		MonitorData2 monitorData = new MonitorData2();

		JsonElement jsonElement = json.getAsJsonObject().get("monitor_data");
		if (jsonElement == null || jsonElement.isJsonNull()) {
			monitorData.setDataPoints(null);
		} else {
			JsonArray array = jsonElement.getAsJsonArray();
			int length = array.size();

			String[][] points = new String[length][2];
			for (int i = 0; i < length; i++) {
				JsonObject jsonObject = array.get(i).getAsJsonObject();
				JsonElement element;
				points[i][MonitorData2.INDEX_TIME] = (element = jsonObject.get("time")).isJsonNull() ? null : element.getAsString();
				points[i][MonitorData2.INDEX_VALUE] = (element = jsonObject.get("value")).isJsonNull() ? null : element.getAsString();
			}
			monitorData.setDataPoints(points);
		}
		return monitorData;
	}

}
