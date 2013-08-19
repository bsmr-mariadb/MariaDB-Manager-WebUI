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

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.skysql.manager.SystemRecord;
import com.skysql.manager.ui.ErrorDialog;

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

	public SystemRecord updateSystem(String systemID) {
		SystemInfo newSystemInfo = new SystemInfo(systemID);
		systemsMap.put(systemID, newSystemInfo.getCurrentSystem());
		return newSystemInfo.getCurrentSystem();
	}

	public boolean add(SystemRecord systemRecord) {

		try {
			APIrestful api = new APIrestful();
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("name", systemRecord.getName());
			jsonParam.put("systemtype", systemRecord.getSystemType());

			if (api.put("system/" + systemRecord.getID(), jsonParam.toString())) {
				WriteResponse writeResponse = APIrestful.getGson().fromJson(api.getResult(), WriteResponse.class);
				if (writeResponse != null && (!writeResponse.getInsertKey().isEmpty() || writeResponse.getUpdateCount() > 0)) {
					return true;
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
			new ErrorDialog(e, "JSON error encoding API request");
		}

		return false;

	}

	public boolean saveName(String name) {

		try {
			APIrestful api = new APIrestful();
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("name", name);
			api.put("system/" + currentID, jsonParam.toString());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			new ErrorDialog(e, "Error encoding API request");
			return false;
		}

	}

	public boolean setProperty(String property, String value) {
		APIrestful api = new APIrestful();

		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("value", value);
			if (api.put("system/" + currentID + "/property/" + property, jsonParam.toString())) {
				WriteResponse writeResponse = APIrestful.getGson().fromJson(api.getResult(), WriteResponse.class);
				if (writeResponse != null && (!writeResponse.getInsertKey().isEmpty() || writeResponse.getUpdateCount() > 0)) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			new ErrorDialog(e, "Error encoding API request");
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

	public SystemInfo(String systemID) {

		APIrestful api = new APIrestful();
		if (api.get("system" + (systemID != null ? "/" + systemID : ""))) {
			try {
				SystemInfo systemInfo = APIrestful.getGson().fromJson(api.getResult(), SystemInfo.class);
				this.systemsMap = systemInfo.systemsMap;
				// currently, get first system in the array
				if (systemsMap != null && !systemsMap.isEmpty()) {
					SystemRecord systemRecord = (SystemRecord) systemsMap.values().toArray()[0];
					this.currentID = systemRecord.getID();
				}
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

class SystemInfoDeserializer implements JsonDeserializer<SystemInfo> {
	public SystemInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException, NullPointerException {
		SystemInfo systemInfo = new SystemInfo();

		JsonArray array = null;

		int length = 0;
		if (json.getAsJsonObject().has("systems")) {
			array = json.getAsJsonObject().get("systems").getAsJsonArray();
			length = array.size();
		} else if (json.getAsJsonObject().has("system")) {
			length = 1;
		}

		LinkedHashMap<String, SystemRecord> systemsMap = new LinkedHashMap<String, SystemRecord>(length);
		systemInfo.setSystemsMap(systemsMap);
		if (length == 0) {
			return systemInfo;
		}

		// {"system":{"systemid":"1","systemtype":"aws","name":"sistema1","started":"Wed, 31 Jul 2013 18:48:41 +0000","lastaccess":"Wed, 31 Jul 2013 18:48:41 +0000","state":"running","nodes":["1"],"lastbackup":null,"properties":{},"monitorlatest":{"connections":null,"traffic":null,"availability":null,"nodestate":null,"capacity":null,"hoststate":null,"clustersize":null,"reppaused":null,"parallelism":null,"recvqueue":null,"flowcontrol":null,"sendqueue":null}},"warnings":["Caching directory \/usr\/local\/skysql\/cache\/api is not writeable, cannot write cache, please check existence, permissions, SELinux"]}

		for (int i = 0; i < length; i++) {
			JsonObject systemObject = (array != null) ? array.get(i).getAsJsonObject() : json.getAsJsonObject().get("system").getAsJsonObject();
			JsonElement element;
			String ID = (element = systemObject.get("systemid")).isJsonNull() ? null : element.getAsString();
			String type = (element = systemObject.get("systemtype")).isJsonNull() ? null : element.getAsString();
			String name = (element = systemObject.get("name")).isJsonNull() ? null : element.getAsString();
			String startDate = (element = systemObject.get("started")).isJsonNull() ? null : element.getAsString();
			String lastAccess = (element = systemObject.get("lastaccess")).isJsonNull() ? null : element.getAsString();
			String lastBackup = (element = systemObject.get("lastbackup")).isJsonNull() ? null : element.getAsString();

			String connections, traffic, availability, nodestate, capacity, hoststate, clustersize, reppaused, parallelism, recvqueue, flowcontrol, sendqueue;
			if (!(element = systemObject.get("monitorlatest")).isJsonNull()) {
				JsonObject monitorObject = element.getAsJsonObject();

				connections = (element = monitorObject.get("connections")).isJsonNull() ? null : element.getAsString();
				traffic = (element = monitorObject.get("traffic")).isJsonNull() ? null : element.getAsString();
				availability = (element = monitorObject.get("availability")).isJsonNull() ? null : element.getAsString();
				nodestate = (element = monitorObject.get("nodestate")).isJsonNull() ? null : element.getAsString();
				capacity = (element = monitorObject.get("capacity")).isJsonNull() ? null : element.getAsString();
				hoststate = (element = monitorObject.get("hoststate")).isJsonNull() ? null : element.getAsString();
				clustersize = (element = monitorObject.get("clustersize")).isJsonNull() ? null : element.getAsString();
				reppaused = (element = monitorObject.get("reppaused")).isJsonNull() ? null : element.getAsString();
				parallelism = (element = monitorObject.get("parallelism")).isJsonNull() ? null : element.getAsString();
				recvqueue = (element = monitorObject.get("recvqueue")).isJsonNull() ? null : element.getAsString();
				flowcontrol = (element = monitorObject.get("flowcontrol")).isJsonNull() ? null : element.getAsString();
				sendqueue = (element = monitorObject.get("sendqueue")).isJsonNull() ? null : element.getAsString();
			} else {
				connections = null;
				traffic = null;
				availability = null;
				nodestate = null;
				capacity = null;
				hoststate = null;
				clustersize = null;
				reppaused = null;
				parallelism = null;
				recvqueue = null;
				flowcontrol = null;
				sendqueue = null;
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

			SystemRecord systemRecord = new SystemRecord(ID, type, name, availability, connections, traffic, startDate, lastAccess, nodes, lastBackup,
					properties);
			systemsMap.put(ID, systemRecord);
		}

		return systemInfo;
	}
}
