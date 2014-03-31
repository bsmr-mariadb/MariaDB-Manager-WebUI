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

import java.lang.reflect.Type;
import java.util.LinkedHashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.skysql.manager.ui.ErrorDialog;

/**
 * The Class UserInfo.
 */
public class UserInfo {

	private static final String NOT_AVAILABLE = "n/a";

	private LinkedHashMap<String, UserObject> usersList;

	/**
	 * Gets the users list.
	 *
	 * @return the users list
	 */
	public LinkedHashMap<String, UserObject> getUsersList() {
		return usersList;
	}

	/**
	 * Instantiates a new user info.
	 */
	public UserInfo() {

	}

	/**
	 * Gets a user info from the API.
	 *
	 * @param dummy the dummy
	 */
	public UserInfo(String dummy) {

		APIrestful api = new APIrestful();
		if (api.get("user")) {
			try {
				UserInfo userInfo = APIrestful.getGson().fromJson(api.getResult(), UserInfo.class);
				this.usersList = userInfo.usersList;
			} catch (NullPointerException e) {
				new ErrorDialog(e, "API did not return expected result for:" + api.errorString());
				throw new RuntimeException("API response");
			} catch (JsonParseException e) {
				new ErrorDialog(e, "JSON parse error in API results for:" + api.errorString());
				throw new RuntimeException("API response");
			}
		}

	}

	/**
	 * Find record by id.
	 *
	 * @param id the id
	 * @return the user object
	 */
	public UserObject findRecordByID(String id) {
		return usersList.get(id);
	}

	/**
	 * Find name by id.
	 *
	 * @param id the id
	 * @return the string
	 */
	public String findNameByID(String id) {
		UserObject userObject = usersList.get(id);
		if (userObject == null) {
			// TODO: reload UserInfo from API, in case this user was created after the last load
			return NOT_AVAILABLE;
		}
		return userObject.getName();
	}

	/**
	 * Find any name by id.
	 *
	 * @param id the id
	 * @return the string
	 */
	public String findAnyNameByID(String id) {
		UserObject userObject = usersList.get(id);
		if (userObject == null) {
			// TODO: reload UserInfo from API, in case this user was created after the last load
			return NOT_AVAILABLE;
		}
		return userObject.getAnyName();
	}

	/**
	 * Provides user id + full name, by id.
	 *
	 * @param id the id
	 * @return the string
	 */
	public String completeNamesByID(String id) {
		UserObject userObject = usersList.get(id);
		if (userObject == null) {
			return null;
		}
		String name = userObject.getName();
		return id + ((name == null || name.isEmpty()) ? "" : " (" + name + ")");
	}

	/**
	 * Save user to API.
	 *
	 * @param userObject the user object
	 * @return true, if successful
	 */
	public boolean setUser(UserObject userObject) {

		if (userObject.set()) {
			userObject.setPassword(null);
			usersList.put(userObject.getUserID(), userObject);
			return true;
		}

		return false;
	}

	/**
	 * Delete user from API.
	 *
	 * @param userID the user id
	 * @return true, if successful
	 */
	public boolean deleteUser(String userID) {

		APIrestful api = new APIrestful();
		if (api.delete("user/" + userID)) {
			usersList.remove(userID);
			return true;
		}

		return false;
	}

	/**
	 * Sets the users list.
	 *
	 * @param usersList the users list
	 */
	protected void setUsersList(LinkedHashMap<String, UserObject> usersList) {
		this.usersList = usersList;
	}

}

/**
 * The Class UserInfoDeserializer.
 */
class UserInfoDeserializer implements JsonDeserializer<UserInfo> {
	public UserInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException, NullPointerException {
		UserInfo userInfo = new UserInfo();

		JsonElement jsonElement = json.getAsJsonObject().get("users");
		if (jsonElement.isJsonNull()) {
			userInfo.setUsersList(new LinkedHashMap<String, UserObject>());
		} else {
			JsonArray array = jsonElement.getAsJsonArray();
			int length = array.size();

			LinkedHashMap<String, UserObject> usersList = new LinkedHashMap<String, UserObject>(length);
			for (int i = 0; i < length; i++) {
				JsonObject backupJson = array.get(i).getAsJsonObject();
				JsonElement element;
				String username = (element = backupJson.get("username")).isJsonNull() ? null : element.getAsString();
				String name = (element = backupJson.get("name")).isJsonNull() ? null : element.getAsString();
				UserObject userObject = new UserObject(username, name);
				usersList.put(username, userObject);
			}
			userInfo.setUsersList(usersList);
		}

		return userInfo;
	}

}
