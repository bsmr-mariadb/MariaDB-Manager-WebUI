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

public class Commands {

	private static Commands commands;
	private static LinkedHashMap<String, String> icons;
	private static LinkedHashMap<String, String> descriptions;
	private static LinkedHashMap<String, String> names;

	public static LinkedHashMap<String, String> getIcons() {
		GetCommands();
		return Commands.icons;
	}

	public static LinkedHashMap<String, String> getDescriptions() {
		GetCommands();
		return Commands.descriptions;
	}

	public static LinkedHashMap<String, String> getNames() {
		GetCommands();
		return Commands.names;
	}

	private static void GetCommands() {
		if (commands == null) {
			String inputLine = null;
			try {
				URL url = new URL("http://localhost/consoleAPI/commands.php?group=control");
				URLConnection sc = url.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(sc.getInputStream()));
				inputLine = in.readLine();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("Could not get response from API");
			}

			Gson gson = AppData.getGson();
			commands = gson.fromJson(inputLine, Commands.class);
		}
	}

	protected void setIcons(LinkedHashMap<String, String> pairs) {
		Commands.icons = pairs;
	}

	protected void setDescriptions(LinkedHashMap<String, String> pairs) {
		Commands.descriptions = pairs;
	}

	protected void setNames(LinkedHashMap<String, String> pairs) {
		Commands.names = pairs;
	}
}

class CommandsDeserializer implements JsonDeserializer<Commands> {
	public Commands deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		Commands commands = new Commands();

		JsonElement jsonElement = json.getAsJsonObject().get("commands");
		if (jsonElement == null || jsonElement.isJsonNull()) {
			commands.setIcons(null);
			commands.setDescriptions(null);
			commands.setNames(null);
		} else {
			JsonArray array = jsonElement.getAsJsonArray();
			int length = array.size();

			LinkedHashMap<String, String> icons = new LinkedHashMap<String, String>(length);
			LinkedHashMap<String, String> descriptions = new LinkedHashMap<String, String>(length);
			LinkedHashMap<String, String> names = new LinkedHashMap<String, String>(length);
			for (int i = 0; i < length; i++) {
				JsonObject pair = array.get(i).getAsJsonObject();
				icons.put(pair.get("id").getAsString(), pair.get("icon").getAsString());
				descriptions.put(pair.get("id").getAsString(), pair.get("description").getAsString());
				names.put(pair.get("id").getAsString(), pair.get("name").getAsString());
			}
			commands.setIcons(icons);
			commands.setDescriptions(descriptions);
			commands.setNames(names);
		}

		return commands;
	}

}
