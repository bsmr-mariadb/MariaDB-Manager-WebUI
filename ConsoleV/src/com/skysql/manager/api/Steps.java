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

public class Steps {

	LinkedHashMap<String, StepRecord> stepsList;

	public LinkedHashMap<String, StepRecord> getStepsList() {
		return stepsList;
	}

	public void setStepsList(LinkedHashMap<String, StepRecord> stepsList) {
		this.stepsList = stepsList;
	}

	public Steps() {

	}

	public Steps(String dummy) {
		APIrestful api = new APIrestful();
		if (api.get("command/step")) {
			Steps steps = APIrestful.getGson().fromJson(api.getResult(), Steps.class);
			if (steps != null) {
				this.stepsList = steps.stepsList;
			}
		}
	}

}

class StepsDeserializer implements JsonDeserializer<Steps> {
	public Steps deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

		Steps steps = new Steps();

		JsonElement jsonElement = json.getAsJsonObject().get("command_steps");
		if (jsonElement == null || jsonElement.isJsonNull()) {
			steps.setStepsList(null);
		} else {
			JsonArray array = jsonElement.getAsJsonArray();
			int length = array.size();

			LinkedHashMap<String, StepRecord> stepsList = new LinkedHashMap<String, StepRecord>(length);
			for (int i = 0; i < length; i++) {
				JsonObject backupJson = array.get(i).getAsJsonObject();

				JsonElement element;
				String id = (element = backupJson.get("id")).isJsonNull() ? null : element.getAsString();
				String script = (element = backupJson.get("script")).isJsonNull() ? null : element.getAsString();
				String icon = (element = backupJson.get("icon")).isJsonNull() ? null : element.getAsString();
				String description = (element = backupJson.get("description")).isJsonNull() ? null : element.getAsString();
				StepRecord stepRecord = new StepRecord(script, icon, description);
				stepsList.put(id, stepRecord);
			}
			steps.setStepsList(stepsList);
		}
		return steps;

	}

}
