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
 * Copyright 2012-2014 SkySQL Ab
 */

package com.skysql.manager.api;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.skysql.manager.ui.ErrorDialog;

public class Versions {

	private String name;
	private String version;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Versions() {

	}

	public Versions(String component) {

		APIrestful api = new APIrestful();
		if (api.get("system/0/node/0/component/" + component)) {
			try {
				Versions versions = APIrestful.getGson().fromJson(api.getResult(), Versions.class);
				this.name = versions.name;
				this.version = versions.version;
			} catch (NullPointerException e) {
				new ErrorDialog(e, "API did not return expected result for:" + api.errorString());
				throw new RuntimeException("API response");
			} catch (JsonParseException e) {
				new ErrorDialog(e, "JSON parse error in API results for:" + api.errorString());
				throw new RuntimeException("API response");
			}
		}
	}

}

class VersionsDeserializer implements JsonDeserializer<Versions> {
	public Versions deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException, NullPointerException {

		Versions versions = new Versions();

		JsonElement jsonElement = json.getAsJsonObject().get("monitorproperties");
		if (!jsonElement.isJsonNull()) {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			JsonElement element;
			versions.setName((element = jsonObject.get("name")) == null || element.isJsonNull() ? null : element.getAsString());
			versions.setVersion((element = jsonObject.get("version")) == null || element.isJsonNull() ? null : element.getAsString());
		}
		return versions;

	}

}
