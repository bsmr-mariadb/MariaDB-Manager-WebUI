package com.skysql.consolev.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.skysql.consolev.MonitorRecord;

public class Monitors {

	public static final String MONITOR_CAPACITY = "5";

	private static LinkedHashMap<String, MonitorRecord> monitorsList;
	private ArrayList<MonitorRecord> monitorsDisplay;

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
		return monitorsList.get(ID);
	}

	public Monitors() {

	}

	public static void reloadMonitors() {

		String inputLine = null;
		try {
			URL url = new URI("http", "localhost", "/consoleAPI/monitors.php", null, null).toURL();

			URLConnection sc = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(sc.getInputStream()));
			inputLine = in.readLine();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Could not get response from API");
		}

		Gson gson = AppData.getGson();
		Monitors monitors = gson.fromJson(inputLine, Monitors.class);
		//		Monitors.monitorsList = monitors.getMonitorsList();
		monitors = null;
	}

	public static String setMonitor(MonitorRecord monitor) {

		String inputLine = null;
		try {
			URL url = new URI("http", "localhost", "/consoleAPI/setmonitor.php", "id=" + monitor.getID() + "&name=" + monitor.getName() + "&description="
					+ monitor.getDescription() + "&unit=" + monitor.getUnit() + "&sql=" + monitor.getSql() + "&delta=" + monitor.isDelta() + "&average="
					+ monitor.isAverage() + "&interval=" + monitor.getInterval() + "&chartType=" + monitor.getChartType(), null).toURL();
			URLConnection sc = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(sc.getInputStream()));
			inputLine = in.readLine();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Could not get response from API");
		}

		Gson gson = AppData.getGson();
		Response response = gson.fromJson(inputLine, Response.class);
		String monitorID = response.getResponse();
		if (monitor.getID() == null && monitorID != null) {
			monitor.setID(monitorID);
			monitorsList.put(monitorID, monitor);
		}

		return (monitorID);
	}

	public static String deleteMonitor(MonitorRecord monitor) {

		String inputLine = null;
		try {
			URL url = new URI("http", "localhost", "/consoleAPI/deletemonitor.php", "id=" + monitor.getID(), null).toURL();
			URLConnection sc = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(sc.getInputStream()));
			inputLine = in.readLine();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Could not get response from API");
		}

		Gson gson = AppData.getGson();
		Response response = gson.fromJson(inputLine, Response.class);
		String monitorID = response.getResponse();
		if (monitorID != null) {
			monitorsList.remove(monitorID);
		}

		return (monitorID);
	}

	protected void setMonitorsList(LinkedHashMap<String, MonitorRecord> monitorsList) {
		Monitors.monitorsList = monitorsList;
	}

}

class MonitorsDeserializer implements JsonDeserializer<Monitors> {
	public Monitors deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		Monitors monitors = new Monitors();

		JsonElement jsonElement = json.getAsJsonObject().get("monitors");
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
				String icon = (element = jsonObject.get("icon")).isJsonNull() ? null : element.getAsString();
				String type = (element = jsonObject.get("type")).isJsonNull() ? null : element.getAsString();
				boolean delta = (element = jsonObject.get("delta")).isJsonNull() ? null : element.getAsBoolean();
				boolean average = (element = jsonObject.get("average")).isJsonNull() ? null : element.getAsBoolean();
				String chartType = (element = jsonObject.get("chartType")).isJsonNull() ? null : element.getAsString();
				int interval = (element = jsonObject.get("interval")).isJsonNull() ? null : element.getAsInt();
				String sql = (element = jsonObject.get("sql")).isJsonNull() ? null : element.getAsString();
				MonitorRecord monitorRecord = new MonitorRecord(id, name, description, unit, icon, type, delta, average, chartType, interval, sql);
				monitorsList.put(id, monitorRecord);
			}
			monitors.setMonitorsList(monitorsList);

		}

		return monitors;
	}

}
