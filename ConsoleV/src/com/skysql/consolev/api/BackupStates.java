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
 * Copyright SkySQL Ab
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

public class BackupStates {

	private static BackupStates backupStates;
	private static LinkedHashMap<String, String> backupStatesDescriptions;

	public static LinkedHashMap<String, String> getDescriptions() {
		GetBackupStates();
		return BackupStates.backupStatesDescriptions;
	}

	private static void GetBackupStates() {
		if (backupStates == null) {
			String inputLine = null;
			try {
				URL url = new URL("http://localhost/consoleAPI/backupstates.php");
				URLConnection sc = url.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(sc.getInputStream()));
				inputLine = in.readLine();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("Could not get response from API");
			}

			Gson gson = AppData.getGson();
			backupStates = gson.fromJson(inputLine, BackupStates.class);
		}
	}

	protected void setBackupStatesDescriptions(LinkedHashMap<String, String> pairs) {
		BackupStates.backupStatesDescriptions = pairs;
	}
}

class BackupStatesDeserializer implements JsonDeserializer<BackupStates> {
	public BackupStates deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		BackupStates backupStates = new BackupStates();

		JsonElement jsonElement = json.getAsJsonObject().get("backupStates");
		if (jsonElement == null || jsonElement.isJsonNull()) {
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
