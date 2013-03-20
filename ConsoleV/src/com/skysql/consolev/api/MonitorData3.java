package com.skysql.consolev.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.skysql.consolev.MonitorRecord;

public class MonitorData3 {

	private MonitorRecord monitor;
	private String[][] dataPoints;

	public String[][] getDataPoints() {
		return dataPoints;
	}

	public void setDataPoints(String[][] dataPoints) {
		this.dataPoints = dataPoints;
	}

	public boolean equals(Object ob) {
		if (!(ob instanceof MonitorData3))
			return false;
		MonitorData3 other = (MonitorData3) ob;
		if (!java.util.Arrays.deepEquals(dataPoints, other.dataPoints))
			return false;

		return true;
	}

	public boolean update(String system, String node, String time, String interval, String count) {

		MonitorData3 newMonitorData = new MonitorData3(monitor, system, node, time, interval, count);
		if (!this.equals(newMonitorData)) {
			dataPoints = newMonitorData.getDataPoints();
			return true;
		} else {
			return false;
		}
	}

	public MonitorData3() {
	}

	public MonitorData3(MonitorRecord monitor, String system, String node, String time, String interval, String count) {

		this.monitor = monitor;

		String inputLine = null;
		try {
			URL url = new URI("http", "localhost", "/consoleAPI/monitorinfo3.php", "monitor=" + monitor.getID() + "&system=" + system + "&node=" + node
					+ "&time=" + time + "&interval=" + interval + "&count=" + count, null).toURL();
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
		MonitorData3 monitorData = gson.fromJson(inputLine, MonitorData3.class);
		this.dataPoints = monitorData.dataPoints;
		monitorData = null;
	}

}

class MonitorDataDeserializer3 implements JsonDeserializer<MonitorData3> {
	public MonitorData3 deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		MonitorData3 monitorData = new MonitorData3();

		JsonElement jsonElement = json.getAsJsonObject().get("monitor_data");
		if (jsonElement == null || jsonElement.isJsonNull()) {
			monitorData.setDataPoints(null);
		} else {
			JsonArray array = jsonElement.getAsJsonArray();
			int length = array.size();

			// String[][] points = new String[length][4];
			ArrayList<String[]> points = new ArrayList<String[]>();
			for (int i = 0; i < length; i++) {
				JsonObject jsonObject = array.get(i).getAsJsonObject();
				JsonElement element;
				String dataPoint[] = new String[4];
				dataPoint[0] = (element = jsonObject.get("min")).isJsonNull() ? null : element.getAsString();
				dataPoint[1] = (element = jsonObject.get("max")).isJsonNull() ? null : element.getAsString();
				dataPoint[2] = (element = jsonObject.get("start")).isJsonNull() ? null : element.getAsString();
				dataPoint[3] = (element = jsonObject.get("end")).isJsonNull() ? null : element.getAsString();
				if (dataPoint[0] != null && dataPoint[1] != null) {
					points.add(dataPoint);
				}
			}
			String arr[][] = points.toArray(new String[points.size()][]);
			monitorData.setDataPoints(arr);
		}
		return monitorData;
	}
}
