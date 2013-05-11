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

package com.skysql.consolev.api;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class CommandStates {

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

	private static void GetCommandStates() {
		if (commandStates == null) {
			APIrestful api = new APIrestful();
			if (api.get("command/state")) {
				commandStates = AppData.getGson().fromJson(api.getResult(), CommandStates.class);
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
	public CommandStates deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		CommandStates commandStates = new CommandStates();

		JsonElement jsonElement = json.getAsJsonObject().get("commandStates");
		if (jsonElement == null || jsonElement.isJsonNull()) {
			commandStates.setIcons(null);
			commandStates.setDescriptions(null);
		} else {
			JsonArray array = jsonElement.getAsJsonArray();
			int length = array.size();

			LinkedHashMap<String, String> icons = new LinkedHashMap<String, String>(length);
			LinkedHashMap<String, String> descriptions = new LinkedHashMap<String, String>(length);
			for (int i = 0; i < length; i++) {
				JsonObject pair = array.get(i).getAsJsonObject();
				icons.put(pair.get("id").getAsString(), pair.get("icon").getAsString());
				descriptions.put(pair.get("id").getAsString(), pair.get("description").getAsString());
			}
			commandStates.setIcons(icons);
			commandStates.setDescriptions(descriptions);
		}

		return commandStates;
	}

}
