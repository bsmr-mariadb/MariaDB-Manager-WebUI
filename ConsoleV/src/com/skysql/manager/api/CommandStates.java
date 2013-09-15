/*
 * This file is distributed as import java.io.BufferedReader;
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
ould have received a copy of the GNU General Public License along with
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
import com.skysql.manager.ui.ErrorDialog;

public class CommandStates {

	public enum CommandState {
		scheduled, running, paused, stopped, done, error;
	}

	private static CommandStates commandStates;
	private static LinkedHashMap<String, String> icons;
	private static LinkedHashMap<String, String> descriptions;

	public static LinkedHashMap<String, String> getIcons() {
		GetCommandStates();
		return CommandStates.icons;
	}

	public static LinkedHashMap<String, String> getDescriptions() {
		GetCommandStates();
		return CommandStates.descriptions;
	}

	public static boolean load() {
		GetCommandStates();
		return (commandStates != null);
	}

	private static void GetCommandStates() {
		if (commandStates == null) {
			APIrestful api = new APIrestful();
			if (api.get("command/state")) {
				try {
					commandStates = APIrestful.getGson().fromJson(api.getResult(), CommandStates.class);
					// TODO: verify states against enum and throw error or log warning depending on discrepancy (missing state: error, new unknown state: warning)

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

	protected void setIcons(LinkedHashMap<String, String> pairs) {
		CommandStates.icons = pairs;
	}

	protected void setDescriptions(LinkedHashMap<String, String> pairs) {
		CommandStates.descriptions = pairs;
	}

}

class CommandStatesDeserializer implements JsonDeserializer<CommandStates> {
	public CommandStates deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException, NullPointerException {
		CommandStates commandStates = new CommandStates();

		JsonElement jsonElement = json.getAsJsonObject().get("commandStates");
		if (jsonElement.isJsonNull()) {
			commandStates.setIcons(null);
			commandStates.setDescriptions(null);
		} else {
			JsonArray array = jsonElement.getAsJsonArray();
			int length = array.size();

			LinkedHashMap<String, String> icons = new LinkedHashMap<String, String>(length);
			LinkedHashMap<String, String> descriptions = new LinkedHashMap<String, String>(length);
			for (int i = 0; i < length; i++) {
				JsonObject pair = array.get(i).getAsJsonObject();
				icons.put(pair.get("state").getAsString(), pair.get("icon").getAsString());
				descriptions.put(pair.get("state").getAsString(), pair.get("description").getAsString());
			}
			commandStates.setIcons(icons);
			commandStates.setDescriptions(descriptions);
		}

		return commandStates;
	}

}