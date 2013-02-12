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

public class SettingsValues {

	private ArrayList<String> values;

	public ArrayList<String> getValues() {
		return values;
	}
	protected void setValues(ArrayList<String> values) {
		this.values = values;
	}

	public SettingsValues() {
	}

	public SettingsValues(String property) {
		String inputLine = null;
		try {
			URL url = new URL("http://localhost/consoleAPI/settingsvalues.php?property=" + property);
			URLConnection sc = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(sc.getInputStream()));
			inputLine = in.readLine();
			in.close();
		} catch (IOException e) {
        	e.printStackTrace();
        	throw new RuntimeException("Could not get response from API");
		}

		Gson gson = AppData.getGson();
		SettingsValues settingsValues = gson.fromJson(inputLine, SettingsValues.class);
		this.values = settingsValues.values;
		settingsValues = null;
	}
	
}

class SettingsValuesDeserializer implements JsonDeserializer<SettingsValues> {
		  public SettingsValues deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
		      throws JsonParseException
		  {
			SettingsValues settingsValues = new SettingsValues();
			
			JsonElement jsonElement = json.getAsJsonObject().get("settingsValues");
			if (jsonElement == null || jsonElement.isJsonNull()) {
				settingsValues.setValues(null);
			} else {
		    	JsonArray array = jsonElement.getAsJsonArray();
		    	int length = array.size();
		    	
		    	ArrayList<String> values = new ArrayList<String>(length);
			    for (int i = 0; i < length; i++) {
			    	JsonObject valueJson = array.get(i).getAsJsonObject();
			    	JsonElement element;
			    	String value = (element = valueJson.get("value")).isJsonNull() ? null : element.getAsString();
			    	values.add(value);
			    }
			    settingsValues.setValues(values);
		    }
		    
		    return settingsValues;
		  }

}
