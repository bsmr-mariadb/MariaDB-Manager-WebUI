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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.skysql.manager.ui.ErrorDialog;

public class NodeStates {
	private static String INVALID_ICON = "invalid";

	private static final Map<String, String> awsIcons;
	static {
		awsIcons = new HashMap<String, String>();
		awsIcons.put("placeholder", "placeholder");
		awsIcons.put("created", "created");
		awsIcons.put("connected", "connected");
		awsIcons.put("unconnected", "unconnected");
		awsIcons.put("unprovisioned", "unprovisioned");
		awsIcons.put("incompatible", "incompatible");
		awsIcons.put("provisioned", "provisioned");
		awsIcons.put("master", "master");
		awsIcons.put("slave", "slave");
		awsIcons.put("offline", "offline");
		awsIcons.put("stopped", "stopped");
		awsIcons.put("error", "error");
		awsIcons.put("standalone", "standalone");
	}
	private static final Map<String, String> galeraIcons;
	static {
		galeraIcons = new HashMap<String, String>();
		galeraIcons.put("placeholder", "placeholder");
		galeraIcons.put("created", "created");
		galeraIcons.put("connected", "connected");
		galeraIcons.put("unconnected", "unconnected");
		galeraIcons.put("unprovisioned", "unprovisioned");
		galeraIcons.put("incompatible", "incompatible");
		galeraIcons.put("provisioned", "provisioned");
		galeraIcons.put("initialized", "initialized");
		galeraIcons.put("open", "open");
		galeraIcons.put("primary", "primary");
		galeraIcons.put("joiner", "joiner");
		galeraIcons.put("joined", "joined");
		galeraIcons.put("synced", "synced");
		galeraIcons.put("donor", "donor");
		galeraIcons.put("incorrectly-joined", "incorrectly-joined");
		galeraIcons.put("isolated", "isolated");
		galeraIcons.put("machine-down", "machine-down");
		galeraIcons.put("down", "down");
	}

	public static final Map<String, Map<String, String>> states;
	static {
		states = new HashMap<String, Map<String, String>>();
		states.put("aws", awsIcons);
		states.put("galera", galeraIcons);
	}

	private static LinkedHashMap<String, NodeStates> nodeStateRecords;
	private LinkedHashMap<String, String> nodeStatesDescriptions;

	public static String getNodeIcon(String systemType, String state) {
		String icon = null;
		if (systemType.equals("aws")) {
			icon = awsIcons.get(state);
		} else if (systemType.equals("galera")) {
			icon = galeraIcons.get(state);
		}

		if (icon == null) {
			System.err.println("Unknown system type + node state found in API response: " + systemType + ", " + state);
			icon = INVALID_ICON;
		}

		return icon;
	}

	public static String getDescription(String systemType, String state) {
		GetNodeStates();
		NodeStates nodeStates = NodeStates.nodeStateRecords.get(systemType);
		String description = nodeStates.nodeStatesDescriptions.get(state);
		return (description == null ? "Invalid" : description);
	}

	public static boolean load() {
		GetNodeStates();
		return (nodeStateRecords != null);
	}

	private synchronized static void GetNodeStates() {
		if (nodeStateRecords == null) {
			APIrestful api = new APIrestful();
			if (api.get("nodestate")) {
				try {
					NodeStates nodeStates = APIrestful.getGson().fromJson(api.getResult(), NodeStates.class);
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

	protected static void setNodeStates(LinkedHashMap<String, NodeStates> nodeStates) {
		NodeStates.nodeStateRecords = nodeStates;
	}

	protected void setNodeStatesDescriptions(LinkedHashMap<String, String> pairs) {
		nodeStatesDescriptions = pairs;
	}
}

// {"nodestates":{"aws":[{"state":"provisioned","stateid":10001,"description":"Has agent, scripts, database"},{"state":"master","stateid":1,"description":"Master"},{"state":"slave","stateid":2,"description":"Slave Online"},{"state":"offline","stateid":3,"description":"Slave Offline"},{"state":"stopped","stateid":5,"description":"Slave Stopped"},{"state":"error","stateid":13,"description":"Slave Error"},{"state":"standalone","stateid":18,"description":"Standalone Database"}],"galera":[{"state":"provisioned","stateid":10001,"description":"Has agent, scripts, database"},{"state":"down","stateid":100,"description":"Down"},{"state":"open","stateid":101,"description":"Open"},{"state":"primary","stateid":102,"description":"Primary"},{"state":"joiner","stateid":103,"description":"Joiner"},{"state":"joined","stateid":104,"description":"Joined"},{"state":"synced","stateid":105,"description":"Synced"},{"state":"donor","stateid":106,"description":"Donor"},{"state":"isolated","stateid":99,"description":"Isolated"}]}
class NodeStatesDeserializer implements JsonDeserializer<NodeStates> {

	public NodeStates deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException, NullPointerException {
		NodeStates nodeStates = new NodeStates();

		JsonElement jsonElement = json.getAsJsonObject().get("nodestates");
		if (jsonElement != null && !jsonElement.isJsonNull()) {

			LinkedHashMap<String, NodeStates> stateRecords = new LinkedHashMap<String, NodeStates>();
			Set<Entry<String, JsonElement>> set = jsonElement.getAsJsonObject().entrySet();
			Iterator<Entry<String, JsonElement>> iter = set.iterator();
			while (iter.hasNext()) {
				Entry<String, JsonElement> entry = iter.next();
				JsonElement element = entry.getValue();
				String systemType = entry.getKey();
				JsonArray statesArray = element.isJsonNull() ? null : element.getAsJsonArray();
				NodeStates states = parseStates(systemType, statesArray);
				stateRecords.put(systemType, states);
			}
			nodeStates.setNodeStates(stateRecords);

		}

		return nodeStates;
	}

	private NodeStates parseStates(String systemType, JsonArray array) {
		NodeStates nodeStates = new NodeStates();

		int length = array.size();
		LinkedHashMap<String, String> descriptions = new LinkedHashMap<String, String>(length);
		for (int i = 0; i < length; i++) {
			JsonObject stateObject = array.get(i).getAsJsonObject();
			String state = stateObject.get("state").getAsString();
			if (NodeStates.states.get(systemType).containsKey(state)) {
				descriptions.put(state, stateObject.get("description").getAsString());
			} else {
				new ErrorDialog(null, "Unrecognised state: \"" + state + "\" for system type: \"" + systemType + "\"");
			}
		}
		nodeStates.setNodeStatesDescriptions(descriptions);

		return nodeStates;
	}

}
