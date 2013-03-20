package com.skysql.consolev.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class SystemInfo {

	private String systemID;
	private String systemName;
	private String startDate;
	private String lastStop;
	private String lastAccess;
	private String[] nodes;
	private String lastCommand;
	private String lastBackup;

	public String getSystemID() {
		return systemID;
	}

	public void setSystemID(String systemID) {
		this.systemID = systemID;
	}

	public String getSystemName() {
		return systemName;
	}

	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getLastStop() {
		return lastStop;
	}

	public void setLastStop(String lastStop) {
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

	public void setNodes(String[] nodes) {
		this.nodes = nodes;
	}

	public String getLastBackup() {
		return lastBackup;
	}

	public void setLastBackup(String lastBackup) {
		this.lastBackup = lastBackup;
	}

	public SystemInfo() {

	}

	public SystemInfo(Gson gson) {
		String inputLine = null;
		try {
			URL systemInfo = new URL("http://localhost/consoleAPI/systeminfo.php");
			// URL systemInfo = new
			// URL("http://api.skysql.black-sheep-research.com/system");
			URLConnection sc = systemInfo.openConnection();
			sc.setRequestProperty("accept", "application/json");
			sc.setRequestProperty("X-skysql-apiversion", "1");
			BufferedReader in = new BufferedReader(new InputStreamReader(sc.getInputStream()));
			inputLine = in.readLine();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not get response from API");
		}

		SystemInfo sysInfo = gson.fromJson(inputLine, SystemInfo.class);
		this.systemID = sysInfo.systemID;
		this.systemName = sysInfo.systemName;
		this.startDate = sysInfo.startDate;
		this.lastStop = sysInfo.lastStop;
		this.lastAccess = sysInfo.lastAccess;
		this.nodes = sysInfo.nodes;
		this.lastCommand = sysInfo.lastCommand;
		this.lastBackup = sysInfo.lastBackup;
		sysInfo = null;
	}

	public String ToolTip() {

		return "<li><b>Nodes:</b> " + ((this.nodes == null) ? "n/a" : Arrays.toString(this.nodes)) + "</li>" + "<li><b>Start Date:</b> "
				+ ((this.startDate == null) ? "n/a" : this.startDate) + "</li>" + "<li><b>Last Stop:</b> " + ((this.lastStop == null) ? "n/a" : this.lastStop)
				+ "</li>" + "<li><b>Last Access:</b> " + ((this.lastAccess == null) ? "n/a" : this.lastAccess) + "</li>" + "<li><b>Last Backup:</b> "
				+ ((this.lastBackup == null) ? "n/a" : this.lastBackup) + "</li>" + "</ul>";
	}

}

class SystemInfoDeserializer implements JsonDeserializer<SystemInfo> {
	public SystemInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		SystemInfo sysInfo = new SystemInfo();

		JsonElement jsonElement = json.getAsJsonObject().get("systems");
		if (jsonElement == null || jsonElement.isJsonNull()) {
			return (sysInfo); // or return null?
		} else {
			JsonArray array = jsonElement.getAsJsonArray();

			// for now just get the first system, ignore others
			JsonObject systemJson = array.get(0).getAsJsonObject();

			JsonElement element;
			sysInfo.setSystemID(((element = systemJson.get("id")) == null || element.isJsonNull()) ? null : element.getAsString());
			sysInfo.setSystemName(((element = systemJson.get("name")) == null || element.isJsonNull()) ? null : element.getAsString());
			sysInfo.setStartDate(((element = systemJson.get("startDate")) == null || element.isJsonNull()) ? null : element.getAsString());
			sysInfo.setLastAccess(((element = systemJson.get("lastAccess")) == null || element.isJsonNull()) ? null : element.getAsString());
			sysInfo.setLastBackup(((element = systemJson.get("lastBackup")) == null || element.isJsonNull()) ? null : element.getAsString());

			element = systemJson.get("nodes");
			if (element == null || element.isJsonNull()) {
				sysInfo.setNodes(null);
			} else {
				JsonArray nodesJson = element.getAsJsonArray();
				String[] nodes = new String[nodesJson.size()];
				sysInfo.setNodes(nodes);
				for (int i = 0; i < nodesJson.size(); i++)
					nodes[i] = nodesJson.get(i).getAsString();
			}

		}
		return sysInfo;
	}

}
