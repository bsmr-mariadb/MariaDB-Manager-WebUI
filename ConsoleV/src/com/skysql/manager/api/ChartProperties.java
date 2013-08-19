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

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;

import javax.xml.bind.DatatypeConverter;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.skysql.manager.ChartMappings;
import com.skysql.manager.MonitorRecord;
import com.skysql.manager.UserChart;
import com.skysql.manager.ui.ErrorDialog;
import com.skysql.manager.ui.components.ChartControls;
import com.sun.xml.bind.DatatypeConverterImpl;
import com.vaadin.server.VaadinSession;

public class ChartProperties implements Serializable {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	protected ArrayList<ChartMappings> chartMappings;
	protected int timeSpan;
	protected String theme;
	private UserObject userObject;

	public ChartProperties() {

	}

	public ChartProperties(String mappings, String settings) {

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

		// First, try to get newer mappings
		String propertyChartMappings = userObject.getProperty(UserObject.PROPERTY_CHART_MAPPINGS);
		if (propertyChartMappings == null) {

			// Second, try to get old CHARTS
			String propertyOldCharts = userObject.getProperty(UserObject.PROPERTY_CHARTS);
			if (propertyOldCharts != null) {
				DatatypeConverter.setDatatypeConverter(DatatypeConverterImpl.theInstance);
				try {
					chartMappings = (ArrayList<ChartMappings>) ChartMappings.fromString(propertyOldCharts);
				} catch (Exception e) {
					e.printStackTrace();
					new ErrorDialog(e, "Error while trying to convert old charts mappings");
				}
				setChartMappings(chartMappings);
				userObject.deleteProperty(UserObject.PROPERTY_CHARTS);
				return;

			} else {
				// TODO: Then, attempt to retrieve new CHART MAPPINGS from ApplicationProperties
			}

		}

		if (propertyChartMappings != null) {
			ChartProperties chartProperties = APIrestful.getGson().fromJson(propertyChartMappings, ChartProperties.class);
			chartMappings = chartProperties.getChartMappings();
		}

		if (chartMappings == null) {

			// Finally, if not available, create fresh mappings from all available Monitors
			chartMappings = new ArrayList<ChartMappings>();
			for (MonitorRecord monitor : Monitors.getMonitorsList(null).values()) {
				ArrayList<String> monitorsForChart = new ArrayList<String>();
				monitorsForChart.add(monitor.getID());
				UserChart userChart = new UserChart(monitor.getName(), monitor.getDescription(), monitor.getUnit(), monitor.getChartType(), 15,
						monitorsForChart);
				ChartMappings chartMapping = new ChartMappings(userChart);
				chartMappings.add(chartMapping);
			}
		}

	}

	public ArrayList<ChartMappings> getChartMappings() {
		return chartMappings;
	}

	public void setChartMappings(ArrayList<ChartMappings> chartMappings) {
		this.chartMappings = chartMappings;

		StringBuilder sb = new StringBuilder();
		sb.append("{\"chartMappings\":[");
		for (ChartMappings chartMapping : chartMappings) {
			sb.append(mappingToJSON(chartMapping) + ",");
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
		sb.append(",\"monitorIDs\":" + mapping.getMonitorIDs().toString() + "}");

		return sb.toString();
	}

	private void saveChartSettings() {
		String settings = "{\"chartSettings\":{\"timeSpan\":" + timeSpan + ",\"theme\":\"" + theme + "\"}}";
		userObject.setProperty(UserObject.PROPERTY_CHART_SETTINGS, settings);
	}
}

class ChartPropertiesDeserializer implements JsonDeserializer<ChartProperties> {
	public ChartProperties deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

		ChartProperties chartProperties = new ChartProperties();

		JsonElement jsonElement = json.getAsJsonObject().get("chartMappings");
		if (jsonElement != null && !jsonElement.isJsonNull()) {

			ArrayList<ChartMappings> chartMappings = new ArrayList<ChartMappings>();

			JsonArray array = jsonElement.getAsJsonArray();
			int length = array.size();

			for (int i = 0; i < length; i++) {
				JsonObject mappingJson = array.get(i).getAsJsonObject();

				JsonElement element;
				String name = (element = mappingJson.get("name")).isJsonNull() ? null : element.getAsString();
				String description = (element = mappingJson.get("description")).isJsonNull() ? null : element.getAsString();
				String unit = (element = mappingJson.get("unit")).isJsonNull() ? null : element.getAsString();
				String type = (element = mappingJson.get("type")).isJsonNull() ? null : element.getAsString();
				int points = (element = mappingJson.get("points")).isJsonNull() ? null : element.getAsInt();
				element = mappingJson.get("monitorIDs");
				ArrayList<String> monitorIDs = new ArrayList<String>();
				if (element != null && !element.isJsonNull()) {
					JsonArray IDs = element.getAsJsonArray();
					for (int j = 0; j < IDs.size(); j++) {
						String id = String.valueOf(IDs.get(j).getAsString());
						monitorIDs.add(id);
					}
				}
				ChartMappings chartMapping = new ChartMappings(name, description, unit, type, points, monitorIDs);
				chartMappings.add(chartMapping);
			}

			chartProperties.chartMappings = chartMappings;
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
