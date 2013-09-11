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

import org.json.JSONObject;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.skysql.manager.ClusterComponent;
import com.skysql.manager.Commands;
import com.skysql.manager.MonitorLatest;
import com.skysql.manager.ui.ErrorDialog;
import com.skysql.manager.ui.RunningTask;

public class NodeInfo extends ClusterComponent {

	private static final String NOT_AVAILABLE = "n/a";

	private Commands commands;
	private String task;
	private String command;
	private String hostname;
	private String privateIP;
	private String publicIP;
	private String instanceID;
	private String username;
	private String password;
	private RunningTask commandTask;

	public Commands getCommands() {
		return commands;
	}

	protected void setCommands(Commands commands) {
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

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
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

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public RunningTask getCommandTask() {
		return commandTask;
	}

	public void setCommandTask(RunningTask commandTask) {
		this.commandTask = commandTask;
	}

	public boolean save() {

		try {
			APIrestful api = new APIrestful();
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("name", this.name);
			jsonParam.put("hostname", this.hostname);
			jsonParam.put("instanceid", this.instanceID);
			jsonParam.put("publicip", this.publicIP);
			jsonParam.put("privateip", this.privateIP);
			jsonParam.put("dbusername", this.username);
			if (this.password != null) {
				jsonParam.put("dbpassword", this.password);
			} else {
				jsonParam.put("dbpassword", JSONObject.NULL);
			}
			if (api.put("system/" + parentID + "/node" + (ID == null || ID.isEmpty() ? "" : "/" + ID), jsonParam.toString())) {
				WriteResponse writeResponse = APIrestful.getGson().fromJson(api.getResult(), WriteResponse.class);
				if (writeResponse != null) {
					if (ID == null && !writeResponse.getInsertKey().isEmpty()) {
						ID = writeResponse.getInsertKey();
						return true;
					} else if (!ID.isEmpty() && (writeResponse.getUpdateCount() > 0 || !writeResponse.getInsertKey().isEmpty())) {
						return true;
					} else {
						return false;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			new ErrorDialog(e, "Error encoding API request");
		}

		return false;

	}

	public boolean delete() {

		APIrestful api = new APIrestful();
		if (api.delete("system/" + parentID + "/node/" + ID)) {
			WriteResponse writeResponse = APIrestful.getGson().fromJson(api.getResult(), WriteResponse.class);
			if (writeResponse != null && writeResponse.getDeleteCount() > 0) {
				return true;
			}
		}
		return false;
	}

	public NodeInfo() {
	}

	public NodeInfo(String systemID, String systemType) {
		this.type = CCType.node;
		this.parentID = systemID;
		this.systemType = systemType;
	}

	public NodeInfo(String systemID, String systemType, String nodeID) {

		APIrestful api = new APIrestful();
		if (api.get("system/" + systemID + "/node/" + nodeID)) {
			try {
				NodeInfo nodeInfo = APIrestful.getGson().fromJson(api.getResult(), NodeInfo.class);
				this.type = CCType.node;
				this.parentID = systemID;
				this.systemType = systemType;
				this.ID = nodeID;
				this.name = nodeInfo.name;
				this.state = nodeInfo.state;
				this.capacity = nodeInfo.capacity;
				this.commands = nodeInfo.commands;
				this.monitorLatest = nodeInfo.monitorLatest;
				this.task = nodeInfo.task;
				this.command = nodeInfo.command;
				this.hostname = nodeInfo.hostname;
				this.privateIP = nodeInfo.privateIP;
				this.publicIP = nodeInfo.publicIP;
				this.instanceID = nodeInfo.instanceID;
				this.username = nodeInfo.username;
				this.password = nodeInfo.password;
			} catch (NullPointerException e) {
				new ErrorDialog(e, "API did not return expected result for:" + api.errorString());
				throw new RuntimeException("API response");
			} catch (JsonParseException e) {
				new ErrorDialog(e, "JSON parse error in API results for:" + api.errorString());
				throw new RuntimeException("API response");
			}
		}

	}

	public String ToolTip() {

		return "<h2>Node</h2>" + "<ul>" + "<li><b>ID:</b> " + this.ID + "</li>" + "<li><b>Name:</b> " + this.name + "</li>" + "<li><b>Hostname:</b> "
				+ this.hostname + "</li>" + "<li><b>Public IP:</b> " + this.publicIP + "</li>" + "<li><b>Private IP:</b> " + this.privateIP + "</li>"
				+ "<li><b>Instance ID:</b> " + this.instanceID + "<li><b>Username:</b> " + this.username + "<li><b>Password:</b> " + this.password + "</li>"
				+ "<li><b>State:</b> " + ((this.state == null) ? NOT_AVAILABLE : NodeStates.getDescription(this.state)) + "</li>" + "<li><b>Monitors:</b> "
				+ ((this.monitorLatest == null) ? NOT_AVAILABLE : monitorLatest.getData().toString()) + "</li>" + "<li><b>Available Commands:</b> "
				+ ((this.commands == null) ? NOT_AVAILABLE : getCommands().getNames().keySet()) + "</li>" + "<li><b>Task ID:</b> "
				+ ((this.task == null) ? NOT_AVAILABLE : this.task) + "</li>" + "<li><b>Running Command:</b> "
				+ ((this.command == null) ? NOT_AVAILABLE : this.command) + "</li>" + "</ul>";

	}

}

// {"node":{"systemid":"1","nodeid":"1","name":"Node1","state":"offline","hostname":"","publicip":"10.0.0.1","privateip":"10.0.0.1","port":"0","instanceid":"","dbusername":"","dbpassword":"","commands":[{"command":"backup","description":"Backup Offline Slave Node","icon":"backup","steps":"backup"},{"command":"restart","description":"Restore Offline Slave Node","icon":"stop","steps":"restore"}],"monitorlatest":{"connections":null,"traffic":null,"availability":null,"nodestate":null,"capacity":null,"hoststate":null,"clustersize":null,"reppaused":null,"parallelism":null,"recvqueue":null,"flowcontrol":null,"sendqueue":null},"command":null,"taskid":null},"warnings":["Caching directory \/usr\/local\/skysql\/cache\/api is not writeable, cannot write cache, please check existence, permissions, SELinux"]}

class NodeInfoDeserializer implements JsonDeserializer<NodeInfo> {
	public NodeInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException, NullPointerException {
		NodeInfo nodeInfo = new NodeInfo();

		JsonElement element = json.getAsJsonObject().get("node");
		if (element.isJsonNull()) {
			return null;
		} else {

			JsonObject jsonObject = element.getAsJsonObject();
			nodeInfo.setName(((element = jsonObject.get("name")) == null || element.isJsonNull()) ? null : element.getAsString());
			nodeInfo.setState(((element = jsonObject.get("state")) == null || element.isJsonNull()) ? null : element.getAsString());
			nodeInfo.setTask(((element = jsonObject.get("taskid")) == null || element.isJsonNull()) ? null : element.getAsString());
			nodeInfo.setCommand(((element = jsonObject.get("command")) == null || element.isJsonNull()) ? null : element.getAsString());
			nodeInfo.setHostname(((element = jsonObject.get("hostname")) == null || element.isJsonNull()) ? null : element.getAsString());
			nodeInfo.setPrivateIP(((element = jsonObject.get("privateip")) == null || element.isJsonNull()) ? null : element.getAsString());
			nodeInfo.setPublicIP(((element = jsonObject.get("publicip")) == null || element.isJsonNull()) ? null : element.getAsString());
			nodeInfo.setInstanceID(((element = jsonObject.get("instanceid")) == null || element.isJsonNull()) ? null : element.getAsString());
			nodeInfo.setUsername(((element = jsonObject.get("dbusername")) == null || element.isJsonNull()) ? null : element.getAsString());
			nodeInfo.setPassword(((element = jsonObject.get("dbpassword")) == null || element.isJsonNull()) ? null : element.getAsString());

			if ((element = jsonObject.get("monitorlatest")) != null && !element.isJsonNull()) {
				MonitorLatest monitorLatest = APIrestful.getGson().fromJson(element.toString(), MonitorLatest.class);
				nodeInfo.setMonitorLatest(monitorLatest);
			}

			if ((element = jsonObject.get("commands")) != null && !element.isJsonNull()) {
				Commands commands = APIrestful.getGson().fromJson("{\"commands\":" + element.toString() + "}", Commands.class);
				nodeInfo.setCommands(commands);
			}

		}
		return nodeInfo;
	}
}
