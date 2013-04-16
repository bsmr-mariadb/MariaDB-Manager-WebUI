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
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class RunSQL {

	private boolean success;
	private String[] results;
	private String error;

	public boolean getSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String[] getResults() {
		return results;
	}

	public void setResults(String[] results) {
		this.results = results;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public RunSQL() {

	}

	public RunSQL(String SQL) {
		String inputLine = null;
		try {
			URL url = new URI("http", AppData.oldAPIurl, "/consoleAPI/runsql.php", "node=" + 1 + "&sql=" + SQL, null).toURL();
			URLConnection sc = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(sc.getInputStream()));
			inputLine = in.readLine();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Could not get response from API");
		}

		Gson gson = AppData.getGson();
		RunSQL runSQL = gson.fromJson(inputLine, RunSQL.class);
		this.success = runSQL.getSuccess();
		this.error = runSQL.getError();
		this.results = runSQL.getResults();

	}

}

class RunSQLDeserializer implements JsonDeserializer<RunSQL> {
	public RunSQL deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		RunSQL runSQL = new RunSQL();

		JsonObject jsonObject = json.getAsJsonObject();

		if (jsonObject == null || jsonObject.isJsonNull()) {

		} else {
			JsonElement element = jsonObject.get("results");
			if (element == null) {
				runSQL.setSuccess(false);
				runSQL.setError(jsonObject.get("error").getAsString());
			} else {
				runSQL.setSuccess(true);
				JsonArray array = element.getAsJsonArray();
				int length = array.size();

				String[] results = new String[length];
				for (int i = 0; i < length; i++) {
					jsonObject = array.get(i).getAsJsonObject();
					results[i] = jsonObject.toString();
				}

				runSQL.setResults(results);
			}

		}

		return runSQL;
	}
}
