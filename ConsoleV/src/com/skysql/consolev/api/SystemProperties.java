package com.skysql.consolev.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashMap;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class SystemProperties {

	private LinkedHashMap<String, String> properties;
	
	public LinkedHashMap<String, String> getProperties() {
		return properties;
	}
	protected void setProperties(LinkedHashMap<String, String> properties) {
		this.properties = properties;
	}

	public String setProperty(String systemID, String property, String value) {
		String inputLine = null;
		try {
			URL url = new URL("http://localhost/consoleAPI/systemproperties.php?system=" + systemID + "&property=" + property + "&value=" + value);
			URLConnection sc = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(sc.getInputStream()));
			inputLine = in.readLine();
			in.close();
		} catch (IOException e) {
        	e.printStackTrace();
        	throw new RuntimeException("Could not get response from API");
		}

		Gson gson = AppData.getGson();
		Response response = gson.fromJson(inputLine, Response.class);
		return response.getResponse();
	}

	public SystemProperties() {
	}

	public SystemProperties(String systemID) {
		String inputLine = null;
		try {
			URL url = new URL("http://localhost/consoleAPI/systemproperties.php?system=" + systemID);
			URLConnection sc = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(sc.getInputStream()));
			inputLine = in.readLine();
			in.close();
		} catch (IOException e) {
        	e.printStackTrace();
        	throw new RuntimeException("Could not get response from API");
		}

		Gson gson = AppData.getGson();
		SystemProperties sysProp = gson.fromJson(inputLine, SystemProperties.class);
		this.properties = sysProp.properties;
		sysProp = null;
	}
	
}

class SystemPropertiesDeserializer implements JsonDeserializer<SystemProperties> {
		  public SystemProperties deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
		      throws JsonParseException
		  {
			SystemProperties sysProps = new SystemProperties();
			
			JsonElement jsonElement = json.getAsJsonObject().get("properties");
			if (jsonElement == null || jsonElement.isJsonNull()) {
		    	sysProps.setProperties(null);
			} else {
		    	JsonArray array = jsonElement.getAsJsonArray();
		    	int length = array.size();
		    	
			    LinkedHashMap<String, String> properties = new LinkedHashMap<String, String>(length);
			    for (int i = 0; i < length; i++) {
			    	JsonObject pair = array.get(i).getAsJsonObject();
			    	properties.put(pair.get("property").getAsString(), pair.get("value").getAsString());
			    }
			    sysProps.setProperties(properties);
		    }
		    
		    return sysProps;
		  }

}
