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
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.skysql.manager.ui.ErrorDialog;

public class SettingsValues {

	public static final String SETTINGS_MAX_BACKUP_SIZE = "maxBackupSize";
	public static final String SETTINGS_MAX_BACKUP_COUNT = "maxBackupCount";
	public static final String SETTINGS_MONITOR_INTERVAL = "monitorInterval";

	private String[] values;

	public String[] getValues() {
		return values;
	}

	protected void setValues(String[] values) {
		this.values = values;
	}

	public SettingsValues() {
	}

	public SettingsValues(String property) {
		APIrestful api = new APIrestful();
		if (api.get("application/1/property/" + property)) {
			try {
				SettingsValues settingsValues = APIrestful.getGson().fromJson(api.getResult(), SettingsValues.class);
				this.values = settingsValues.values;
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

class SettingsValuesDeserializer implements JsonDeserializer<SettingsValues> {
	public SettingsValues deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException, NullPointerException {
		SettingsValues settingsValues = new SettingsValues();

		JsonElement jsonElement = json.getAsJsonObject().get("applicationproperty");
		if (jsonElement.isJsonNull()) {
			settingsValues.setValues(null);
		} else {
			JsonObject jsonObject = (JsonObject) jsonElement;
			Set<Entry<String, JsonElement>> set = jsonObject.entrySet();
			Iterator<Entry<String, JsonElement>> iter = set.iterator();
			if (iter.hasNext()) {
				Entry<String, JsonElement> entry = iter.next();
				String value = entry.getValue().getAsString();
				settingsValues.setValues(value.split(","));
			}
		}

		return settingsValues;
	}
}
