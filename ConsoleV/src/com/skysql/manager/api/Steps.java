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
import com.skysql.manager.StepRecord;
import com.skysql.manager.ui.ErrorDialog;

public class Steps {

	private static Steps steps;
	private static LinkedHashMap<String, StepRecord> stepsList;

	public static LinkedHashMap<String, StepRecord> getStepsList() {
		GetSteps();
		return Steps.stepsList;
	}

	protected void setStepsList(LinkedHashMap<String, StepRecord> stepsList) {
		Steps.stepsList = stepsList;
	}

	public static boolean load() {
		GetSteps();
		return (steps != null);
	}

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

// {"command_steps":[{"state":"start","icon":"starting","description":"Start node up, start replication"},{"state":"stop","icon":"stopping","description":"Stop replication, shut node down"},{"state":"isolate","icon":"isolating","description":"Take node out of replication"},{"state":"recover","icon":"recovering","description":"Put node back into replication"},{"state":"promote","icon":"promoting","description":"Promote a slave to master"},{"state":"synchronize","icon":"synchronizing","description":"Synchronize a node"},{"state":"backup","icon":"backingup","description":"Backup a node"},{"state":"restore","icon":"restoring","description":"Restore a node"}],"warnings":["Caching directory \/usr\/local\/skysql\/cache\/api is not writeable, cannot write cache, please check existence, permissions, SELinux"]}

class StepsDeserializer implements JsonDeserializer<Steps> {
	public Steps deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException, NullPointerException {

		Steps steps = new Steps();

		JsonElement jsonElement = json.getAsJsonObject().get("command_steps");
		if (jsonElement.isJsonNull()) {
			steps.setStepsList(null);
		} else {
			JsonArray array = jsonElement.getAsJsonArray();
			int length = array.size();

			LinkedHashMap<String, StepRecord> stepsList = new LinkedHashMap<String, StepRecord>(length);
			for (int i = 0; i < length; i++) {
				JsonObject backupJson = array.get(i).getAsJsonObject();

				JsonElement element;
				String id = (element = backupJson.get("state")).isJsonNull() ? null : element.getAsString();
				String icon = (element = backupJson.get("icon")).isJsonNull() ? null : element.getAsString();
				String description = (element = backupJson.get("description")).isJsonNull() ? null : element.getAsString();
				StepRecord stepRecord = new StepRecord(icon, description);
				stepsList.put(id, stepRecord);
			}
			steps.setStepsList(stepsList);
		}
		return steps;

	}

}
