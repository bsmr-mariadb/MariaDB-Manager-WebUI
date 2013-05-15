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
import java.util.LinkedHashMap;

import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.skysql.manager.SystemRecord;

public class SystemInfo {

	public static final String SYSTEM_NODEID = "0";

	public static final String PROPERTY_EIP = "EIP";
	public static final String PROPERTY_MONYOG = "MONyog";
	public static final String PROPERTY_PHPMYADMIN = "phpMyAdmin";
	public static final String PROPERTY_DEFAULTMONITORINTERVAL = "MonitorInterval";
	public static final String PROPERTY_DEFAULTMAXBACKUPCOUNT = "maxBackupCount";
	public static final String PROPERTY_DEFAULTMAXBACKUPSIZE = "maxBackupSize";
	public static final String PROPERTY_VERSION = "VERSION";
	public static final String PROPERTY_SKIPLOGIN = "SKIPLOGIN";

	private LinkedHashMap<String, SystemRecord> systemsMap;
	private String currentID;

	public LinkedHashMap<String, SystemRecord> getSystemsMap() {
		return systemsMap;
	}

	protected void setSystemsMap(LinkedHashMap<String, SystemRecord> systemsMap) {
		this.systemsMap = systemsMap;
	}

	public String getCurrentID() {
		return currentID;
	}

	public SystemRecord getSystemRecord(String key) {
		return systemsMap.get(key);
	}

	public SystemRecord getCurrentSystem() {
		return systemsMap.get(currentID);
	}

	public SystemRecord update(String ID) {
		// TODO: this should get an update for the given system
		return systemsMap.get(currentID);
	}

	public boolean add(SystemRecord systemRecord) {

		try {
			APIrestful api = new APIrestful();
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("name", systemRecord.getName());
			if (api.put("system/" + systemRecord.getID(), jsonParam.toString())) {
				WriteResponse writeResponse = APIrestful.getGson().fromJson(api.getResult(), WriteResponse.class);
				if (writeResponse != null && (!writeResponse.getInsertKey().isEmpty() || writeResponse.getUpdateCount() > 0)) {
					return true;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;

	}

	public void saveName(String name) {

		try {
			APIrestful api = new APIrestful();
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("name", name);
			api.put("system/" + currentID, jsonParam.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public boolean setProperty(String property, String value) {
		APIrestful api = new APIrestful();
		if (api.put("system/" + currentID + "/property/" + property, value)) {
			WriteResponse writeResponse = APIrestful.getGson().fromJson(api.getResult(), WriteResponse.class);
			if (writeResponse != null && (!writeResponse.getInsertKey().isEmpty() || writeResponse.getUpdateCount() > 0)) {
				return true;
			}
		}
		return false;

	}

	public boolean deleteProperty(String property) {
		APIrestful api = new APIrestful();
		if (api.delete("system/" + currentID + "/property/" + property)) {
			WriteResponse writeResponse = APIrestful.getGson().fromJson(api.getResult(), WriteResponse.class);
			if (writeResponse != null && writeResponse.getDeleteCount() > 0) {
				return true;
			}
		}
		return false;

	}

	public SystemInfo() {

	}

	public SystemInfo(String dummy) {

		APIrestful api = new APIrestful();
		if (api.get("system")) {
			SystemInfo systemInfo = APIrestful.getGson().fromJson(api.getResult(), SystemInfo.class);
			this.systemsMap = systemInfo.systemsMap;
			// currently, get first system in the array
			if (systemsMap != null && !systemsMap.isEmpty()) {
				SystemRecord systemRecord = (SystemRecord) systemsMap.values().toArray()[0];
				this.currentID = systemRecord.getID();
			}
		}

	}

}

// updated 2013-05-13
//{"system":[{"system":"1","name":"Galera Cluster","startDate":null,"lastAccess":null,"state":"1","nodes":["1","2","3"],"lastBackup":"2013-04-30 10:00:00","properties":{"IPMonitor":"false"},"commands":["2","3"],"connections":[null],"packets":[null],"health":[null]},{"system":"2","name":"Pippo","startDate":"2013-05-09 21:56:37","lastAccess":"2013-05-09 21:56:37","state":null,"nodes":["1","33"],"lastBackup":null,"commands":null,"connections":[null],"packets":[null],"health":[null]}]}

class SystemInfoDeserializer implements JsonDeserializer<SystemInfo> {
	public SystemInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		SystemInfo systemInfo = new SystemInfo();

		JsonObject jsonObject = json.getAsJsonObject();
		if (!jsonObject.has("system")) {
			return null;
		}

		JsonArray array = jsonObject.get("system").getAsJsonArray();
		int length = array.size();

		LinkedHashMap<String, SystemRecord> systemsMap = new LinkedHashMap<String, SystemRecord>(length);
		systemInfo.setSystemsMap(systemsMap);
		if (length == 0) {
			return systemInfo;
		}

		for (int i = 0; i < length; i++) {
			JsonObject systemObject = array.get(i).getAsJsonObject();
			JsonElement element;
			String ID = (element = systemObject.get("system")).isJsonNull() ? null : element.getAsString();
			String name = (element = systemObject.get("name")).isJsonNull() ? null : element.getAsString();
			String startDate = (element = systemObject.get("startDate")).isJsonNull() ? null : element.getAsString();
			String lastAccess = (element = systemObject.get("lastAccess")).isJsonNull() ? null : element.getAsString();
			String lastBackup = (element = systemObject.get("lastBackup")).isJsonNull() ? null : element.getAsString();

			String health = null;
			if ((element = systemObject.get("health")) != null) {
				if ((element = element.getAsJsonArray()) != null && ((element = ((JsonArray) element).get(0)) != null) && !element.isJsonNull()) {
					health = element.getAsString();
				}
			}

			String connections = null;
			if ((element = systemObject.get("connections")) != null) {
				if ((element = element.getAsJsonArray()) != null && ((element = ((JsonArray) element).get(0)) != null) && !element.isJsonNull()) {
					connections = element.getAsString();
				}
			}

			String packets = null;
			if ((element = systemObject.get("packets")) != null) {
				if ((element = element.getAsJsonArray()) != null && ((element = ((JsonArray) element).get(0)) != null) && !element.isJsonNull()) {
					packets = element.getAsString();
				}
			}

			String[] nodes = null;
			if ((element = systemObject.get("nodes")) != null && !element.isJsonNull()) {
				JsonArray nodesJson = element.getAsJsonArray();
				int nodesCount = nodesJson.size();

				nodes = new String[nodesCount];
				for (int nodesIndex = 0; nodesIndex < nodesCount; nodesIndex++) {
					nodes[nodesIndex] = nodesJson.get(nodesIndex).getAsString();
				}
			}

			LinkedHashMap<String, String> properties = new LinkedHashMap<String, String>();
			if ((element = systemObject.get("properties")) != null && !element.isJsonNull()) {

				JsonObject propertiesJson = element.getAsJsonObject();

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

			}

			SystemRecord systemRecord = new SystemRecord(ID, name, health, connections, packets, startDate, lastAccess, nodes, lastBackup, properties);
			systemsMap.put(ID, systemRecord);
		}

		return systemInfo;
	}
}
