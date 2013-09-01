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
import java.net.URLEncoder;
import java.util.LinkedHashMap;

import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.skysql.manager.MonitorRecord;
import com.skysql.manager.ui.ErrorDialog;

public class Monitors {

	//public static final String MONITOR_CAPACITY = "5";

	private static LinkedHashMap<String, LinkedHashMap<String, MonitorRecord>> monitorsMap;
	private static LinkedHashMap<String, MonitorRecord> currentList;

	public static LinkedHashMap<String, MonitorRecord> getMonitorsList() {
		return currentList;
	}

	public static LinkedHashMap<String, MonitorRecord> getMonitorsList(String systemType) {
		if (monitorsMap == null) {
			reloadMonitors();
		}
		currentList = monitorsMap.get(systemType);
		return currentList;
	}

	public static MonitorRecord getMonitor(String ID) {
		return (currentList != null ? currentList.get(ID) : null);
	}

	protected void setMonitorsMap(LinkedHashMap<String, LinkedHashMap<String, MonitorRecord>> monitorsMap) {
		Monitors.monitorsMap = monitorsMap;
	}

	public Monitors() {

	}

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

	public synchronized static String setMonitor(MonitorRecord monitor) {

		APIrestful api = new APIrestful();

		try {
			boolean success;
			if (monitor.getID() != null) {

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

				success = api.put("monitorclass/" + monitor.getID(), jsonParam.toString());
			} else {

				StringBuffer regParam = new StringBuffer();
				regParam.append("name=" + URLEncoder.encode(monitor.getName(), "UTF-8"));
				regParam.append("&description=" + URLEncoder.encode(monitor.getDescription(), "UTF-8"));
				regParam.append("&sql=" + URLEncoder.encode(monitor.getSql(), "UTF-8"));
				regParam.append("&unit=" + URLEncoder.encode(monitor.getUnit(), "UTF-8"));
				regParam.append("&delta=" + (monitor.isDelta() ? "1" : "0"));
				regParam.append("&systemaverage=" + (monitor.isAverage() ? "1" : "0"));
				regParam.append("&monitortype=" + "SQL");

				// type needs to indicate int/float/string etc. instead of LineChart/AreaChart
				regParam.append("&type=" + URLEncoder.encode(monitor.getChartType(), "UTF-8"));
				int interval = monitor.getInterval();
				regParam.append("&interval=" + String.valueOf(interval));

				success = api.post("monitorclass", regParam.toString());
			}

		} catch (Exception e) {
			e.printStackTrace();
			new ErrorDialog(e, "Error encoding API request");
		}

		WriteResponse writeResponse = APIrestful.getGson().fromJson(api.getResult(), WriteResponse.class);
		if (writeResponse != null && !writeResponse.getInsertKey().isEmpty()) {
			String monitorID = writeResponse.getInsertKey();
			monitor.setID(monitorID);
			currentList.put(monitorID, monitor);
			return monitorID;
		} else if (writeResponse != null && writeResponse.getUpdateCount() > 0) {
			return monitor.getID();
		} else {
			return null;
		}

	}

	public synchronized static boolean deleteMonitor(MonitorRecord monitor) {

		APIrestful api = new APIrestful();
		if (api.delete("monitorclass/" + monitor.getID())) {
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

class MonitorsDeserializer implements JsonDeserializer<Monitors> {
	public Monitors deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException, NullPointerException {
		Monitors monitors = new Monitors();

		JsonArray array = null;

		int length = 0;
		if (json.getAsJsonObject().has("monitorclasses")) {
			array = json.getAsJsonObject().get("monitorclasses").getAsJsonArray();
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

		length = array.size();
		for (int i = 0; i < length; i++) {
			JsonObject jsonObject = array.get(i).getAsJsonObject();
			JsonElement element;
			String systemType = (element = jsonObject.get("systemtype")).isJsonNull() ? null : element.getAsString();
			String id = (element = jsonObject.get("monitor")).isJsonNull() ? null : element.getAsString();
			String name = (element = jsonObject.get("name")).isJsonNull() ? null : element.getAsString();
			String description = (element = jsonObject.get("description")).isJsonNull() ? null : element.getAsString();
			String unit = (element = jsonObject.get("unit")).isJsonNull() ? null : element.getAsString();
			String type = (element = jsonObject.get("monitortype")).isJsonNull() ? null : element.getAsString();
			boolean delta = (element = jsonObject.get("delta")).isJsonNull() ? false : element.getAsBoolean();
			boolean average = (element = jsonObject.get("systemaverage")).isJsonNull() ? false : element.getAsBoolean();
			String chartType = (element = jsonObject.get("charttype")).isJsonNull() ? null : element.getAsString();
			String intervalString = (element = jsonObject.get("interval")).isJsonNull() ? null : element.getAsString();
			int interval = (intervalString != null && !intervalString.isEmpty()) ? Integer.valueOf(intervalString) : 0;
			String sql = (element = jsonObject.get("sql")).isJsonNull() ? null : element.getAsString();
			if (type.equals("SQL") && chartType != null) {
				// take only SQL monitors with chartType != null
				MonitorRecord monitorRecord = new MonitorRecord(systemType, id, name, description, unit, type, delta, average, chartType, interval, sql);
				monitorsMap.get(systemType).put(id, monitorRecord);
			}
		}

		return monitors;
	}
}
