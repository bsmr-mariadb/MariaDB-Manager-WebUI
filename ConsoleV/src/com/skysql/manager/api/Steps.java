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
import com.skysql.manager.ManagerUI;
import com.skysql.manager.ui.ErrorDialog;

/**
 * The Class Steps.
 */
public class Steps {

	//backup, configure, install-agent, install-packages, isolate, probe, promote, recover, register, restart, restore, setup-ssh, start, stop, synchronize;

	private static Steps steps;
	private static LinkedHashMap<String, String> stepsList;

	/**
	 * Gets the description.
	 *
	 * @param step the step
	 * @return the description
	 */
	public static String getDescription(String step) {
		GetSteps();
		String description = stepsList.get(step);
		if (description == null) {
			ManagerUI.error("Unknown step found in API response: " + step);
			description = "Unknown step: " + step;
		}

		return description;
	}

	/**
	 * Sets the steps list.
	 *
	 * @param stepsList the steps list
	 */
	protected void setStepsList(LinkedHashMap<String, String> stepsList) {
		Steps.stepsList = stepsList;
	}

	/**
	 * Attempts top load the list of steps and returns true if successful.
	 *
	 * @return true, if successful
	 */
	public static boolean load() {
		GetSteps();
		return (steps != null);
	}

	/**
	 * Gets the steps from the API.
	 */
	private static void GetSteps() {
		if (steps == null) {
			APIrestful api = new APIrestful();
			if (api.get("command/step")) {
				try {
					steps = APIrestful.getGson().fromJson(api.getResult(), Steps.class);
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

// {"command_steps":[{"step":"start","description":"Start node up, start replication"},{"step":"stop","description":"Stop replication, shut node down"},{"step":"isolate","description":"Take node out of replication"},{"step":"recover","description":"Put node back into replication"},{"step":"promote","description":"Promote a slave to master"},{"step":"synchronize","description":"Synchronize a node"},{"step":"backup","description":"Backup a node"},{"step":"restore","description":"Restore a node"},{"step":"restart","description":"Restart a node from error state"}]}

/**
 * The Class StepsDeserializer.
 */
class StepsDeserializer implements JsonDeserializer<Steps> {
	public Steps deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException, NullPointerException {

		Steps steps = new Steps();

		JsonElement jsonElement = json.getAsJsonObject().get("command_steps");
		if (jsonElement.isJsonNull()) {
			steps.setStepsList(null);
		} else {
			JsonArray array = jsonElement.getAsJsonArray();
			int length = array.size();

			LinkedHashMap<String, String> stepsList = new LinkedHashMap<String, String>(length);
			for (int i = 0; i < length; i++) {
				JsonObject backupJson = array.get(i).getAsJsonObject();

				JsonElement element;
				String id = (element = backupJson.get("step")).isJsonNull() ? null : element.getAsString();
				String description = (element = backupJson.get("description")).isJsonNull() ? null : element.getAsString();
				stepsList.put(id, description);
			}
			steps.setStepsList(stepsList);
		}
		return steps;

	}

}
