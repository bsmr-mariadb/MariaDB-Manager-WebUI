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
import com.skysql.manager.Commands;

// {"commands":[{"command":"backup","description":"Backup Online Slave Node","icon":"backup","steps":"isolate,backup,promote"},{"command":"backup","description":"Backup Offline Slave Node","icon":"backup","steps":"backup"},{"command":"start","description":"Start Stopped Node","icon":"start","steps":"start"},{"command":"stop","description":"Stop Master Node","icon":"stop","steps":"stop"},{"command":"stop","description":"Stop Slave Node","icon":"stop","steps":"stop"},{"command":"restore","description":"Restore Online Slave Node","icon":"stop","steps":"isolate,restore,synchronize"},{"command":"restart","description":"Restore Offline Slave Node","icon":"stop","steps":"restore"},{"command":"start","description":"Stop Node in Error","icon":"stop","steps":"stop"},{"command":"restart","description":"Restart Master Node","icon":"stop","steps":"stop,start"},{"command":"restart","description":"Restart Slave Node","icon":"restart","steps":"stop,start"},{"command":"restart","description":"Restart Node in Error","icon":"restart","steps":"restart"},{"command":"promote","description":"Promote Slave Node","icon":"promote","steps":"promote"}],"warnings":["Caching directory \/usr\/local\/skysql\/cache\/api is not writeable, cannot write cache, please check existence, permissions, SELinux"]}

public class CommandsDeserializer implements JsonDeserializer<Commands> {
	public Commands deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException, NullPointerException {
		Commands commands = new Commands();

		JsonElement jsonElement = json.getAsJsonObject().get("commands");
		if (jsonElement.isJsonNull()) {
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
			LinkedHashMap<String, String> steps = new LinkedHashMap<String, String>(length);
			for (int i = 0; i < length; i++) {
				JsonObject pair = array.get(i).getAsJsonObject();
				String command = pair.get("command").getAsString();
				icons.put(command, pair.get("icon").getAsString());
				descriptions.put(command, pair.get("description").getAsString());
				names.put(command, command);
				steps.put(command, pair.get("steps").getAsString());
			}
			commands.setIcons(icons);
			commands.setDescriptions(descriptions);
			commands.setNames(names);
			commands.setSteps(steps);
		}

		return commands;
	}

}