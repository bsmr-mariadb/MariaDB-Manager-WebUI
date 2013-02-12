package com.skysql.consolev.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URI;
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
import com.skysql.consolev.UserRecord;

public class UserInfo {

	ArrayList<UserRecord> usersList;

	public ArrayList<UserRecord> getUsersList() {
		return usersList;
	}
	
	public void setUsersList(ArrayList<UserRecord> usersList) {
		this.usersList = usersList;
	}

	public UserInfo() {
		
	}
	
	public UserInfo(String dummy) {
		
    	String inputLine = null;
        try {
        	URL url = new URI("http", "localhost", "/consoleAPI/userinfo.php", null, null).toURL();
        	URLConnection sc = url.openConnection();
        	BufferedReader in = new BufferedReader(new InputStreamReader(sc.getInputStream()));
        	inputLine = in.readLine();
        	in.close();
        } catch (Exception e) {
        	e.printStackTrace();
        	throw new RuntimeException("Could not get response from API");
        }

		Gson gson = AppData.getGson();
		UserInfo userInfo = gson.fromJson(inputLine, UserInfo.class);
		this.usersList = userInfo.usersList;
		userInfo = null;
		
	}

	public UserRecord findRecordByID(String id){    
		for (UserRecord user : usersList) {
	        if (user.getID().equals(id)) {
	            return user;
	        }
	    }
	    return null; 
	}

	public String findNameByID(String id){    
	    for (UserRecord user : usersList) {
	        if (user.getID().equals(id)) {
	            return user.getName();
	        }
	    }
	    return null; 
	}

	public String findIDByName(String name){    
	    for (UserRecord user : usersList) {
	        if (user.getName().equals(name)) {
	            return user.getID();
	        }
	    }
	    return null; 
	}

}

class UserInfoDeserializer implements JsonDeserializer<UserInfo> {
		  public UserInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
		      throws JsonParseException
		  {
			UserInfo userInfo = new UserInfo();
			
			JsonElement jsonElement = json.getAsJsonObject().get("users");
			if (jsonElement == null || jsonElement.isJsonNull()) {
		    	userInfo.setUsersList(null);
			} else {
		    	JsonArray array = jsonElement.getAsJsonArray();
		    	int length = array.size();

		    	ArrayList<UserRecord> usersList = new ArrayList<UserRecord>(length);
			    for (int i = 0; i < length; i++) {
			    	JsonObject backupJson = array.get(i).getAsJsonObject();
			    	JsonElement element;
			    	String id = (element = backupJson.get("id")).isJsonNull() ? null : element.getAsString();
			    	String name = (element = backupJson.get("name")).isJsonNull() ? null : element.getAsString();
			    	UserRecord userRecord = new UserRecord(id, name);
			    	usersList.add(userRecord);
			    }
			    userInfo.setUsersList(usersList);
		    }

		    return userInfo;
		  }
		
}
