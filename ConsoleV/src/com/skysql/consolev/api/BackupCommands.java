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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashMap;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class BackupCommands {

	private static BackupCommands backupCommands;
	private static LinkedHashMap<String, String> icons;
	private static LinkedHashMap<String, String> descriptions;
	private static LinkedHashMap<String, String> names;

	public static LinkedHashMap<String, String> getIcons() {
		GetCommands();
		return BackupCommands.icons;
	}

	public static LinkedHashMap<String, String> getDescriptions() {
		GetCommands();
		return BackupCommands.descriptions;
	}

	public static LinkedHashMap<String, String> getNames() {
		GetCommands();
		return BackupCommands.names;
	}

	private static void GetCommands() {
		if (backupCommands == null) {
			String inputLine = null;
			try {
				URL url = new URL("http://" + AppData.oldAPIurl + "/consoleAPI/commands.php?group=backup");
				URLConnection sc = url.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(sc.getInputStream()));
				inputLine = in.readLine();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("Could not get response from API");
			}

			Gson gson = AppData.getGson();
			backupCommands = gson.fromJson(inputLine, BackupCommands.class);
		}
	}

	protected void setIcons(LinkedHashMap<String, String> pairs) {
		BackupCommands.icons = pairs;
	}

	protected void setDescriptions(LinkedHashMap<String, String> pairs) {
		BackupCommands.descriptions = pairs;
	}

	protected void setNames(LinkedHashMap<String, String> pairs) {
		BackupCommands.names = pairs;
	}
}

class BackupCommandsDeserializer implements JsonDeserializer<BackupCommands> {
	public BackupCommands deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		BackupCommands commands = new BackupCommands();

		JsonElement jsonElement = json.getAsJsonObject().get("commands");
		if (jsonElement == null || jsonElement.isJsonNull()) {
			commands.setIcons(null);
			commands.setDescriptions(null);
			commands.setNames(null);
		} else {
			JsonArray array = jsonElement.getAsJsonArray();
			int length = array.size();

			LinkedHashMap<String, String> icons = new LinkedHashMap<String, String>(length);
			LinkedHashMap<String, String> descriptions = new LinkedHashMap<String, String>(length);
			LinkedHashMap<String, String> names = new LinkedHashMap<String, String>(length);
			for (int i = 0; i < length; i++) {
				JsonObject pair = array.get(i).getAsJsonObject();
				icons.put(pair.get("id").getAsString(), pair.get("icon").getAsString());
				descriptions.put(pair.get("id").getAsString(), pair.get("description").getAsString());
				names.put(pair.get("id").getAsString(), pair.get("name").getAsString());
			}
			commands.setIcons(icons);
			commands.setDescriptions(descriptions);
			commands.setNames(names);
		}

		return commands;
	}

}
