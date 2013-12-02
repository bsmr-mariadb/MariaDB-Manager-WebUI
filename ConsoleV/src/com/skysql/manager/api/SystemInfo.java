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
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.skysql.manager.MonitorLatest;
import com.skysql.manager.SystemRecord;
import com.skysql.manager.ui.ErrorDialog;

public class SystemInfo {

	public static final String SYSTEM_NODEID = "0";
	public static final String SYSTEM_ROOT = "0";

	public static final String PROPERTY_EIP = "EIP";
	public static final String PROPERTY_MONYOG = "MONyog";
	public static final String PROPERTY_PHPMYADMIN = "phpMyAdmin";
	public static final String PROPERTY_DEFAULTMONITORINTERVAL = "MonitorInterval";
	public static final String PROPERTY_DEFAULTMAXBACKUPCOUNT = "maxBackupCount";
	public static final String PROPERTY_DEFAULTMAXBACKUPSIZE = "maxBackupSize";
	public static final String PROPERTY_VERSION = "VERSION";
	public static final String PROPERTY_SKIPLOGIN = "SKIPLOGIN";

	private LinkedHashMap<String, SystemRecord> systemsMap;
	private String currentID = SYSTEM_ROOT;

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

	public void setCurrentSystem(String systemID) {
		this.currentID = systemID;
	}

	public SystemRecord updateSystem(String systemID) {
		SystemInfo newSystemInfo = new SystemInfo(systemID);
		SystemRecord systemRecord = newSystemInfo.getSystemRecord(systemID);
		if (systemID.equals(SYSTEM_ROOT)) {
			systemsMap = newSystemInfo.systemsMap;
		} else {
			SystemRecord oldSystemRecord = systemsMap.get(systemID);
			if (oldSystemRecord != null) {
				systemRecord.setButton(oldSystemRecord.getButton());
			}
			systemsMap.put(systemID, systemRecord);
		}
		return (systemRecord);
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
		} catch (JSONException e) {
			new ErrorDialog(e, "Error encoding API request");
			throw new RuntimeException("Error encoding API request");
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
		if (api.get("system" + (systemID != null && !systemID.equals(SYSTEM_ROOT) ? "/" + systemID : ""))) {
			try {
				SystemInfo systemInfo = APIrestful.getGson().fromJson(api.getResult(), SystemInfo.class);
				this.systemsMap = systemInfo.systemsMap;
				// if there's only one system, select it
				if (systemID.equals(SYSTEM_ROOT) && systemsMap != null && systemsMap.size() == 2) {
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

// {"system":{"systemid":"1","systemtype":"aws","name":"sistema1","started":"Wed, 31 Jul 2013 18:48:41 +0000","lastaccess":"Wed, 31 Jul 2013 18:48:41 +0000","state":"running","nodes":["1"],"lastbackup":null,"properties":{},"monitorlatest":{"connections":null,"traffic":null,"availability":null,"nodestate":null,"capacity":null,"hoststate":null,"clustersize":null,"reppaused":null,"parallelism":null,"recvqueue":null,"flowcontrol":null,"sendqueue":null}},"warnings":["Caching directory \/usr\/local\/skysql\/cache\/api is not writeable, cannot write cache, please check existence, permissions, SELinux"]}

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

		for (int i = 0; i < length; i++) {

			SystemRecord systemRecord = new SystemRecord(SystemInfo.SYSTEM_ROOT);

			JsonObject systemObject = (array != null) ? array.get(i).getAsJsonObject() : json.getAsJsonObject().get("system").getAsJsonObject();
			JsonElement element;
			systemRecord.setID((element = systemObject.get("systemid")).isJsonNull() ? null : element.getAsString());
			systemRecord.setSystemType((element = systemObject.get("systemtype")).isJsonNull() ? null : element.getAsString());
			systemRecord.setName((element = systemObject.get("name")).isJsonNull() ? null : element.getAsString());
			systemRecord.setState((element = systemObject.get("state")).isJsonNull() ? null : element.getAsString());
			systemRecord.setStartDate((element = systemObject.get("started")).isJsonNull() ? null : element.getAsString());
			systemRecord.setLastAccess((element = systemObject.get("lastaccess")).isJsonNull() ? null : element.getAsString());
			systemRecord.setLastBackup((element = systemObject.get("lastbackup")).isJsonNull() ? null : element.getAsString());
			systemRecord.setDBUsername((element = systemObject.get("dbusername")).isJsonNull() ? null : element.getAsString());
			systemRecord.setDBPassword((element = systemObject.get("dbpassword")).isJsonNull() ? null : element.getAsString());
			systemRecord.setRepUsername((element = systemObject.get("repusername")).isJsonNull() ? null : element.getAsString());
			systemRecord.setRepPassword((element = systemObject.get("reppassword")).isJsonNull() ? null : element.getAsString());
			systemRecord.setLastMonitored(((element = systemObject.get("lastmonitored")).isJsonNull()) ? null : element.getAsString());

			MonitorLatest monitorLatest = null;
			if ((element = systemObject.get("monitorlatest")) != null && !element.isJsonNull()) {
				monitorLatest = APIrestful.getGson().fromJson(element.toString(), MonitorLatest.class);
			}
			systemRecord.setMonitorLatest(monitorLatest);

			String[] nodes = null;
			if ((element = systemObject.get("nodes")) != null && !element.isJsonNull()) {
				JsonArray nodesJson = element.getAsJsonArray();
				int nodesCount = nodesJson.size();

				nodes = new String[nodesCount];
				for (int nodesIndex = 0; nodesIndex < nodesCount; nodesIndex++) {
					nodes[nodesIndex] = nodesJson.get(nodesIndex).getAsString();
				}
			}
			systemRecord.setNodes(nodes);

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
			systemRecord.setProperties(properties);

			systemsMap.put(systemRecord.getID(), systemRecord);
		}

		if (array != null) {
			// create a "ROOT" system record to contain the series of flat systems; in a hierarchical organization of systems, this might be provided by the API
			SystemRecord rootRecord = new SystemRecord(null);
			rootRecord.setID(SystemInfo.SYSTEM_ROOT);
			rootRecord.setName("Root");
			String[] systems = new String[systemsMap.keySet().size()];
			int i = 0;
			for (String systemID : systemsMap.keySet()) {
				systems[i++] = systemID;
			}
			rootRecord.setNodes(systems);
			systemsMap.put(SystemInfo.SYSTEM_ROOT, rootRecord);
		}

		return systemInfo;
	}
}
