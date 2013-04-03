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
	public static final String PROPERTY_SKIPLOGIN = "SKIP_LOGIN";

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

		//		Gson gson = AppData.getGson();
		//		RestfulResponse response = gson.fromJson(inputLine, RestfulResponse.class);
		//		if (!response.isSuccess()) {
		//			Notification.show(response.getErrors());
		//		}

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

		try {
			APIrestful api = new APIrestful();
			inputLine = api.get("system/" + ID);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not get response from API");
		}

		gson = AppData.getGson();
		systemInfo = gson.fromJson(inputLine, SystemInfo.class);
		this.name = systemInfo.name;
		this.startDate = systemInfo.startDate;
		this.lastStop = systemInfo.lastStop;
		this.lastAccess = systemInfo.lastAccess;
		this.nodes = systemInfo.nodes;
		this.lastCommand = systemInfo.lastCommand;
		this.lastBackup = systemInfo.lastBackup;
		this.properties = systemInfo.properties;

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
		if (jsonObject.has("systems")) {
			JsonArray array = jsonObject.get("systems").getAsJsonArray();
			// for now just get the first system, ignore others
			jsonObject = array.get(0).getAsJsonObject();
			systemInfo.setID(((element = jsonObject.get("id")) == null || element.isJsonNull()) ? null : element.getAsString());
		} else if (jsonObject.has("system")) {
			jsonObject = jsonObject.get("system").getAsJsonObject();

			systemInfo.setName(((element = jsonObject.get("name")) == null || element.isJsonNull()) ? null : element.getAsString());
			systemInfo.setStartDate(((element = jsonObject.get("startDate")) == null || element.isJsonNull()) ? null : element.getAsString());
			systemInfo.setLastAccess(((element = jsonObject.get("lastAccess")) == null || element.isJsonNull()) ? null : element.getAsString());
			systemInfo.setLastBackup(((element = jsonObject.get("lastBackup")) == null || element.isJsonNull()) ? null : element.getAsString());

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

				value = (((element = propertiesJson.get(SystemInfo.PROPERTY_EIP)) == null || element.isJsonNull()) ? null : element.getAsString());
				properties.put(SystemInfo.PROPERTY_EIP, value);

				value = (((element = propertiesJson.get(SystemInfo.PROPERTY_MONYOG)) == null || element.isJsonNull()) ? null : element.getAsString());
				properties.put(SystemInfo.PROPERTY_MONYOG, value);

				value = (((element = propertiesJson.get(SystemInfo.PROPERTY_PHPMYADMIN)) == null || element.isJsonNull()) ? null : element.getAsString());
				properties.put(SystemInfo.PROPERTY_PHPMYADMIN, value);

				value = (((element = propertiesJson.get(SystemInfo.PROPERTY_DEFAULTMONITORINTERVAL)) == null || element.isJsonNull()) ? null : element
						.getAsString());
				properties.put(SystemInfo.PROPERTY_DEFAULTMONITORINTERVAL, value);

				value = (((element = propertiesJson.get(SystemInfo.PROPERTY_DEFAULTMAXBACKUPCOUNT)) == null || element.isJsonNull()) ? null : element
						.getAsString());
				properties.put(SystemInfo.PROPERTY_DEFAULTMAXBACKUPCOUNT, value);

				value = (((element = propertiesJson.get(SystemInfo.PROPERTY_DEFAULTMAXBACKUPSIZE)) == null || element.isJsonNull()) ? null : element
						.getAsString());
				properties.put(SystemInfo.PROPERTY_DEFAULTMAXBACKUPSIZE, value);

				value = (((element = propertiesJson.get(SystemInfo.PROPERTY_VERSION)) == null || element.isJsonNull()) ? null : element.getAsString());
				properties.put(SystemInfo.PROPERTY_VERSION, value);

				value = (((element = propertiesJson.get(SystemInfo.PROPERTY_SKIPLOGIN)) == null || element.isJsonNull()) ? null : element.getAsString());
				properties.put(SystemInfo.PROPERTY_SKIPLOGIN, value);

				systemInfo.setProperties(properties);
			}

		}
		return systemInfo;
	}
}
