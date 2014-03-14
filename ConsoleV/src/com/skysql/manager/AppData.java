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

package com.skysql.manager;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Type;
import java.util.Hashtable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.skysql.manager.ui.ErrorDialog;

/**
 * The Class AppData. It loads the application initial configuration, consisting of the API uri and keys.
 */
public class AppData {

	public static String configPATH = "/usr/local/skysql/config/manager.json";
	private static AppData appData;

	/**
	 * New instance.
	 *
	 * @return the app data
	 */
	public static AppData newInstance() {
		appData = new AppData(null);
		if (appData.apiURI != null) {
			return appData;
		} else {
			return null;
		}
	}

	private String apiURI;
	private Hashtable<String, String> apiKeys;

	/**
	 * Gets the api uri.
	 *
	 * @return the api uri
	 */
	public String getApiURI() {
		return apiURI;
	}

	/**
	 * Sets the api uri.
	 *
	 * @param apiURI the new api uri
	 */
	protected void setApiURI(String apiURI) {
		this.apiURI = apiURI;
	}

	/**
	 * Gets the api keys.
	 *
	 * @return the api keys
	 */
	public Hashtable<String, String> getApiKeys() {
		return apiKeys;
	}

	/**
	 * Sets the api keys.
	 *
	 * @param apiKeys the api keys
	 */
	protected void setApiKeys(Hashtable<String, String> apiKeys) {
		this.apiKeys = apiKeys;
	}

	/**
	 * Instantiates a new app data.
	 */
	protected AppData() {
	}

	/**
	 * Instantiates a new app data.
	 *
	 * @param dummy a dummy parameter
	 */
	private AppData(String dummy) {
		File file = new File(configPATH);
		try {
			String configString = convertStreamToString(new FileInputStream(file));

			GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.registerTypeAdapter(AppData.class, new AppDataDeserializer());
			Gson gson = gsonBuilder.create();
			AppData config = gson.fromJson(configString, AppData.class);
			if (config != null) {
				this.apiURI = config.apiURI;
				this.apiKeys = config.apiKeys;
			}

		} catch (Exception e) {
			new ErrorDialog(e, "Error setting up application");
			throw new RuntimeException("Error setting up application");
		}

	}

	/**
	 * Convert stream to string.
	 *
	 * @param is the input stream
	 * @return the string
	 */
	public static String convertStreamToString(java.io.InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	/**
	 * The Class Debug is used to toggle extra debugging info.
	 */
	public final class Debug {
		//set to false to allow compiler to identify and eliminate unreachable code
		public static final boolean ON = false;
	}

}

/**
 * The Class AppDataDeserializer reads the JSON into the AppData class
 */
class AppDataDeserializer implements JsonDeserializer<AppData> {
	public AppData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

		AppData config = new AppData();

		if (!json.isJsonObject()) {
			return null;
		}

		JsonObject jsonObject = json.getAsJsonObject();
		JsonElement jsonElement;

		if (jsonObject.has("api")) {
			JsonObject apiObject = jsonObject.get("api").getAsJsonObject();

			config.setApiURI(((jsonElement = apiObject.get("uri")) == null || jsonElement.isJsonNull()) ? null : jsonElement.getAsString());

			if ((jsonElement = apiObject.get("keys")) != null && !jsonElement.isJsonNull()) {
				JsonArray array = jsonElement.getAsJsonArray();
				int length = array.size();
				Hashtable<String, String> apiKeys = new Hashtable<String, String>(length);
				for (int i = 0; i < length; i++) {
					JsonObject pair = array.get(i).getAsJsonObject();
					apiKeys.put(pair.get("id").getAsString(), pair.get("code").getAsString());
				}
				config.setApiKeys(apiKeys);
			}
		}

		return config;
	}
}
