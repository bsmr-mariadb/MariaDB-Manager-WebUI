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

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.skysql.manager.ChartMappings;
import com.skysql.manager.MonitorRecord;
import com.skysql.manager.UserChart;
import com.skysql.manager.ui.components.ChartControls;
import com.vaadin.server.VaadinSession;

public class ChartProperties implements Serializable {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	protected LinkedHashMap<String, ArrayList<ChartMappings>> chartsMap;
	protected int timeSpan;
	protected String theme;
	private UserObject userObject;

	public ChartProperties() {

	}

	public ChartProperties(String dummy) {

		userObject = VaadinSession.getCurrent().getAttribute(UserObject.class);

		String propertyChartSettings = userObject.getProperty(UserObject.PROPERTY_CHART_SETTINGS);
		if (propertyChartSettings != null) {
			ChartProperties chartProperties = APIrestful.getGson().fromJson(propertyChartSettings, ChartProperties.class);
			timeSpan = chartProperties.getTimeSpan();
			theme = chartProperties.getTheme();
		} else {
			timeSpan = ChartControls.DEFAULT_INTERVAL;
			theme = ChartControls.DEFAULT_THEME;
		}

		// Try to get mappings from user properties
		String propertyChartMappings = userObject.getProperty(UserObject.PROPERTY_CHART_MAPPINGS);
		if (propertyChartMappings != null) {
			ChartProperties chartProperties = APIrestful.getGson().fromJson(propertyChartMappings, ChartProperties.class);
			this.chartsMap = chartProperties.getChartsMap();
		}

		// If not available, create fresh mappings from all available Monitors
		if (chartsMap == null) {
			chartsMap = new LinkedHashMap<String, ArrayList<ChartMappings>>();
			for (String type : SystemTypes.getList().keySet()) {
				ArrayList<ChartMappings> chartMappings = new ArrayList<ChartMappings>();
				for (MonitorRecord monitor : Monitors.getMonitorsList(type).values()) {
					if (monitor.getChartType() == null) {
						continue;
					}
					ArrayList<String> monitorsForChart = new ArrayList<String>();
					monitorsForChart.add(monitor.getID());
					UserChart userChart = new UserChart(monitor.getName(), monitor.getDescription(), monitor.getUnit(), monitor.getChartType(), 15,
							monitorsForChart);
					ChartMappings chartMapping = new ChartMappings(userChart);
					chartMappings.add(chartMapping);
				}
				chartsMap.put(type, chartMappings);
			}
		}

	}

	protected LinkedHashMap<String, ArrayList<ChartMappings>> getChartsMap() {
		return chartsMap;
	}

	protected void setChartsMap(LinkedHashMap<String, ArrayList<ChartMappings>> chartsMap) {
		this.chartsMap = chartsMap;
	}

	public ArrayList<ChartMappings> getChartMappings(String key) {
		return chartsMap.get(key);
	}

	public void setChartMappings(String key, ArrayList<ChartMappings> value) {
		chartsMap.put(key, value);
		save();
	}

	// {"chartProperties":[{"systemtype":"aws","mappings":[{...},...]},{"systemid":"1","mappings":[{...},...]}]}
	public void save() {

		StringBuilder sb = new StringBuilder();
		sb.append("{\"chartProperties\":[");
		for (String type : chartsMap.keySet()) {
			sb.append("{\"systemtype\":\"" + type + "\",\"mappings\":[");
			for (ChartMappings chartMapping : chartsMap.get(type)) {
				sb.append(mappingToJSON(chartMapping) + ",");
			}
			if (sb.charAt(sb.length() - 1) == ',') {
				sb.deleteCharAt(sb.length() - 1);
			}
			sb.append("]},");
		}
		if (sb.charAt(sb.length() - 1) == ',') {
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append("]}");

		userObject.setProperty(UserObject.PROPERTY_CHART_MAPPINGS, sb.toString());

		// Encode a String into bytes
		//		byte[] input = sb.toString().getBytes("UTF-8");
		//
		//		// Compress the bytes
		//		byte[] output = new byte[input.length];
		//		Deflater compresser = new Deflater();
		//		compresser.setInput(input);
		//		compresser.finish();
		//		int compressedDataLength = compresser.deflate(output);
		//		userObject.setProperty(UserObject.PROPERTY_CHART_MAPPINGS, ChartMappings.toString(new String(output, 0, compressedDataLength)));

	}

	public int getTimeSpan() {
		return timeSpan;
	}

	public void setTimeSpan(int timeSpan) {
		this.timeSpan = timeSpan;
		saveChartSettings();
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
		saveChartSettings();
	}

	private String mappingToJSON(ChartMappings mapping) {
		StringBuilder sb = new StringBuilder();
		sb.append("{\"name\":" + (mapping.getName() == null ? "null" : "\"" + mapping.getName() + "\""));
		sb.append(",\"description\":" + (mapping.getDescription() == null ? "null" : "\"" + mapping.getDescription() + "\""));
		sb.append(",\"unit\":" + (mapping.getUnit() == null ? "null" : "\"" + mapping.getUnit() + "\""));
		sb.append(",\"type\":" + (mapping.getType() == null ? "null" : "\"" + mapping.getType() + "\""));
		sb.append(",\"points\":" + mapping.getPoints());
		sb.append(",\"monitorIDs\":[");
		for (String monitorID : mapping.getMonitorIDs()) {
			sb.append("\"" + monitorID + "\",");
		}
		if (sb.charAt(sb.length() - 1) == ',') {
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append("]}");

		return sb.toString();
	}

	private void saveChartSettings() {
		String settings = "{\"chartSettings\":{\"timeSpan\":" + timeSpan + ",\"theme\":\"" + theme + "\"}}";
		userObject.setProperty(UserObject.PROPERTY_CHART_SETTINGS, settings);
	}
}

/***
{"chartProperties":[
{"systemtype":"aws","mappings":[
	{"name":"Connections","description":"","unit":null,"type":"LineChart","points":15,"monitorIDs":["connections"]},
	{"name":"Mixed","description":"","unit":"kB/min","type":"LineChart","points":15,"monitorIDs":["traffic","connections"]},
	{"name":"Availability","description":"","unit":"%","type":"LineChart","points":15,"monitorIDs":["availability"]}
]},
{"systemtype":"galera","mappings":[
	{"name":"Connections","description":"","unit":null,"type":"LineChart","points":15,"monitorIDs":["connections"]},
    {"name":"Network Traffic","description":"","unit":"kB/min","type":"LineChart","points":15,"monitorIDs":["traffic"]},
    {"name":"Availability","description":"","unit":"%","type":"LineChart","points":15,"monitorIDs":["availability"]},
    {"name":"Cluster Size","description":"Number of nodes in the cluster","unit":null,"type":"LineChart","points":15,"monitorIDs":["clustersize"]},
    {"name":"Replication Paused","description":"Percentage of time for which replication was paused","unit":"%","type":"LineChart","points":15,"monitorIDs":["reppaused"]},
    {"name":"Parallelism","description":"Average No. of parallel transactions","unit":null,"type":"LineChart","points":15,"monitorIDs":["parallelism"]},
    {"name":"Avg Receive Queue","description":"Average receive queue length","unit":null,"type":"LineChart","points":15,"monitorIDs":["recvqueue"]},
    {"name":"Flow Controlled","description":"Flow control messages sent","unit":null,"type":"LineChart","points":15,"monitorIDs":["flowcontrol"]},
    {"name":"Avg Send Queue","description":"Average length of send queue","unit":null,"type":"LineChart","points":15,"monitorIDs":["sendqueue"]}
]}]}
***/
class ChartPropertiesDeserializer implements JsonDeserializer<ChartProperties> {
	public ChartProperties deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

		ChartProperties chartProperties = new ChartProperties();

		JsonElement jsonElement = json.getAsJsonObject().get("chartProperties");
		if (jsonElement != null && !jsonElement.isJsonNull()) {

			LinkedHashMap<String, ArrayList<ChartMappings>> chartsMap = new LinkedHashMap<String, ArrayList<ChartMappings>>();
			chartProperties.setChartsMap(chartsMap);

			JsonArray array = jsonElement.getAsJsonArray();
			for (int i = 0; i < array.size(); i++) {
				JsonObject jsonObject = array.get(i).getAsJsonObject();

				String systemType = (jsonElement = jsonObject.get("systemtype")).isJsonNull() ? null : jsonElement.getAsString();
				JsonArray mappingJson = jsonObject.get("mappings").getAsJsonArray();
				int length = mappingJson.size();

				ArrayList<ChartMappings> chartsList = new ArrayList<ChartMappings>(length);

				for (int j = 0; j < length; j++) {
					JsonObject mappingObject = mappingJson.get(j).getAsJsonObject();

					JsonElement element;
					String name = (element = mappingObject.get("name")).isJsonNull() ? null : element.getAsString();
					String description = (element = mappingObject.get("description")).isJsonNull() ? null : element.getAsString();
					String unit = (element = mappingObject.get("unit")).isJsonNull() ? null : element.getAsString();
					String type = (element = mappingObject.get("type")).isJsonNull() ? null : element.getAsString();
					int points = (element = mappingObject.get("points")).isJsonNull() ? null : element.getAsInt();
					element = mappingObject.get("monitorIDs");
					ArrayList<String> monitorIDs = new ArrayList<String>();
					if (element != null && !element.isJsonNull()) {
						JsonArray IDs = element.getAsJsonArray();
						for (int k = 0; k < IDs.size(); k++) {
							String id = String.valueOf(IDs.get(k).getAsString());
							monitorIDs.add(id);
						}
					}
					ChartMappings chartMapping = new ChartMappings(name, description, unit, type, points, monitorIDs);
					chartsList.add(chartMapping);
				}

				chartsMap.put(systemType, chartsList);
			}

		}

		jsonElement = json.getAsJsonObject().get("chartSettings");
		if (jsonElement != null && !jsonElement.isJsonNull()) {
			JsonObject settingsJson = jsonElement.getAsJsonObject();
			JsonElement element;
			chartProperties.timeSpan = (element = settingsJson.get("timeSpan")).isJsonNull() ? 0 : element.getAsInt();
			chartProperties.theme = (element = settingsJson.get("theme")).isJsonNull() ? null : element.getAsString();
		}

		return chartProperties;

	}
}
