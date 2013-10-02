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
 * Copyright 2012-2013 SkySQL Ab
 */

package com.skysql.manager.api;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.skysql.manager.ui.ErrorDialog;

public class UserObject {

	public static final String PROPERTY_CHART_MAPPINGS = "com.skysql.manager.chart.mappings";
	public static final String PROPERTY_CHART_SETTINGS = "com.skysql.manager.chart.settings";
	public static final String PROPERTY_TIME_ADJUST = "com.skysql.manager.time.adjust";
	public static final String PROPERTY_TIME_FORMAT = "com.skysql.manager.time.format";
	public static final String PROPERTY_COMMAND_EXECUTION = "com.skysql.manager.command.execution";

	private String userID;
	private String name;
	private String password;
	private LinkedHashMap<String, String> properties;

	public String getUserID() {
		return userID;
	}

	public String getName() {
		return (name != null && !name.isEmpty()) ? name : userID;
	}

	public String getPassword() {
		return password;
	}

	public String getProperty(String key) {
		if (properties != null)
			return properties.get(key);
		else
			return null;
	}

	public boolean set() {
		try {
			APIrestful api = new APIrestful();
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("name", name);
			jsonParam.put("password", password);
			if (api.put("user/" + userID, jsonParam.toString())) {
				WriteResponse writeResponse = APIrestful.getGson().fromJson(api.getResult(), WriteResponse.class);
				if (writeResponse != null && (!writeResponse.getInsertKey().isEmpty() || writeResponse.getUpdateCount() > 0)) {
					return true;
				}
			}
		} catch (JSONException e) {
			new ErrorDialog(e, "Error encoding API request");
			throw new RuntimeException("Error encoding API request");
		}

		return false;
	}

	public boolean setProperty(String key, String value) {
		if (properties == null) {
			properties = new LinkedHashMap<String, String>();
		}
		properties.put(key, value);

		try {
			APIrestful api = new APIrestful();
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("value", value);
			return api.put("user/" + userID + "/property/" + key, jsonParam.toString());
		} catch (JSONException e) {
			new ErrorDialog(e, "Error encoding API request");
			throw new RuntimeException("Error encoding API request");
		}

	}

	public boolean deleteProperty(String key) {
		if (properties != null) {
			APIrestful api = new APIrestful();
			if (api.delete("user/" + userID + "/property/" + key)) {
				properties.remove(key);
				return true;
			}
		}

		return false;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPassword(String password) {
		this.password = password;
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

		APIrestful api = new APIrestful();
		if (api.post("user/" + userID, "password=" + password)) {
			try {
				UserObject login = APIrestful.getGson().fromJson(api.getResult(), UserObject.class);
				if (login.getUserID() == null) {
					return false;
				}
				this.userID = userID;
				this.name = login.name;
				this.properties = login.properties;
				return true;

			} catch (NullPointerException e) {
				new ErrorDialog(e, "API did not return expected result for:" + api.errorString());
				throw new RuntimeException("API response");
			} catch (JsonParseException e) {
				new ErrorDialog(e, "JSON parse error in API results for:" + api.errorString());
				throw new RuntimeException("API response");
			}
		}
		return false;
	}

}

class UserObjectDeserializer implements JsonDeserializer<UserObject> {
	public UserObject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException, NullPointerException {
		UserObject userObject = new UserObject();

		JsonObject jsonObject = json.getAsJsonObject();
		JsonElement element;

		if (jsonObject.has("user")) {
			jsonObject = jsonObject.get("user").getAsJsonObject();
			userObject.setUserID(((element = jsonObject.get("username")) == null || element.isJsonNull()) ? null : element.getAsString());
			userObject.setName(((element = jsonObject.get("name")) == null || element.isJsonNull()) ? null : element.getAsString());

			JsonElement jsonElement = jsonObject.get("properties");
			if (jsonElement == null || jsonElement.isJsonNull()) {
				userObject.setProperties(null);
			} else {
				jsonObject = (JsonObject) jsonElement;
				LinkedHashMap<String, String> properties = new LinkedHashMap<String, String>();
				Set<Entry<String, JsonElement>> set = jsonObject.entrySet();
				Iterator<Entry<String, JsonElement>> iter = set.iterator();
				while (iter.hasNext()) {
					Entry<String, JsonElement> entry = iter.next();
					String property = entry.getKey();
					String value = entry.getValue().getAsString();
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
