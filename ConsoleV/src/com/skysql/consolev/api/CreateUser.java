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

public class CreateUser {

	private String userID;
	
	public String getUserID() {
		return userID;
	}
	public void setID(String userID) {
		this.userID = userID;
	}

	public CreateUser() {
	}
	
	public CreateUser(String systemID, String name, String password) {

		String inputLine = null;
		try {
			URL url = new URI("http", "localhost", "/consoleAPI/createuser.php", "system=" + systemID + "&name=" + name + "&password=" + password, null).toURL();
			URLConnection sc = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(sc.getInputStream()));
			inputLine = in.readLine();
			in.close();
		} catch (Exception e) {
        	e.printStackTrace();
        	throw new RuntimeException("Could not get response from API");
		}

		Gson gson = AppData.getGson();
		CreateUser userLogin = gson.fromJson(inputLine, CreateUser.class);
		this.userID = userLogin.userID;
		userLogin = null;
				
	}
	
}

class CreateUserDeserializer implements JsonDeserializer<CreateUser> {
		  public CreateUser deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
		      throws JsonParseException
		  {
			CreateUser userLogin = new CreateUser();

		    JsonObject jsonObject = json.getAsJsonObject();
		    
		    JsonElement element;
		    userLogin.setID(((element = jsonObject.get("id")) == null || element.isJsonNull()) ? null : element.getAsString());
		    
		    return userLogin;
		  }

}
