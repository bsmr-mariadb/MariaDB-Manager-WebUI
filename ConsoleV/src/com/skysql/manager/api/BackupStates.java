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
import java.util.LinkedHashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.skysql.manager.ui.ErrorDialog;

public class BackupStates {

	private static BackupStates backupStates;
	private static LinkedHashMap<String, String> backupStatesDescriptions;

	public static LinkedHashMap<String, String> getDescriptions() {
		GetBackupStates();
		return BackupStates.backupStatesDescriptions;
	}

	public static boolean load() {
		GetBackupStates();
		return (backupStates != null);
	}

	private static void GetBackupStates() {
		if (backupStates == null) {
			APIrestful api = new APIrestful();
			if (api.get("backupstate")) {
				try {
					backupStates = APIrestful.getGson().fromJson(api.getResult(), BackupStates.class);
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

	protected void setBackupStatesDescriptions(LinkedHashMap<String, String> pairs) {
		BackupStates.backupStatesDescriptions = pairs;
	}
}

class BackupStatesDeserializer implements JsonDeserializer<BackupStates> {
	public BackupStates deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException, NullPointerException {
		BackupStates backupStates = new BackupStates();

		JsonElement jsonElement = json.getAsJsonObject().get("backupStates");
		if (jsonElement.isJsonNull()) {
			backupStates.setBackupStatesDescriptions(null);
		} else {
			JsonArray array = jsonElement.getAsJsonArray();
			int length = array.size();

			LinkedHashMap<String, String> descriptions = new LinkedHashMap<String, String>(length);
			for (int i = 0; i < length; i++) {
				JsonObject pair = array.get(i).getAsJsonObject();
				descriptions.put(pair.get("state").getAsString(), pair.get("description").getAsString());
			}
			backupStates.setBackupStatesDescriptions(descriptions);
		}

		return backupStates;
	}

}
