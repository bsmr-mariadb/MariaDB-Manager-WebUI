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
 * Copyright SkySQL Ab
 */

package com.skysql.consolev.api;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.LinkedHashMap;

import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class UserObject {

	public static final String PROPERTY_CHARTS = "CHARTS";

	private String userID;
	private String name;
	private LinkedHashMap<String, String> properties;

	public String getUserID() {
		return userID;
	}

	public String getName() {
		return name;
	}

	public String getProperty(String key) {
		if (properties != null)
			return properties.get(key);
		else
			return null;
	}

	public boolean setProperty(String key, String value) {
		if (properties == null) {
			properties = new LinkedHashMap<String, String>();
		}
		properties.put(key, value);

		String inputLine = null;
		try {
			APIrestful api = new APIrestful();
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("value", URLEncoder.encode(value, "UTF8"));
			inputLine = api.put("user/" + userID + "/property/" + key, jsonParam.toString());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Could not get response from API");
		}

		return (inputLine == null) ? false : true;

	}

	protected void setUserID(String userID) {
		this.userID = userID;
	}

	protected void setName(String name) {
		this.name = name;
	}

	protected void setProperties(LinkedHashMap<String, String> properties) {
		this.properties = properties;
	}

	public UserObject() {

	}

	public UserObject(String userID, String name) {
		this.userID = userID;
		this.name = name;
	}

	public boolean login(String userID, String password) {

		String inputLine = null;
		try {
			APIrestful api = new APIrestful();
			inputLine = api.post("user/" + userID, "password=" + password);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Could not get response from API");
		}

		Gson gson = AppData.getGson();
		UserObject login = gson.fromJson(inputLine, UserObject.class);
		if (login.getUserID() == null) {
			return false;
		}
		this.userID = userID;
		this.name = login.name;

		try {
			APIrestful api = new APIrestful();
			inputLine = api.get("user/" + userID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Could not get response from API");
		}

		UserObject user = gson.fromJson(inputLine, UserObject.class);
		this.properties = user.properties;

		return true;
	}

}

class UserObjectDeserializer implements JsonDeserializer<UserObject> {
	public UserObject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		UserObject userObject = new UserObject();

		JsonObject jsonObject = json.getAsJsonObject();
		JsonElement element;

		if (jsonObject.has("username")) {
			userObject.setUserID(((element = jsonObject.get("username")) == null || element.isJsonNull()) ? null : element.getAsString());
			userObject.setName(((element = jsonObject.get("name")) == null || element.isJsonNull()) ? null : element.getAsString());
			element = jsonObject.get("properties");
			if (element == null || element.isJsonNull()) {
				userObject.setProperties(null);
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
				userObject.setProperties(properties);
			}

		} else if (jsonObject.has("errors")) {

		} else {

		}

		return userObject;
	}

}
