/*
 * This file is distributed as part of the MariaDB Manager.  It is free
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
 * Copyright 2012-2014 SkySQL Corporation Ab
 */

package com.skysql.manager.api;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
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

/**
 * The Class UserObject.
 */
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

	/**
	 * Gets the user id.
	 *
	 * @return the user id
	 */
	public String getUserID() {
		return userID;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the password.
	 *
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Gets the user name if available or the id.
	 *
	 * @return the any name
	 */
	public String getAnyName() {
		return (name != null && !name.isEmpty()) ? name : userID;
	}

	/**
	 * Gets the property.
	 *
	 * @param key the key
	 * @return the property
	 */
	public String getProperty(String key) {
		if (properties != null)
			return properties.get(key);
		else
			return null;
	}

	/**
	 * Saves the user to the API.
	 *
	 * @return true, if successful
	 */
	public boolean set() {
		try {
			APIrestful api = new APIrestful();
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("name", name);
			if (password != null && !password.isEmpty()) {
				jsonParam.put("password", password);
			}
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

	/**
	 * Sets the property.
	 *
	 * @param key the key
	 * @param value the value
	 * @return true, if successful
	 */
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

	/**
	 * Delete property.
	 *
	 * @param key the key
	 * @return true, if successful
	 */
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

	/**
	 * Sets the user id.
	 *
	 * @param userID the new user id
	 */
	public void setUserID(String userID) {
		this.userID = userID;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the password.
	 *
	 * @param password the new password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Sets the properties.
	 *
	 * @param properties the properties
	 */
	protected void setProperties(LinkedHashMap<String, String> properties) {
		this.properties = properties;
	}

	/**
	 * Instantiates a new user object.
	 */
	public UserObject() {

	}

	/**
	 * Instantiates a new user object.
	 *
	 * @param userID the user id
	 * @param name the name
	 */
	public UserObject(String userID, String name) {
		this.userID = userID;
		this.name = name;
	}

	/**
	 * Logs user with the API.
	 *
	 * @param userID the user id
	 * @param password the password
	 * @return true, if successful
	 */
	public boolean login(String userID, String password) {

		APIrestful api = new APIrestful();
		try {
			StringBuffer regParam = new StringBuffer();
			regParam.append("password=" + URLEncoder.encode(password, "UTF-8"));
			if (api.post("user/" + userID, regParam.toString())) {
				UserObject login = APIrestful.getGson().fromJson(api.getResult(), UserObject.class);
				if (login.getUserID() == null) {
					return false;
				}
				this.userID = userID;
				this.name = login.name;
				this.properties = login.properties;
				return true;
			}
		} catch (UnsupportedEncodingException e) {
			new ErrorDialog(e, "Error encoding API request");
			throw new RuntimeException("Error encoding API request");
		} catch (NullPointerException e) {
			new ErrorDialog(e, "API did not return expected result for:" + api.errorString());
			throw new RuntimeException("API response");
		} catch (JsonParseException e) {
			new ErrorDialog(e, "JSON parse error in API results for:" + api.errorString());
			throw new RuntimeException("API response");
		}

		return false;
	}
}

/**
 * The Class UserObjectDeserializer.
 */
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
