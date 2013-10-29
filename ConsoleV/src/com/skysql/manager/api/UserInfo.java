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
import java.util.LinkedHashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.skysql.manager.ui.ErrorDialog;

public class UserInfo {

	private static final String NOT_AVAILABLE = "n/a";

	LinkedHashMap<String, UserObject> usersList;

	public LinkedHashMap<String, UserObject> getUsersList() {
		return usersList;
	}

	public UserInfo() {

	}

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

	public UserObject findRecordByID(String id) {
		return usersList.get(id);
	}

	public String findNameByID(String id) {
		UserObject userObject = usersList.get(id);
		if (userObject == null) {
			// TODO: reload UserInfo from API, in case this user was created after the last load
			return NOT_AVAILABLE;
		}
		return userObject.getName();
	}

	public String findAnyNameByID(String id) {
		UserObject userObject = usersList.get(id);
		if (userObject == null) {
			// TODO: reload UserInfo from API, in case this user was created after the last load
			return NOT_AVAILABLE;
		}
		return userObject.getAnyName();
	}

	public String completeNamesByID(String id) {
		UserObject userObject = usersList.get(id);
		if (userObject == null) {
			return null;
		}
		String name = userObject.getName();
		return id + ((name == null || name.isEmpty()) ? "" : " (" + name + ")");
	}

	public boolean setUser(UserObject userObject) {

		if (userObject.set()) {
			userObject.setPassword(null);
			usersList.put(userObject.getUserID(), userObject);
			return true;
		}

		return false;
	}

	public boolean deleteUser(String userID) {

		APIrestful api = new APIrestful();
		if (api.delete("user/" + userID)) {
			usersList.remove(userID);
			return true;
		}

		return false;
	}

	protected void setUsersList(LinkedHashMap<String, UserObject> usersList) {
		this.usersList = usersList;
	}

}

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
