package com.skysql.consolev.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URI;
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

public class UserLogin {

	private String userID;
	private LinkedHashMap<String, String> properties;
	
	public String getUserID() {
		return userID;
	}
	public void setID(String userID) {
		this.userID = userID;
	}
	public LinkedHashMap<String, String> getProperties() {
		return properties;
	}
	public void setProperties(LinkedHashMap<String, String> properties) {
		this.properties = properties;
	}

	public UserLogin() {
	}
	
	public UserLogin(String systemID, String name, String password) {

		String inputLine = null;
		try {
			URL url = new URI("http", "localhost", "/consoleAPI/login.php", "system=" + systemID + "&name=" + name + "&password=" + password, null).toURL();
			URLConnection sc = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(sc.getInputStream()));
			inputLine = in.readLine();
			in.close();
		} catch (Exception e) {
        	e.printStackTrace();
        	throw new RuntimeException("Could not get response from API");
		}

		Gson gson = AppData.getGson();
		UserLogin userLogin = gson.fromJson(inputLine, UserLogin.class);
		this.userID = userLogin.userID;
		userLogin = null;
				
	}
	
}

class UserLoginDeserializer implements JsonDeserializer<UserLogin> {
		  public UserLogin deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
		      throws JsonParseException
		  {
			UserLogin userLogin = new UserLogin();

		    JsonObject jsonObject = json.getAsJsonObject();
		    
		    JsonElement element;
		    userLogin.setID(((element = jsonObject.get("id")) == null || element.isJsonNull()) ? null : element.getAsString());

		    element = jsonObject.get("properties");
			if (element == null || element.isJsonNull()) {
				userLogin.setProperties(null);
			} else {
		    	JsonArray array = element.getAsJsonArray();
		    	int length = array.size();

		   		LinkedHashMap<String, String> properties = new LinkedHashMap<String, String>(length);
		   		for (int i = 0; i < length; i++) {
		   			JsonObject pair = array.get(i).getAsJsonObject();
		   			String property = (element = pair.get("property")).isJsonNull() ? null : element.getAsString();
			    	String value = (element = pair.get("value")).isJsonNull() ? null : element.getAsString();
		   		 	properties.put(property, value);
		   		}
		   		userLogin.setProperties(properties);
		    }
		    
		    return userLogin;
		  }

}
