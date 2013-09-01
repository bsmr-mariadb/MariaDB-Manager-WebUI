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

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.skysql.manager.ui.ErrorDialog;

public class SystemTypes {

	private static SystemTypes systemTypes;
	private static LinkedHashMap<String, String> typesList;

	public static LinkedHashMap<String, String> getList() {
		GetSystemTypes();
		return SystemTypes.typesList;
	}

	protected void setList(LinkedHashMap<String, String> typesList) {
		SystemTypes.typesList = typesList;
	}

	public static boolean load() {
		GetSystemTypes();
		return (systemTypes != null);
	}

	private static void GetSystemTypes() {
		if (systemTypes == null) {
			APIrestful api = new APIrestful();
			if (api.get("systemtype")) {
				try {
					systemTypes = APIrestful.getGson().fromJson(api.getResult(), SystemTypes.class);
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

}

// {"systemtypes":{"aws":"Amazon AWS based System","galera":"Galera multi-master System"},"warnings":["Caching directory \/usr\/local\/skysql\/cache\/api is not writeable, cannot write cache, please check existence, permissions, SELinux"]}

class SystemTypesDeserializer implements JsonDeserializer<SystemTypes> {
	public SystemTypes deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException, NullPointerException {

		SystemTypes systemTypes = new SystemTypes();

		JsonElement jsonElement = json.getAsJsonObject().get("systemtypes");
		if (jsonElement == null || jsonElement.isJsonNull()) {
			systemTypes.setList(null);
		} else {
			LinkedHashMap<String, String> types = new LinkedHashMap<String, String>();
			Set<Entry<String, JsonElement>> set = jsonElement.getAsJsonObject().entrySet();
			Iterator<Entry<String, JsonElement>> iter = set.iterator();
			while (iter.hasNext()) {
				Entry<String, JsonElement> entry = iter.next();
				String type = entry.getKey();
				String description = entry.getValue().getAsString();
				types.put(type, description);
			}
			systemTypes.setList(types);
		}
		return systemTypes;

	}

}
