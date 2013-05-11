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

package com.skysql.consolev.api;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class Commands {

	private static Commands commands;
	private static LinkedHashMap<String, String> icons;
	private static LinkedHashMap<String, String> descriptions;
	private static LinkedHashMap<String, String> names;
	private static LinkedHashMap<String, String[]> steps;

	public static LinkedHashMap<String, String> getIcons() {
		GetCommands();
		return Commands.icons;
	}

	public static LinkedHashMap<String, String> getDescriptions() {
		GetCommands();
		return Commands.descriptions;
	}

	public static LinkedHashMap<String, String> getNames() {
		GetCommands();
		return Commands.names;
	}

	public static String[] getSteps(String command) {
		GetCommands();
		return Commands.steps.get(command);
	}

	private static void GetCommands() {
		if (commands == null) {
			APIrestful api = new APIrestful();
			if (api.get("command")) {
				commands = AppData.getGson().fromJson(api.getResult(), Commands.class);
			}
		}
	}

	protected void setIcons(LinkedHashMap<String, String> pairs) {
		Commands.icons = pairs;
	}

	protected void setDescriptions(LinkedHashMap<String, String> pairs) {
		Commands.descriptions = pairs;
	}

	protected void setNames(LinkedHashMap<String, String> pairs) {
		Commands.names = pairs;
	}

	protected void setSteps(LinkedHashMap<String, String[]> pairs) {
		Commands.steps = pairs;
	}

}

class CommandsDeserializer implements JsonDeserializer<Commands> {
	public Commands deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		Commands commands = new Commands();

		JsonElement jsonElement = json.getAsJsonObject().get("commands");
		if (jsonElement == null || jsonElement.isJsonNull()) {
			commands.setIcons(null);
			commands.setDescriptions(null);
			commands.setNames(null);
			commands.setSteps(null);
		} else {
			JsonArray array = jsonElement.getAsJsonArray();
			int length = array.size();

			LinkedHashMap<String, String> icons = new LinkedHashMap<String, String>(length);
			LinkedHashMap<String, String> descriptions = new LinkedHashMap<String, String>(length);
			LinkedHashMap<String, String> names = new LinkedHashMap<String, String>(length);
			LinkedHashMap<String, String[]> steps = new LinkedHashMap<String, String[]>(length);
			for (int i = 0; i < length; i++) {
				JsonObject pair = array.get(i).getAsJsonObject();
				icons.put(pair.get("id").getAsString(), pair.get("icon").getAsString());
				descriptions.put(pair.get("id").getAsString(), pair.get("description").getAsString());
				names.put(pair.get("id").getAsString(), pair.get("name").getAsString());
				steps.put(pair.get("id").getAsString(), pair.get("steps").getAsString().split(","));
			}
			commands.setIcons(icons);
			commands.setDescriptions(descriptions);
			commands.setNames(names);
			commands.setSteps(steps);
		}

		return commands;
	}

}
