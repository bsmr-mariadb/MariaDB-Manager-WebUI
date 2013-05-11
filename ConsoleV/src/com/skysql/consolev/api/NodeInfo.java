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

import java.lang.reflect.Type;

import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.skysql.consolev.ui.RunningTask;

public class NodeInfo extends ClusterComponent {

	private String systemID;
	private String[] commands;
	private String task;
	private String command;
	private String privateIP;
	private String publicIP;
	private String instanceID;
	private RunningTask commandTask;
	private String capacity;

	public String getSystemID() {
		return systemID;
	}

	public void setSystemID(String systemID) {
		this.systemID = systemID;
	}

	public String[] getCommands() {
		return commands;
	}

	public void setCommands(String[] commands) {
		this.commands = commands;
	}

	public String getTask() {
		return task;
	}

	public void setTask(String task) {
		this.task = task;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getPrivateIP() {
		return privateIP;
	}

	public void setPrivateIP(String privateIP) {
		this.privateIP = privateIP;
	}

	public String getPublicIP() {
		return publicIP;
	}

	public void setPublicIP(String publicIP) {
		this.publicIP = publicIP;
	}

	public String getInstanceID() {
		return instanceID;
	}

	public void setInstanceID(String instanceID) {
		this.instanceID = instanceID;
	}

	public RunningTask getCommandTask() {
		return commandTask;
	}

	public void setCommandTask(RunningTask commandTask) {
		this.commandTask = commandTask;
	}

	public String getCapacity() {
		return capacity;
	}

	public void setCapacity(String capacity) {
		this.capacity = capacity;
	}

	public void saveName(String name) {

		try {
			APIrestful api = new APIrestful();
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("name", name);
			api.put("system/" + systemID + "/node/" + ID, jsonParam.toString());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error preparing API call");
		}

	}

	public NodeInfo() {
	}

	public NodeInfo(String systemID, String nodeID) {

		APIrestful api = new APIrestful();
		if (api.get("system/" + systemID + "/node/" + nodeID)) {
			NodeInfo nodeInfo = AppData.getGson().fromJson(api.getResult(), NodeInfo.class);
			this.type = ClusterComponent.CCType.node;
			this.systemID = systemID;
			this.ID = nodeID;
			this.name = nodeInfo.name;
			this.status = nodeInfo.status;
			this.health = nodeInfo.health;
			this.connections = nodeInfo.connections;
			this.packets = nodeInfo.packets;
			this.commands = nodeInfo.commands;
			this.task = nodeInfo.task;
			this.command = nodeInfo.command;
			this.privateIP = nodeInfo.privateIP;
			this.publicIP = nodeInfo.publicIP;
			this.instanceID = nodeInfo.instanceID;
			this.type = CCType.node;
		}

	}

	public String ToolTip() {
		StringBuffer commands = new StringBuffer("[");
		if (this.commands != null && this.commands.length > 0) {
			for (String command : this.commands) {
				commands.append(Commands.getNames().get(command));
				commands.append(",");
			}
			commands.deleteCharAt(commands.length() - 1);
		}
		commands.append("]");

		return "<h2>Node</h2>" + "<ul>" + "<li><b>ID:</b> " + this.ID + "</li>" + "<li><b>Name:</b> " + this.name + "</li>" + "<li><b>Public IP:</b> "
				+ this.publicIP + "</li>" + "<li><b>Private IP:</b> " + this.privateIP + "</li>" + "<li><b>Instance ID:</b> " + this.instanceID + "</li>"
				+ "<li><b>Status:</b> " + ((this.status == null) ? "n/a" : NodeStates.getNodeStatesDescriptions().get(this.status)) + "</li>"
				+ "<li><b>Availabilty:</b> " + ((this.health == null) ? "n/a" : this.health) + "%</li>" + "<li><b>Connections:</b> "
				+ ((this.connections == null) ? "n/a" : this.connections) + "</li>" + "<li><b>Data Transfer:</b> "
				+ ((this.packets == null) ? "n/a" : this.packets) + " KB</li>" + "<li><b>Available Commands:</b> "
				+ ((this.commands == null) ? "n/a" : commands) + "</li>" + "<li><b>Task ID:</b> " + ((this.task == null) ? "n/a" : this.task) + "</li>"
				+ "<li><b>Running Command:</b> " + ((this.command == null) ? "n/a" : Commands.getNames().get(this.command)) + "</li>" + "</ul>";

	}

}

class NodeInfoDeserializer implements JsonDeserializer<NodeInfo> {
	public NodeInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		NodeInfo nodeInfo = new NodeInfo();

		JsonElement element = json.getAsJsonObject().get("node");
		if (element == null || element.isJsonNull()) {
			return null;
		} else {

			JsonObject jsonObject = (JsonObject) element.getAsJsonArray().get(0);
			nodeInfo.setName(((element = jsonObject.get("name")) == null || element.isJsonNull()) ? null : element.getAsString());
			nodeInfo.setStatus(((element = jsonObject.get("state")) == null || element.isJsonNull()) ? null : element.getAsString());
			nodeInfo.setTask(((element = jsonObject.get("task")) == null || element.isJsonNull()) ? null : element.getAsString());
			nodeInfo.setCommand(((element = jsonObject.get("command")) == null || element.isJsonNull()) ? null : element.getAsString());
			nodeInfo.setPrivateIP(((element = jsonObject.get("privateIP")) == null || element.isJsonNull()) ? null : element.getAsString());
			nodeInfo.setPublicIP(((element = jsonObject.get("publicIP")) == null || element.isJsonNull()) ? null : element.getAsString());
			nodeInfo.setInstanceID(((element = jsonObject.get("instanceID")) == null || element.isJsonNull()) ? null : element.getAsString());
			if ((element = jsonObject.get("health")) != null) {
				if ((element = element.getAsJsonArray()) != null && ((element = ((JsonArray) element).get(0)) != null) && !element.isJsonNull()) {
					nodeInfo.setHealth(element.getAsString());
				}
			}
			if ((element = jsonObject.get("connections")) != null) {
				if ((element = element.getAsJsonArray()) != null && ((element = ((JsonArray) element).get(0)) != null) && !element.isJsonNull()) {
					nodeInfo.setConnections(element.getAsString());
				}
			}
			if ((element = jsonObject.get("packets")) != null) {
				if ((element = element.getAsJsonArray()) != null && ((element = ((JsonArray) element).get(0)) != null) && !element.isJsonNull()) {
					nodeInfo.setPackets(element.getAsString());
				}
			}

			element = jsonObject.get("commands");
			if (element == null || element.isJsonNull()) {
				nodeInfo.setCommands(new String[0]);
			} else {
				JsonArray commandsJson = element.getAsJsonArray();
				int length = commandsJson.size();
				String[] commands = new String[length];
				nodeInfo.setCommands(commands);
				for (int i = 0; i < length; i++) {
					commands[i] = commandsJson.get(i).getAsString();
				}
			}
		}
		return nodeInfo;
	}
}
