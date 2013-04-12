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
 * Copyright SkySQL Ab
 */

package com.skysql.consolev.api;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashMap;

import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class SystemInfo extends ClusterComponent {

	public static final String SYSTEM_NODEID = "0";

	public static final String PROPERTY_EIP = "EIP";
	public static final String PROPERTY_MONYOG = "MONyog";
	public static final String PROPERTY_PHPMYADMIN = "phpMyAdmin";
	public static final String PROPERTY_DEFAULTMONITORINTERVAL = "MonitorInterval";
	public static final String PROPERTY_DEFAULTMAXBACKUPCOUNT = "maxBackupCount";
	public static final String PROPERTY_DEFAULTMAXBACKUPSIZE = "maxBackupSize";
	public static final String PROPERTY_VERSION = "VERSION";
	public static final String PROPERTY_SKIPLOGIN = "SKIPLOGIN";

	private String startDate;
	private String lastStop;
	private String lastAccess;
	private String[] nodes;
	private LinkedHashMap<String, String> properties;
	private String lastCommand;
	private String lastBackup;

	public String getStartDate() {
		return startDate;
	}

	protected void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getLastStop() {
		return lastStop;
	}

	protected void setLastStop(String lastStop) {
		this.lastStop = lastStop;
	}

	public String getLastAccess() {
		return lastAccess;
	}

	public void setLastAccess(String lastAccess) {
		this.lastAccess = lastAccess;
	}

	public String[] getNodes() {
		return nodes;
	}

	protected void setNodes(String[] nodes) {
		this.nodes = nodes;
	}

	public LinkedHashMap<String, String> getProperties() {
		return properties;
	}

	protected void setProperties(LinkedHashMap<String, String> properties) {
		this.properties = properties;
	}

	public String getLastBackup() {
		return lastBackup;
	}

	public void setLastBackup(String lastBackup) {
		this.lastBackup = lastBackup;
	}

	public void saveName(String name) {

		String inputLine = null;
		try {
			APIrestful api = new APIrestful();
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("name", name);
			inputLine = api.put("system/" + ID, jsonParam.toString());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Could not get response from API");
		}

	}

	public String setProperty(String property, String value) {
		String inputLine = null;
		try {
			APIrestful api = new APIrestful();
			inputLine = api.put("system/" + ID + "/property/" + property, value);

		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not get response from API");
		}

		Gson gson = AppData.getGson();
		Response response = gson.fromJson(inputLine, Response.class);
		return response.getResponse();
	}

	public String deleteProperty(String property) {
		String inputLine = null;
		try {
			APIrestful api = new APIrestful();
			inputLine = api.delete("system/" + ID + "/property/" + property);

		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not get response from API");
		}

		Gson gson = AppData.getGson();
		Response response = gson.fromJson(inputLine, Response.class);
		return response.getResponse();
	}

	public SystemInfo() {

	}

	public SystemInfo(String dummy) {

		String inputLine = null;
		try {
			APIrestful api = new APIrestful();
			inputLine = api.get("system");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not get response from API");
		}

		Gson gson = AppData.getGson();
		SystemInfo systemInfo = gson.fromJson(inputLine, SystemInfo.class);
		this.type = ClusterComponent.CCType.system;
		this.ID = systemInfo.ID;
		this.name = systemInfo.name;
		this.startDate = systemInfo.startDate;
		this.lastStop = systemInfo.lastStop;
		this.lastAccess = systemInfo.lastAccess;
		this.nodes = systemInfo.nodes;
		this.lastCommand = systemInfo.lastCommand;
		this.lastBackup = systemInfo.lastBackup;
		this.properties = systemInfo.properties;
		this.health = systemInfo.health;
		this.connections = systemInfo.connections;
		this.packets = systemInfo.packets;

	}

	public String ToolTip() {

		return "<h2>Node</h2>" + "<ul>" + "<li><b>ID:</b> " + this.ID + "</li>" + "<li><b>Name:</b> " + this.name + "</li>" + "<li><b>Nodes:</b> "
				+ ((this.nodes == null) ? "n/a" : Arrays.toString(this.nodes)) + "</li>" + "<li><b>Start Date:</b> "
				+ ((this.startDate == null) ? "n/a" : this.startDate) + "</li>" + "<li><b>Last Stop:</b> " + ((this.lastStop == null) ? "n/a" : this.lastStop)
				+ "</li>" + "<li><b>Last Access:</b> " + ((this.lastAccess == null) ? "n/a" : this.lastAccess) + "</li>" + "<li><b>Last Backup:</b> "
				+ ((this.lastBackup == null) ? "n/a" : this.lastBackup) + "</li>" + "</ul>";

	}

}

class SystemInfoDeserializer implements JsonDeserializer<SystemInfo> {
	public SystemInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		SystemInfo systemInfo = new SystemInfo();

		JsonElement element;
		JsonObject jsonObject = json.getAsJsonObject();
		if (jsonObject.has("system")) {
			JsonArray array = jsonObject.get("system").getAsJsonArray();
			// for now just get the first system, ignore others
			jsonObject = array.get(0).getAsJsonObject();

			systemInfo.setID(((element = jsonObject.get("system")) == null || element.isJsonNull()) ? null : element.getAsString());
			systemInfo.setName(((element = jsonObject.get("name")) == null || element.isJsonNull()) ? null : element.getAsString());
			systemInfo.setStartDate(((element = jsonObject.get("startDate")) == null || element.isJsonNull()) ? null : element.getAsString());
			systemInfo.setLastAccess(((element = jsonObject.get("lastAccess")) == null || element.isJsonNull()) ? null : element.getAsString());
			systemInfo.setLastBackup(((element = jsonObject.get("lastBackup")) == null || element.isJsonNull()) ? null : element.getAsString());
			if ((element = jsonObject.get("health")) != null) {
				if ((element = element.getAsJsonArray()) != null && ((element = ((JsonArray) element).get(0)) != null) && !element.isJsonNull()) {
					systemInfo.setHealth(element.getAsString());
				}
			}
			if ((element = jsonObject.get("connections")) != null) {
				if ((element = element.getAsJsonArray()) != null && ((element = ((JsonArray) element).get(0)) != null) && !element.isJsonNull()) {
					systemInfo.setConnections(element.getAsString());
				}
			}
			if ((element = jsonObject.get("packets")) != null) {
				if ((element = element.getAsJsonArray()) != null && ((element = ((JsonArray) element).get(0)) != null) && !element.isJsonNull()) {
					systemInfo.setPackets(element.getAsString());
				}
			}

			element = jsonObject.get("nodes");
			if (element == null || element.isJsonNull()) {
				systemInfo.setNodes(null);
			} else {
				JsonArray nodesJson = element.getAsJsonArray();
				int length = nodesJson.size();

				String[] nodes = new String[length];
				for (int i = 0; i < nodesJson.size(); i++) {
					nodes[i] = nodesJson.get(i).getAsString();
				}
				systemInfo.setNodes(nodes);
			}

			element = jsonObject.get("properties");
			if (element == null || element.isJsonNull()) {
				systemInfo.setProperties(null);
			} else {
				JsonObject propertiesJson = element.getAsJsonObject();
				LinkedHashMap<String, String> properties = new LinkedHashMap<String, String>();
				String value;

				if ((element = propertiesJson.get(SystemInfo.PROPERTY_EIP)) != null) {
					properties.put(SystemInfo.PROPERTY_EIP, element.isJsonNull() ? null : element.getAsString());
				}

				if ((element = propertiesJson.get(SystemInfo.PROPERTY_MONYOG)) != null) {
					properties.put(SystemInfo.PROPERTY_MONYOG, element.isJsonNull() ? null : element.getAsString());
				}

				if ((element = propertiesJson.get(SystemInfo.PROPERTY_PHPMYADMIN)) != null) {
					properties.put(SystemInfo.PROPERTY_PHPMYADMIN, element.isJsonNull() ? null : element.getAsString());
				}

				if ((element = propertiesJson.get(SystemInfo.PROPERTY_DEFAULTMONITORINTERVAL)) != null) {
					properties.put(SystemInfo.PROPERTY_DEFAULTMONITORINTERVAL, element.isJsonNull() ? null : element.getAsString());
				}

				if ((element = propertiesJson.get(SystemInfo.PROPERTY_DEFAULTMAXBACKUPCOUNT)) != null) {
					properties.put(SystemInfo.PROPERTY_DEFAULTMAXBACKUPCOUNT, element.isJsonNull() ? null : element.getAsString());
				}

				if ((element = propertiesJson.get(SystemInfo.PROPERTY_DEFAULTMAXBACKUPSIZE)) != null) {
					properties.put(SystemInfo.PROPERTY_DEFAULTMAXBACKUPSIZE, element.isJsonNull() ? null : element.getAsString());
				}

				if ((element = propertiesJson.get(SystemInfo.PROPERTY_VERSION)) != null) {
					properties.put(SystemInfo.PROPERTY_VERSION, element.isJsonNull() ? null : element.getAsString());
				}

				if ((element = propertiesJson.get(SystemInfo.PROPERTY_SKIPLOGIN)) != null) {
					properties.put(SystemInfo.PROPERTY_SKIPLOGIN, element.isJsonNull() ? null : element.getAsString());
				}

				systemInfo.setProperties(properties);
			}

		}
		return systemInfo;
	}
}
