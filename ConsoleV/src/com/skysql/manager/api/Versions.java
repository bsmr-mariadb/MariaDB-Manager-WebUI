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
 * The Class Versions.
 */
public class Versions {

	public static final String NOT_AVAILABLE = "n/a";

	private static LinkedHashMap<String, Versions> versionsList = null;
	private String name;
	private String version;
	private String release;
	private String date;

	/**
	 * Gets the versions list.
	 *
	 * @return the versionsList
	 */
	public static LinkedHashMap<String, Versions> getVersionsList() {
		return versionsList;
	}

	/**
	 * Sets the versions list.
	 *
	 * @param versionsList the new versions list
	 */
	public static void setVersionsList(LinkedHashMap<String, Versions> versionsList) {
		Versions.versionsList = versionsList;
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
	 * Gets the version.
	 *
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Gets the release.
	 *
	 * @return the release
	 */
	public String getRelease() {
		return release;
	}

	/**
	 * Gets the date.
	 *
	 * @return the date
	 */
	public String getDate() {
		return date;
	}

	/**
	 * Registers the GUI with the API and collects the whole list of Components.
	 *
	 * @param name the name
	 * @param version the version
	 * @param release the release
	 * @param date the date
	 */
	public Versions(String id, String name, String version, String release, String date) {

		APIrestful api = new APIrestful();

		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("value", name);
			api.put("system/0/node/0/component/" + id + "/property/name", jsonParam.toString());
			jsonParam.put("value", version);
			api.put("system/0/node/0/component/" + id + "/property/version", jsonParam.toString());
			jsonParam.put("value", release);
			api.put("system/0/node/0/component/" + id + "/property/release", jsonParam.toString());

			if (api.get("system/0/node/0/component")) {
				APIrestful.getGson().fromJson(api.getResult(), Versions.class);
			}

		} catch (JSONException e) {
			new ErrorDialog(e, "Error encoding API request");
			throw new RuntimeException("Error encoding API request");
		} catch (NullPointerException e) {
			new ErrorDialog(e, "API did not return expected result for:" + api.errorString());
			throw new RuntimeException("API response");
		} catch (JsonParseException e) {
			new ErrorDialog(e, "JSON parse error in API results for:" + api.errorString());
			throw new RuntimeException("API response");
		}

	}

	public Versions(String name, String version, String release, String date) {

		this.name = name;
		this.version = version;
		this.release = release;
		this.date = date;

	}

}

/**
 * The Class VersionsDeserializer.
 */
class VersionsDeserializer implements JsonDeserializer<Versions> {
	public Versions deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException, NullPointerException {

		JsonElement jsonElement = json.getAsJsonObject().get("components");
		if (jsonElement != null && !jsonElement.isJsonNull()) {

			LinkedHashMap<String, Versions> versionsList = new LinkedHashMap<String, Versions>();
			Set<Entry<String, JsonElement>> set = jsonElement.getAsJsonObject().entrySet();
			Iterator<Entry<String, JsonElement>> iter = set.iterator();
			while (iter.hasNext()) {
				Entry<String, JsonElement> entry = iter.next();
				String componentID = entry.getKey();
				JsonObject jsonObject = entry.getValue().getAsJsonObject();
				JsonElement element;
				String name = (element = jsonObject.get("name")) == null || element.isJsonNull() ? Versions.NOT_AVAILABLE : element.getAsString();
				String version = (element = jsonObject.get("version")) == null || element.isJsonNull() ? Versions.NOT_AVAILABLE : element.getAsString();
				String release = (element = jsonObject.get("release")) == null || element.isJsonNull() ? Versions.NOT_AVAILABLE : element.getAsString();
				String date = (element = jsonObject.get("date")) == null || element.isJsonNull() ? null : element.getAsString();
				versionsList.put(componentID, new Versions(name, version, release, date));
			}
			Versions.setVersionsList(versionsList);
		}

		return null;
	}
}
