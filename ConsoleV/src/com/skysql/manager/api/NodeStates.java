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
import java.util.Iterator;
import java.util.LinkedHashMap;
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

	private static LinkedHashMap<String, NodeStates> nodeStateRecords;
	private static LinkedHashMap<String, String> nodeStatesIDs;
	private static LinkedHashMap<String, String> nodeStatesIcons;
	private static LinkedHashMap<String, String> nodeStatesDescriptions;

	public static String getNodeID(String state) {
		GetNodeStates();
		String ID = nodeStatesIDs.get(state);
		return ID;
	}

	public static String getNodeIcon(String systemType, String state) {
		GetNodeStates();
		NodeStates nodeStates = NodeStates.nodeStateRecords.get(systemType);
		String icon = null;
		if (nodeStates != null) {
			icon = nodeStates.nodeStatesIcons.get(state);
		}
		return (icon == null ? "invalid" : icon);
	}

	public static String getDescription(String state) {
		GetNodeStates();
		String description = nodeStatesDescriptions.get(state);
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
					NodeStates.nodeStateRecords = nodeStates.nodeStateRecords;
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

	protected void setNodeStates(LinkedHashMap<String, NodeStates> nodeStates) {
		NodeStates.nodeStateRecords = nodeStates;
	}

	protected void setNodeStatesIDs(LinkedHashMap<String, String> pairs) {
		NodeStates.nodeStatesIDs = pairs;
	}

	protected void setNodeStatesIcons(LinkedHashMap<String, String> pairs) {
		NodeStates.nodeStatesIcons = pairs;
	}

	protected void setNodeStatesDescriptions(LinkedHashMap<String, String> pairs) {
		NodeStates.nodeStatesDescriptions = pairs;
	}
}

/***
{"nodestates":{
"aws":[{"state":"master","stateid":1,"description":"Master","icon":"master"},{"state":"slave","stateid":2,"description":"Slave Online","icon":"slave"},{"state":"offline","stateid":3,"description":"Slave Offline","icon":"offline"},{"state":"stopped","stateid":5,"description":"Slave Stopped","icon":"stopped"},{"state":"error","stateid":13,"description":"Slave Error","icon":"error"},{"state":"standalone","stateid":18,"description":"Standalone Database","icon":"node"}],
"galera":[{"state":"down","stateid":100,"description":"Down","icon":"stopped"},{"state":"open","stateid":101,"description":"Open","icon":"starting"},{"state":"primary","stateid":102,"description":"Primary","icon":"master"},{"state":"joiner","stateid":103,"description":"Joiner","icon":"promoting"},{"state":"joined","stateid":104,"description":"Joined","icon":"master"},{"state":"synced","stateid":105,"description":"Synced","icon":"master"},{"state":"donor","stateid":106,"description":"Donor","icon":"master"},{"state":"isolated","stateid":99,"description":"Isolated","icon":"isolated"}]},"warnings":["Caching directory \/usr\/local\/skysql\/cache\/api is not writeable, cannot write cache, please check existence, permissions, SELinux"]}
***/

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
				JsonArray statesArray = element.isJsonNull() ? null : element.getAsJsonArray();
				NodeStates states = parseStates(statesArray);
				stateRecords.put(entry.getKey(), states);
			}
			nodeStates.setNodeStates(stateRecords);

		}

		return nodeStates;
	}

	NodeStates parseStates(JsonArray array) {
		NodeStates nodeStates = new NodeStates();

		int length = array.size();
		LinkedHashMap<String, String> IDs = new LinkedHashMap<String, String>(length);
		LinkedHashMap<String, String> icons = new LinkedHashMap<String, String>(length);
		LinkedHashMap<String, String> descriptions = new LinkedHashMap<String, String>(length);
		for (int i = 0; i < length; i++) {
			JsonObject stateObject = array.get(i).getAsJsonObject();
			String state = stateObject.get("state").getAsString();
			IDs.put(state, stateObject.get("stateid").getAsString());
			icons.put(state, stateObject.get("icon").getAsString());
			descriptions.put(state, stateObject.get("description").getAsString());
		}
		nodeStates.setNodeStatesIDs(IDs);
		nodeStates.setNodeStatesIcons(icons);
		nodeStates.setNodeStatesDescriptions(descriptions);

		return nodeStates;
	}

}
