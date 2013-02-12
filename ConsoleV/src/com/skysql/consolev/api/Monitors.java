package com.skysql.consolev.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.skysql.consolev.MonitorRecord;

public class Monitors {

	private static ArrayList<MonitorRecord> monitorsList;
	
	public static ArrayList<MonitorRecord> getMonitorsList() {
		GetMonitors();
		return monitorsList;
	}
	

	private static void GetMonitors() {
		if (monitorsList == null) {
			String inputLine = null;
			try {
				URL url = new URL("http://localhost/consoleAPI/monitors.php");
				URLConnection sc = url.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(sc.getInputStream()));
				inputLine = in.readLine();
				in.close();
			} catch (IOException e) {
	        	e.printStackTrace();
	        	throw new RuntimeException("Could not get response from API");
			}

			Gson gson = AppData.getGson();
			Monitors monitors = gson.fromJson(inputLine, Monitors.class);
			Monitors.monitorsList = monitors.monitorsList;
			monitors = null;
		}
	}
	
	protected void setMonitorsList(ArrayList<MonitorRecord> monitorsList) {
		this.monitorsList = monitorsList;
	}

}

class MonitorsDeserializer implements JsonDeserializer<Monitors> {
		  public Monitors deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
		      throws JsonParseException
		  {
			Monitors monitors = new Monitors();
			
			JsonElement jsonElement = json.getAsJsonObject().get("monitors");
			if (jsonElement == null || jsonElement.isJsonNull()) {
			    monitors.setMonitorsList(null);
			} else {
		    	JsonArray array = jsonElement.getAsJsonArray();
		    	int length = array.size();

			    ArrayList<MonitorRecord> monitorsList = new ArrayList<MonitorRecord>(length);
			    for (int i = 0; i < length; i++) {
			    	JsonObject jsonObject = array.get(i).getAsJsonObject();
			    	JsonElement element;
			    	String id = (element = jsonObject.get("id")).isJsonNull() ? null : element.getAsString();
			    	String name = (element = jsonObject.get("name")).isJsonNull() ? null : element.getAsString();
			    	String description = (element = jsonObject.get("description")).isJsonNull() ? null : element.getAsString();
			    	String icon = (element = jsonObject.get("icon")).isJsonNull() ? null : element.getAsString();
			    	String type = (element = jsonObject.get("type")).isJsonNull() ? null : element.getAsString();
			    	MonitorRecord monitorRecord = new MonitorRecord(id, name, description, icon, type);
			    	monitorsList.add(monitorRecord);
			    }
			    monitors.setMonitorsList(monitorsList);

		    }
		    
		    return monitors;
		  }

}
