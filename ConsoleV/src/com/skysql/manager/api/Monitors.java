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

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.skysql.manager.MonitorRecord;
import com.skysql.manager.api.Monitors.PermittedMonitorType;
import com.skysql.manager.ui.ErrorDialog;

// TODO: Auto-generated Javadoc
/**
 * The Class Monitors.
 */
public class Monitors {

	/**
	 * The Enum MonitorNames only includes some of the historic monitor names and is used in info panel and popups.
	 */
	public enum MonitorNames {

		/** The connections. */
		connections,
		/** The traffic. */
		traffic,
		/** The availability. */
		availability,
		/** The nodestate. */
		nodestate,
		/** The capacity. */
		capacity,
		/** The hoststate. */
		hoststate;
	}

	/**
	 * The Enum PermittedMonitorType determines which types of monitors are going to be visible to the user.
	 */
	public enum PermittedMonitorType {

		/** The sql. */
		SQL,
		/** The global. */
		GLOBAL,
		/** The js. */
		JS;
	}

	/**
	 * The Enum EditableMonitorType determines which types of monitors are going to be editable by the user.
	 */
	public enum EditableMonitorType {

		/** The sql. */
		SQL;
	}

	/** The monitors map. */
	private static LinkedHashMap<String, LinkedHashMap<String, MonitorRecord>> monitorsMap;

	/** The current list. */
	private static LinkedHashMap<String, MonitorRecord> currentList;

	/**
	 * Gets the monitors list.
	 *
	 * @return the monitors list
	 */
	public static LinkedHashMap<String, MonitorRecord> getMonitorsList() {
		return currentList;
	}

	/**
	 * Gets the monitors list.
	 *
	 * @param systemType the system type
	 * @return the monitors list
	 */
	public static LinkedHashMap<String, MonitorRecord> getMonitorsList(String systemType) {
		if (monitorsMap == null) {
			reloadMonitors();
		}
		currentList = monitorsMap.get(systemType);
		return currentList;
	}

	/**
	 * Gets the monitor.
	 *
	 * @param ID the id
	 * @return the monitor
	 */
	public static MonitorRecord getMonitor(String ID) {
		return (currentList != null ? currentList.get(ID) : null);
	}

	/**
	 * Sets the monitors map.
	 *
	 * @param monitorsMap the monitors map
	 */
	protected void setMonitorsMap(LinkedHashMap<String, LinkedHashMap<String, MonitorRecord>> monitorsMap) {
		Monitors.monitorsMap = monitorsMap;
	}

	/**
	 * Instantiates a new monitors.
	 */
	public Monitors() {

	}

	/**
	 * Reload list of monitors from API.
	 */
	public synchronized static void reloadMonitors() {

		APIrestful api = new APIrestful();
		if (api.get("monitorclass")) {
			try {
				Monitors monitors = APIrestful.getGson().fromJson(api.getResult(), Monitors.class);
				Monitors.monitorsMap = monitors.monitorsMap;
			} catch (NullPointerException e) {
				new ErrorDialog(e, "API did not return expected result for:" + api.errorString());
				throw new RuntimeException("API response");
			} catch (JsonParseException e) {
				new ErrorDialog(e, "JSON parse error in API results for:" + api.errorString());
				throw new RuntimeException("API response");
			}
		}

	}

	/**
	 * Writes the monitor to the API.
	 *
	 * @param monitor the monitor
	 * @return true, if successful
	 */
	public synchronized static boolean setMonitor(MonitorRecord monitor) {

		APIrestful api = new APIrestful();

		try {
			boolean success;

			JSONObject jsonParam = new JSONObject();
			jsonParam.put("name", monitor.getName());
			jsonParam.put("description", monitor.getDescription());
			jsonParam.put("sql", monitor.getSql());
			jsonParam.put("unit", monitor.getUnit());
			jsonParam.put("delta", monitor.isDelta() ? "1" : "0");
			jsonParam.put("systemaverage", monitor.isAverage() ? "1" : "0");
			jsonParam.put("monitortype", "SQL");

			// type needs to indicate int/float/string etc. instead of LineChart/AreaChart
			jsonParam.put("type", monitor.getChartType());
			int interval = monitor.getInterval();
			jsonParam.put("interval", String.valueOf(interval));

			if (monitor.getID() == null) {
				monitor.setID(createUniqueID(monitor.getName()));
			}

			success = api.put("monitorclass/" + monitor.getSystemType() + "/key/" + monitor.getID(), jsonParam.toString());

		} catch (JSONException e) {
			new ErrorDialog(e, "Error encoding API request");
			throw new RuntimeException("Error encoding API request");
		}

		WriteResponse writeResponse = APIrestful.getGson().fromJson(api.getResult(), WriteResponse.class);
		if (writeResponse != null && (!writeResponse.getInsertKey().isEmpty() || writeResponse.getUpdateCount() > 0)) {
			currentList.put(monitor.getID(), monitor);
			return true;
		} else {
			return false;
		}

	}

	/**
	 * Checks if the proposed monitor key is unique.
	 *
	 * @param proposedName the proposed name
	 * @return true, if is name unique
	 */
	public static boolean isNameUnique(String proposedName) {
		for (Map.Entry<String, MonitorRecord> entry : currentList.entrySet()) {
			MonitorRecord monitor = entry.getValue();
			if (monitor.getName().equals(proposedName)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Creates the unique monitor ID (API: monitor key).
	 *
	 * @param name the name
	 * @return the string
	 */
	private synchronized static String createUniqueID(String name) {
		String ID = name.replaceAll("[^a-zA-Z0-9.-]", "");
		int i = 0;
		String uniqueID = ID;
		while (Monitors.getMonitor(uniqueID) != null) {
			uniqueID = ID + i++;
		}
		return uniqueID;
	}

	/**
	 * Deletes monitor from API.
	 *
	 * @param monitor the monitor
	 * @return true, if successful
	 */
	public synchronized static boolean deleteMonitor(MonitorRecord monitor) {

		APIrestful api = new APIrestful();
		if (api.delete("monitorclass/" + monitor.getSystemType() + "/key/" + monitor.getID())) {
			WriteResponse writeResponse = APIrestful.getGson().fromJson(api.getResult(), WriteResponse.class);
			if (writeResponse != null && writeResponse.getDeleteCount() > 0) {
				currentList.remove(monitor.getID());
				return true;
			}
		}
		return false;
	}

}

/***
// {"monitorclasses":[
{"systemtype":"aws","monitor":"connections","name":"Connections","sql":"select variable_value from global_status where variable_name = \"THREADS_CONNECTED\";","description":"","charttype":"LineChart","delta":"0","monitortype":"SQL","systemaverage":"0","interval":"30","unit":null,"monitorid":"1"},
{"systemtype":"aws","monitor":"traffic","name":"Network Traffic","sql":"select round(sum(variable_value) \/ 1024) from global_status where variable_name in (\"BYTES_RECEIVED\", \"BYTES_SENT\");","description":"","charttype":"LineChart","delta":"1","monitortype":"SQL","systemaverage":"0","interval":"30","unit":"kB\/min","monitorid":"2"},
{"systemtype":"aws","monitor":"availability","name":"Availability","sql":"select 100;","description":"","charttype":"LineChart","delta":"0","monitortype":"SQL","systemaverage":"1","interval":"30","unit":"%","monitorid":"3"},
{"systemtype":"aws","monitor":"nodestate","name":"Node State","sql":"crm status bynode","description":"","charttype":null,"delta":"0","monitortype":"CRM","systemaverage":"0","interval":"30","unit":null,"monitorid":"4"},
{"systemtype":"aws","monitor":"capacity","name":"Capacity","sql":"select round(((select variable_value from global_status where variable_name = \"THREADS_CONNECTED\") * 100) \/ variable_value) from global_variables where variable_name = \"MAX_CONNECTIONS\";","description":"","charttype":null,"delta":"0","monitortype":"SQL","systemaverage":"1","interval":"30","unit":"%","monitorid":"5"},
{"systemtype":"aws","monitor":"hoststate","name":"Host State","sql":"","description":"","charttype":null,"delta":"0","monitortype":"PING","systemaverage":"0","interval":"30","unit":null,"monitorid":"6"},
{"systemtype":"galera","monitor":"connections","name":"Connections","sql":"select variable_value from global_status where variable_name = \"THREADS_CONNECTED\";","description":"","charttype":"LineChart","delta":"0","monitortype":"SQL","systemaverage":"0","interval":"30","unit":null,"monitorid":"7"},
{"systemtype":"galera","monitor":"traffic","name":"Network Traffic","sql":"select round(sum(variable_value) \/ 1024) from global_status where variable_name in (\"BYTES_RECEIVED\", \"BYTES_SENT\");","description":"","charttype":"LineChart","delta":"1","monitortype":"SQL","systemaverage":"0","interval":"30","unit":"kB\/min","monitorid":"8"},
{"systemtype":"galera","monitor":"availability","name":"Availability","sql":"select 100;","description":"","charttype":"LineChart","delta":"0","monitortype":"SQL","systemaverage":"1","interval":"30","unit":"%","monitorid":"9"},
{"systemtype":"galera","monitor":"capacity","name":"Capacity","sql":"select round(((select variable_value from global_status where variable_name = \"THREADS_CONNECTED\") * 100) \/ variable_value) from global_variables where variable_name = \"MAX_CONNECTIONS\";","description":"","charttype":null,"delta":"0","monitortype":"SQL","systemaverage":"1","interval":"30","unit":"%","monitorid":"10"},
{"systemtype":"galera","monitor":"hoststate","name":"Host State","sql":"","description":"","charttype":null,"delta":"0","monitortype":"PING","systemaverage":"0","interval":"30","unit":null,"monitorid":"11"},
{"systemtype":"galera","monitor":"nodestate","name":"NodeState","sql":"select 100 + variable_value from global_status where variable_name = \"WSREP_LOCAL_STATE\" union select 107 limit 1;","description":"","charttype":null,"delta":"0","monitortype":"SQL_NODE_STATE","systemaverage":"1","interval":"30","unit":null,"monitorid":"12"},
{"systemtype":"galera","monitor":"clustersize","name":"Cluster Size","sql":"select variable_value from global_status where variable_name = \"WSREP_CLUSTER_SIZE\";","description":"Number of nodes in the cluster","charttype":"LineChart","delta":"0","monitortype":"SQL","systemaverage":"1","interval":"30","unit":null,"monitorid":"13"},
{"systemtype":"galera","monitor":"reppaused","name":"Replication Paused","sql":"select variable_value * 100 from global_status where variable_name = \"WSREP_FLOW_CONTROL_PAUSED\";","description":"Percentage of time for which replication was paused","charttype":"LineChart","delta":"0","monitortype":"SQL","systemaverage":"1","interval":"30","unit":"%","monitorid":"14"},
{"systemtype":"galera","monitor":"parallelism","name":"Parallelism","sql":"select variable_value from global_status where variable_name = \"WSREP_CERT_DEPS_DISTANCE\";","description":"Average No. of parallel transactions","charttype":"LineChart","delta":"0","monitortype":"SQL","systemaverage":"1","interval":"30","unit":null,"monitorid":"15"},
{"systemtype":"galera","monitor":"recvqueue","name":"Avg Receive Queue","sql":"select variable_value from global_status where variable_name = \"WSREP_LOCAL_RECV_QUEUE_AVG\";","description":"Average receive queue length","charttype":"LineChart","delta":"0","monitortype":"SQL","systemaverage":"1","interval":"30","unit":null,"monitorid":"16"},
{"systemtype":"galera","monitor":"flowcontrol","name":"Flow Controlled","sql":"select variable_value from global_status where variable_name = \"WSREP_FLOW_CONTROL_SENT\";","description":"Flow control messages sent","charttype":"LineChart","delta":"1","monitortype":"SQL","systemaverage":"0","interval":"30","unit":null,"monitorid":"17"},
{"systemtype":"galera","monitor":"sendqueue","name":"Avg Send Queue","sql":"select variable_value from global_status where variable_name = \"WSREP_LOCAL_SEND_QUEUE_AVG\";","description":"Average length of send queue","charttype":"LineChart","delta":"0","monitortype":"SQL","systemaverage":"1","interval":"30","unit":null,"monitorid":"18"}],

// {"monitorclass":[
{"systemtype":"aws","monitor":"connections","name":"Connections","sql":"select variable_value from global_status where variable_name = \"THREADS_CONNECTED\";","description":"","charttype":"LineChart","delta":"0","monitortype":"SQL","systemaverage":"0","interval":"30","unit":null,"monitorid":"1"},
{"systemtype":"aws","monitor":"traffic","name":"Network Traffic","sql":"select round(sum(variable_value) \/ 1024) from global_status where variable_name in (\"BYTES_RECEIVED\", \"BYTES_SENT\");","description":"","charttype":"LineChart","delta":"1","monitortype":"SQL","systemaverage":"0","interval":"30","unit":"kB\/min","monitorid":"2"},
{"systemtype":"aws","monitor":"availability","name":"Availability","sql":"select 100;","description":"","charttype":"LineChart","delta":"0","monitortype":"SQL","systemaverage":"1","interval":"30","unit":"%","monitorid":"3"},
{"systemtype":"aws","monitor":"nodestate","name":"Node State","sql":"crm status bynode","description":"","charttype":null,"delta":"0","monitortype":"CRM","systemaverage":"0","interval":"30","unit":null,"monitorid":"4"},
{"systemtype":"aws","monitor":"capacity","name":"Capacity","sql":"select round(((select variable_value from global_status where variable_name = \"THREADS_CONNECTED\") * 100) \/ variable_value) from global_variables where variable_name = \"MAX_CONNECTIONS\";","description":"","charttype":null,"delta":"0","monitortype":"SQL","systemaverage":"1","interval":"30","unit":"%","monitorid":"5"},
{"systemtype":"aws","monitor":"hoststate","name":"Host State","sql":"","description":"","charttype":null,"delta":"0","monitortype":"PING","systemaverage":"0","interval":"30","unit":null,"monitorid":"6"}]
***/

/***
{"monitorclasses":
{"aws":[
	{"systemtype":"aws","monitor":"connections","name":"Connections","sql":"select variable_value from global_status where variable_name = \"THREADS_CONNECTED\";","description":"","charttype":"LineChart","delta":"0","monitortype":"SQL","systemaverage":"0","interval":"30","unit":null,"monitorid":"1"},
	{"systemtype":"aws","monitor":"traffic","name":"Network Traffic","sql":"select round(sum(variable_value) \/ 1024) from global_status where variable_name in (\"BYTES_RECEIVED\", \"BYTES_SENT\");","description":"","charttype":"LineChart","delta":"1","monitortype":"SQL","systemaverage":"0","interval":"30","unit":"kB\/min","monitorid":"2"},
	{"systemtype":"aws","monitor":"availability","name":"Availability","sql":"select 100;","description":"","charttype":"LineChart","delta":"0","monitortype":"SQL","systemaverage":"1","interval":"30","unit":"%","monitorid":"3"},{"systemtype":"aws","monitor":"nodestate","name":"Node State","sql":"crm status bynode","description":"","charttype":null,"delta":"0","monitortype":"CRM","systemaverage":"0","interval":"30","unit":null,"monitorid":"4"},
	{"systemtype":"aws","monitor":"capacity","name":"Capacity","sql":"select round(((select variable_value from global_status where variable_name = \"THREADS_CONNECTED\") * 100) \/ variable_value) from global_variables where variable_name = \"MAX_CONNECTIONS\";","description":"","charttype":null,"delta":"0","monitortype":"SQL","systemaverage":"1","interval":"30","unit":"%","monitorid":"5"},
	{"systemtype":"aws","monitor":"hoststate","name":"Host State","sql":"","description":"","charttype":null,"delta":"0","monitortype":"PING","systemaverage":"0","interval":"30","unit":null,"monitorid":"6"}],
"galera":[
	{"systemtype":"galera","monitor":"connections","name":"Connections","sql":"select variable_value from global_status where variable_name = \"THREADS_CONNECTED\";","description":"","charttype":"LineChart","delta":"0","monitortype":"SQL","systemaverage":"0","interval":"30","unit":null,"monitorid":"7"},
	{"systemtype":"galera","monitor":"traffic","name":"Network Traffic","sql":"select round(sum(variable_value) \/ 1024) from global_status where variable_name in (\"BYTES_RECEIVED\", \"BYTES_SENT\");","description":"","charttype":"LineChart","delta":"1","monitortype":"SQL","systemaverage":"0","interval":"30","unit":"kB\/min","monitorid":"8"},
	{"systemtype":"galera","monitor":"availability","name":"Availability","sql":"select 100;","description":"","charttype":"LineChart","delta":"0","monitortype":"SQL","systemaverage":"1","interval":"30","unit":"%","monitorid":"9"},{"systemtype":"galera","monitor":"capacity","name":"Capacity","sql":"select round(((select variable_value from global_status where variable_name = \"THREADS_CONNECTED\") * 100) \/ variable_value) from global_variables where variable_name = \"MAX_CONNECTIONS\";","description":"","charttype":null,"delta":"0","monitortype":"SQL","systemaverage":"1","interval":"30","unit":"%","monitorid":"10"},
	{"systemtype":"galera","monitor":"hoststate","name":"Host State","sql":"","description":"","charttype":null,"delta":"0","monitortype":"PING","systemaverage":"0","interval":"30","unit":null,"monitorid":"11"},
	{"systemtype":"galera","monitor":"nodestate","name":"NodeState","sql":"select 100 + variable_value from global_status where variable_name = \"WSREP_LOCAL_STATE\" union select 107 limit 1;","description":"","charttype":null,"delta":"0","monitortype":"SQL_NODE_STATE","systemaverage":"1","interval":"30","unit":null,"monitorid":"12"},
	{"systemtype":"galera","monitor":"clustersize","name":"Cluster Size","sql":"select variable_value from global_status where variable_name = \"WSREP_CLUSTER_SIZE\";","description":"Number of nodes in the cluster","charttype":"LineChart","delta":"0","monitortype":"SQL","systemaverage":"1","interval":"30","unit":null,"monitorid":"13"},
	{"systemtype":"galera","monitor":"reppaused","name":"Replication Paused","sql":"select variable_value * 100 from global_status where variable_name = \"WSREP_FLOW_CONTROL_PAUSED\";","description":"Percentage of time for which replication was paused","charttype":"LineChart","delta":"0","monitortype":"SQL","systemaverage":"1","interval":"30","unit":"%","monitorid":"14"},
	{"systemtype":"galera","monitor":"parallelism","name":"Parallelism","sql":"select variable_value from global_status where variable_name = \"WSREP_CERT_DEPS_DISTANCE\";","description":"Average No. of parallel transactions","charttype":"LineChart","delta":"0","monitortype":"SQL","systemaverage":"1","interval":"30","unit":null,"monitorid":"15"},
	{"systemtype":"galera","monitor":"recvqueue","name":"Avg Receive Queue","sql":"select variable_value from global_status where variable_name = \"WSREP_LOCAL_RECV_QUEUE_AVG\";","description":"Average receive queue length","charttype":"LineChart","delta":"0","monitortype":"SQL","systemaverage":"1","interval":"30","unit":null,"monitorid":"16"},
	{"systemtype":"galera","monitor":"flowcontrol","name":"Flow Controlled","sql":"select variable_value from global_status where variable_name = \"WSREP_FLOW_CONTROL_SENT\";","description":"Flow control messages sent","charttype":"LineChart","delta":"1","monitortype":"SQL","systemaverage":"0","interval":"30","unit":null,"monitorid":"17"},
	{"systemtype":"galera","monitor":"sendqueue","name":"Avg Send Queue","sql":"select variable_value from global_status where variable_name = \"WSREP_LOCAL_SEND_QUEUE_AVG\";","description":"Average length of send queue","charttype":"LineChart","delta":"0","monitortype":"SQL","systemaverage":"1","interval":"30","unit":null,"monitorid":"18"}]}
***/

class MonitorsDeserializer implements JsonDeserializer<Monitors> {
	public Monitors deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException, NullPointerException {
		Monitors monitors = new Monitors();

		JsonArray array = null;
		JsonObject object = null;

		if (json.getAsJsonObject().has("monitorclasses")) {
			JsonElement monitorsElement = json.getAsJsonObject().get("monitorclasses");
			if (monitorsElement.isJsonArray()) {
				array = monitorsElement.getAsJsonArray();
			} else {
				object = monitorsElement.getAsJsonObject();
			}
		} else if (json.getAsJsonObject().has("monitorclass")) {
			array = json.getAsJsonObject().get("monitorclass").getAsJsonArray();
		} else {
			monitors.setMonitorsMap(null);
			return monitors;
		}

		LinkedHashMap<String, LinkedHashMap<String, MonitorRecord>> monitorsMap = new LinkedHashMap<String, LinkedHashMap<String, MonitorRecord>>();
		monitors.setMonitorsMap(monitorsMap);
		for (String type : SystemTypes.getList().keySet()) {
			monitorsMap.put(type, new LinkedHashMap<String, MonitorRecord>());
		}

		if (array != null) {
			parseMonitors(array, monitorsMap);
		} else {
			for (String type : SystemTypes.getList().keySet()) {
				array = object.get(type).getAsJsonArray();
				parseMonitors(array, monitorsMap);
			}
		}

		return monitors;
	}

	private void parseMonitors(JsonArray array, LinkedHashMap<String, LinkedHashMap<String, MonitorRecord>> monitorsMap) {

		for (int i = 0; i < array.size(); i++) {
			JsonObject jsonObject = array.get(i).getAsJsonObject();
			JsonElement element;
			String systemType = (element = jsonObject.get("systemtype")).isJsonNull() ? null : element.getAsString();
			String id = (element = jsonObject.get("monitor")).isJsonNull() ? null : element.getAsString();
			String name = (element = jsonObject.get("name")).isJsonNull() ? null : element.getAsString();
			String description = (element = jsonObject.get("description")).isJsonNull() ? null : element.getAsString();
			String unit = (element = jsonObject.get("unit")).isJsonNull() ? null : element.getAsString();
			String monitorType = (element = jsonObject.get("monitortype")).isJsonNull() ? null : element.getAsString();
			boolean delta = (element = jsonObject.get("delta")).isJsonNull() ? false : element.getAsBoolean();
			boolean average = (element = jsonObject.get("systemaverage")).isJsonNull() ? false : element.getAsBoolean();
			String chartType = (element = jsonObject.get("charttype")).isJsonNull() ? null : element.getAsString();
			String intervalString = (element = jsonObject.get("interval")).isJsonNull() ? null : element.getAsString();
			int interval = (intervalString != null && !intervalString.isEmpty()) ? Integer.valueOf(intervalString) : 0;
			String sql = (element = jsonObject.get("sql")).isJsonNull() ? null : element.getAsString();
			for (PermittedMonitorType permitted : PermittedMonitorType.values()) {
				if (permitted.name().equals(monitorType)) {
					MonitorRecord monitorRecord = new MonitorRecord(systemType, id, name, description, unit, monitorType, delta, average, chartType, interval,
							sql);
					monitorsMap.get(systemType).put(id, monitorRecord);
					break;
				}
			}

		}

	}
}
