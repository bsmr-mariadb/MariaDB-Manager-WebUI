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
import com.skysql.consolev.ui.RunningTask;
import com.vaadin.ui.VerticalLayout;

public class NodeInfo {

	private String systemID;
	private String nodeID;
	private String name;
	private String status;
	private String health;
	private String connections;
	private String packets;
	private String[] commands;
	private String task;
	private String command;
	private String privateIP;
	private String publicIP;
	private String instanceID;
	private VerticalLayout button;
	private RunningTask commandTask;
	private RunningTask backupTask;
	private String capacity;
	private String type;

	public String getSystemID() {
		return systemID;
	}

	public void setSystemID(String systemID) {
		this.systemID = systemID;
	}

	public String getNodeID() {
		return nodeID;
	}

	public void setNodeID(String nodeID) {
		this.nodeID = nodeID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getHealth() {
		return health;
	}

	public void setHealth(String health) {
		this.health = health;
	}

	public String getConnections() {
		return connections;
	}

	public void setConnections(String connections) {
		this.connections = connections;
	}

	public String getPackets() {
		return packets;
	}

	public void setPackets(String packets) {
		this.packets = packets;
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

	public VerticalLayout getButton() {
		return button;
	}

	public void setButton(VerticalLayout button) {
		this.button = button;
	}

	public RunningTask getCommandTask() {
		return commandTask;
	}

	public void setCommandTask(RunningTask commandTask) {
		this.commandTask = commandTask;
	}

	public RunningTask getBackupTask() {
		return backupTask;
	}

	public void setBackupTask(RunningTask backupTask) {
		this.backupTask = backupTask;
	}

	public String getCapacity() {
		return capacity;
	}

	public void setCapacity(String capacity) {
		this.capacity = capacity;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public NodeInfo() {
	}

	public NodeInfo(String systemID, String nodeID) {

		String inputLine = null;
		try {
			URL url = new URI("http", "localhost", "/consoleAPI/nodeinfo.php", "system=" + systemID + "&node=" + nodeID, null).toURL();
			URLConnection sc = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(sc.getInputStream()));
			inputLine = in.readLine();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Could not get response from API");
		}

		Gson gson = AppData.getGson();
		NodeInfo nodeInfo = gson.fromJson(inputLine, NodeInfo.class);
		this.systemID = systemID;
		this.nodeID = nodeID;
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
		this.type = nodeID.equalsIgnoreCase("0") ? "system" : "node";
		nodeInfo = null;

	}

	public String ToolTip() {
		StringBuffer commands = new StringBuffer("[");
		if (this.commands != null) {
			for (String command : this.commands) {
				commands.append(Commands.getNames().get(command));
				commands.append(",");
			}
			commands.deleteCharAt(commands.length() - 1);
			commands.append("]");
		}

		return "<h2>" + (this.nodeID.equalsIgnoreCase("0") ? "System" : "Node") + "</h2>" + "<ul>" + "<li><b>ID:</b> " + this.nodeID + "</li>"
				+ "<li><b>Name:</b> " + this.name + "</li>" + (this.nodeID.equalsIgnoreCase("0") ? "" : "<li><b>Public IP:</b> " + this.publicIP + "</li>")
				+ (this.nodeID.equalsIgnoreCase("0") ? "" : "<li><b>Private IP:</b> " + this.privateIP + "</li>")
				+ (this.nodeID.equalsIgnoreCase("0") ? "" : "<li><b>Instance ID:</b> " + this.instanceID + "</li>") + "<li><b>Status:</b> "
				+ ((this.status == null) ? "n/a" : NodeStates.getNodeStatesDescriptions().get(this.status)) + "</li>" + "<li><b>Availabilty:</b> "
				+ ((this.health == null) ? "n/a" : this.health) + "%</li>" + "<li><b>Connections:</b> "
				+ ((this.connections == null) ? "n/a" : this.connections) + "</li>" + "<li><b>Data Transfer:</b> "
				+ ((this.packets == null) ? "n/a" : this.packets) + " KB</li>" + "<li><b>Available Commands:</b> "
				+ ((this.commands == null) ? "n/a" : commands) + "</li>" + "<li><b>Task ID:</b> " + ((this.task == null) ? "n/a" : this.task) + "</li>"
				+ "<li><b>Running Command:</b> " + ((this.command == null) ? "n/a" : Commands.getNames().get(this.command)) + "</li>"
				+ (this.nodeID.equalsIgnoreCase("0") ? "" : "</ul>");
	}

}

class NodeInfoDeserializer implements JsonDeserializer<NodeInfo> {
	public NodeInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		NodeInfo nodeInfo = new NodeInfo();

		JsonObject jsonObject = json.getAsJsonObject();

		JsonElement element;
		nodeInfo.setName(((element = jsonObject.get("name")) == null || element.isJsonNull()) ? null : element.getAsString());
		nodeInfo.setStatus(((element = jsonObject.get("status")) == null || element.isJsonNull()) ? null : element.getAsString());
		nodeInfo.setHealth(((element = jsonObject.get("health")) == null || element.isJsonNull()) ? null : element.getAsString());
		nodeInfo.setConnections(((element = jsonObject.get("connections")) == null || element.isJsonNull()) ? null : element.getAsString());
		nodeInfo.setPackets(((element = jsonObject.get("packets")) == null || element.isJsonNull()) ? null : element.getAsString());
		nodeInfo.setTask(((element = jsonObject.get("task")) == null || element.isJsonNull()) ? null : element.getAsString());
		nodeInfo.setCommand(((element = jsonObject.get("command")) == null || element.isJsonNull()) ? null : element.getAsString());
		nodeInfo.setPrivateIP(((element = jsonObject.get("privateIP")) == null || element.isJsonNull()) ? null : element.getAsString());
		nodeInfo.setPublicIP(((element = jsonObject.get("publicIP")) == null || element.isJsonNull()) ? null : element.getAsString());
		nodeInfo.setInstanceID(((element = jsonObject.get("instanceID")) == null || element.isJsonNull()) ? null : element.getAsString());

		element = jsonObject.get("commands");
		if (element == null || element.isJsonNull()) {
			nodeInfo.setCommands(new String[0]);
		} else {
			JsonArray commandsJson = element.getAsJsonArray();
			int length = commandsJson.size();
			String[] commands = new String[length];
			nodeInfo.setCommands(commands);
			for (int i = 0; i < length; i++)
				commands[i] = commandsJson.get(i).getAsString();
		}

		return nodeInfo;
	}

}
