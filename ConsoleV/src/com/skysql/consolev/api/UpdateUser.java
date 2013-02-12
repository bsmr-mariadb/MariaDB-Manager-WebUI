package com.skysql.consolev.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class UpdateUser {

	private String userID;
	
	public String getUserID() {
		return userID;
	}
	public void setID(String userID) {
		this.userID = userID;
	}

	public UpdateUser() {
	}
	
	public UpdateUser(String systemID, String userID, String name, String password) {

		String inputLine = null;
		try {
			URL url = new URI("http", "localhost", "/consoleAPI/updateuser.php", "system=" + systemID + "&id=" + userID + ((name != null) ? "&name=" + name : "") + ((password != null) ? "&password=" + password : ""), null).toURL();
			URLConnection sc = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(sc.getInputStream()));
			inputLine = in.readLine();
			in.close();
		} catch (Exception e) {
        	e.printStackTrace();
        	throw new RuntimeException("Could not get response from API");
		}

		Gson gson = AppData.getGson();
		UpdateUser userLogin = gson.fromJson(inputLine, UpdateUser.class);
		this.userID = userLogin.userID;
		userLogin = null;
				
	}
	
}

class UpdateUserDeserializer implements JsonDeserializer<UpdateUser> {
		  public UpdateUser deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
		      throws JsonParseException
		  {
			UpdateUser userLogin = new UpdateUser();

		    JsonObject jsonObject = json.getAsJsonObject();
		    
		    JsonElement element;
		    userLogin.setID(((element = jsonObject.get("id")) == null || element.isJsonNull()) ? null : element.getAsString());
		    
		    return userLogin;
		  }

}
