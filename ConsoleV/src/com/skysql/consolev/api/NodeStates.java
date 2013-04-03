package com.skysql.consolev.api;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class NodeStates {

	private static NodeStates nodeStates;
	private static LinkedHashMap<String, String> nodeStatesIcons;
	private static LinkedHashMap<String, String> nodeStatesDescriptions;

	public static String getNodeIcon(String state) {
		GetNodeStates();
		String icon = nodeStatesIcons.get(state);
		return (icon == null ? "invalid" : icon);
	}

	public static LinkedHashMap<String, String> getNodeStatesDescriptions() {
		GetNodeStates();
		return NodeStates.nodeStatesDescriptions;
	}

	private synchronized static void GetNodeStates() {
		if (nodeStates == null) {
			String inputLine = null;
			try {
				APIrestful api = new APIrestful();
				inputLine = api.get("nodestate");
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("Could not get response from API");
			}

			Gson gson = AppData.getGson();
			nodeStates = gson.fromJson(inputLine, NodeStates.class);
		}
	}

	protected void setNodeStatesIcons(LinkedHashMap<String, String> pairs) {
		NodeStates.nodeStatesIcons = pairs;
	}

	protected void setNodeStatesDescriptions(LinkedHashMap<String, String> pairs) {
		NodeStates.nodeStatesDescriptions = pairs;
	}
}

class NodeStatesDeserializer implements JsonDeserializer<NodeStates> {
	public NodeStates deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		NodeStates nodeStates = new NodeStates();

		JsonElement jsonElement = json.getAsJsonObject().get("nodestates");
		if (jsonElement == null || jsonElement.isJsonNull()) {
			nodeStates.setNodeStatesIcons(null);
			nodeStates.setNodeStatesDescriptions(null);
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
			nodeStates.setNodeStatesIcons(icons);
			nodeStates.setNodeStatesDescriptions(descriptions);
		}

		return nodeStates;
	}

}
