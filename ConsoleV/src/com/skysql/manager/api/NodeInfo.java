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

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.skysql.manager.ClusterComponent;
import com.skysql.manager.Commands;
import com.skysql.manager.MonitorLatest;
import com.skysql.manager.TaskRecord;
import com.skysql.manager.ui.ErrorDialog;
import com.skysql.manager.ui.RunningTask;

public class NodeInfo extends ClusterComponent {

	private static final String NOT_AVAILABLE = "n/a";

	private Commands commands;
	private TaskRecord task;
	private String hostname;
	private String privateIP;
	private String publicIP;
	private String port;
	private String instanceID;
	private RunningTask commandTask;

	public Commands getCommands() {
		return commands;
	}

	protected void setCommands(Commands commands) {
		this.commands = commands;
	}

	public TaskRecord getTask() {
		return task;
	}

	public void setTask(TaskRecord task) {
		this.task = task;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getPublicIP() {
		return publicIP;
	}

	public void setPublicIP(String publicIP) {
		this.publicIP = publicIP;
	}

	public String getPrivateIP() {
		return privateIP;
	}

	public void setPrivateIP(String privateIP) {
		this.privateIP = privateIP;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
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

	public boolean save() {

		APIrestful api = new APIrestful();
		boolean success = false;

		try {
			if (this.ID != null) {
				JSONObject jsonParam = new JSONObject();
				jsonParam.put("name", this.name);
				jsonParam.put("hostname", this.hostname);
				jsonParam.put("instanceid", this.instanceID);
				jsonParam.put("publicip", this.publicIP);
				jsonParam.put("privateip", this.privateIP);
				jsonParam.put("dbusername", this.dbUsername);
				jsonParam.put("dbpassword", this.dbPassword != null ? this.dbPassword : JSONObject.NULL);
				jsonParam.put("repusername", this.repUsername);
				jsonParam.put("reppassword", this.repPassword != null ? this.repPassword : JSONObject.NULL);
				success = api.put("system/" + parentID + "/node/" + ID, jsonParam.toString());
			} else {
				StringBuffer regParam = new StringBuffer();
				regParam.append("name=" + URLEncoder.encode(this.name, "UTF-8"));
				regParam.append("&hostname=" + URLEncoder.encode(this.hostname, "UTF-8"));
				regParam.append("&instanceid=" + URLEncoder.encode(this.instanceID, "UTF-8"));
				regParam.append("&publicip=" + URLEncoder.encode(this.publicIP, "UTF-8"));
				regParam.append("&privateip=" + URLEncoder.encode(this.privateIP, "UTF-8"));
				regParam.append("&dbusername=" + URLEncoder.encode(this.dbUsername, "UTF-8"));
				regParam.append("&dbpassword=" + URLEncoder.encode(this.dbPassword, "UTF-8"));
				regParam.append("&repusername=" + URLEncoder.encode(this.repUsername, "UTF-8"));
				regParam.append("&reppassword=" + URLEncoder.encode(this.repPassword, "UTF-8"));
				success = api.post("system/" + parentID + "/node", regParam.toString());
			}

		} catch (JSONException e) {
			new ErrorDialog(e, "Error encoding API request");
			throw new RuntimeException("Error encoding API request");
		} catch (UnsupportedEncodingException e) {
			new ErrorDialog(e, "Error encoding API request");
			throw new RuntimeException("Error encoding API request");
		}

		if (success) {
			WriteResponse writeResponse = APIrestful.getGson().fromJson(api.getResult(), WriteResponse.class);
			if (writeResponse != null && !writeResponse.getInsertKey().isEmpty()) {
				ID = writeResponse.getInsertKey();
				return true;
			} else if (writeResponse != null && writeResponse.getUpdateCount() > 0) {
				return true;
			}
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
		this.state = "placeholder";
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
				this.updated = nodeInfo.updated;
				this.capacity = nodeInfo.capacity;
				this.commands = nodeInfo.commands;
				this.monitorLatest = nodeInfo.monitorLatest;
				this.task = nodeInfo.task;
				this.hostname = nodeInfo.hostname;
				this.publicIP = nodeInfo.publicIP;
				this.privateIP = nodeInfo.privateIP;
				this.port = nodeInfo.port;
				this.instanceID = nodeInfo.instanceID;
				this.dbUsername = nodeInfo.dbUsername;
				this.dbPassword = nodeInfo.dbPassword;
				this.repUsername = nodeInfo.repUsername;
				this.repPassword = nodeInfo.repPassword;
				this.lastMonitored = nodeInfo.lastMonitored;
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

		return "<h2>Node</h2>" + "<ul>" + "<li><b>State:</b> "
				+ ((this.state == null) ? NOT_AVAILABLE : this.state + " - " + NodeStates.getDescription(this.systemType, this.state)) + "</li>"
				+ "<li><b>ID:</b> " + this.ID + "</li>" + "<li><b>Name:</b> " + this.name + "</li>" + "<li><b>Hostname:</b> " + this.hostname + "</li>"
				+ "<li><b>Public IP:</b> " + this.publicIP + "</li>" + "<li><b>Private IP:</b> " + this.privateIP + "</li>" + "<li><b>Instance ID:</b> "
				+ this.instanceID + "<li><b>DB Username:</b> " + this.dbUsername + "<li><b>DB Password:</b> " + this.dbPassword + "</li>"
				+ "<li><b>Rep Username:</b> " + this.repUsername + "<li><b>Rep Password:</b> " + this.repPassword + "</li>" + "<li><b>Available Commands:</b> "
				+ ((this.commands == null) ? NOT_AVAILABLE : getCommands().getNames().keySet()) + "</li>" + "<li><b>Command running:</b> "
				+ ((this.task == null || !this.task.getState().equals("running")) ? NOT_AVAILABLE : this.task.getCommand()) + "</li>" + "</ul>";

	}

}

// {"node":{"systemid":"1","nodeid":"1","name":"node1","state":"joined","updated":"Thu, 26 Sep 2013 13:21:30 +0000","hostname":"","publicip":"","privateip":"10.0.1.1","port":"0","instanceid":"","dbusername":"","dbpassword":"","repusername":"","reppassword":"","commands":[{"command":"stop","description":"Stop Node when Joined","steps":"stop"},{"command":"restart","description":"Restart Node when Joined","steps":"stop,start"},{"command":"isolate","description":"Take Joined Node out of Replication","steps":"isolate"},{"command":"recover","description":"Recover Joined Node","steps":"recover"},{"command":"backup","description":"Backup Joined Node","steps":"isolate,backup,recover"}],"monitorlatest":{"connections":"11","traffic":null,"availability":null,"capacity":null,"hoststate":null,"nodestate":null,"clustersize":null,"reppaused":null,"parallelism":null,"recvqueue":null,"flowcontrol":null,"sendqueue":null},"task":{"taskid":"1","systemid":"1","nodeid":"1","privateip":"10.0.1.1","username":"admin","command":"restart","parameters":"","steps":"stop,start","started":"Thu, 26 Sep 2013 13:29:16 +0000","pid":"2360","updated":"Thu, 26 Sep 2013 13:29:16 +0000","completed":"Thu, 26 Sep 2013 13:29:16 +0000","stepindex":"1","state":"done"}},"warnings":["Configuration at \/etc\/skysqlmgr\/api.ini does not specify a logging directory","Caching directory \/usr\/local\/skysql\/cache\/api is not writeable, cannot write cache, please check existence, permissions, SELinux"]}

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
			nodeInfo.setUpdated(((element = jsonObject.get("updated")) == null || element.isJsonNull()) ? null : element.getAsString());
			nodeInfo.setHostname(((element = jsonObject.get("hostname")) == null || element.isJsonNull()) ? null : element.getAsString());
			nodeInfo.setPublicIP(((element = jsonObject.get("publicip")) == null || element.isJsonNull()) ? null : element.getAsString());
			nodeInfo.setPrivateIP(((element = jsonObject.get("privateip")) == null || element.isJsonNull()) ? null : element.getAsString());
			nodeInfo.setPort(((element = jsonObject.get("port")) == null || element.isJsonNull()) ? null : element.getAsString());
			nodeInfo.setInstanceID(((element = jsonObject.get("instanceid")) == null || element.isJsonNull()) ? null : element.getAsString());
			nodeInfo.setDBUsername(((element = jsonObject.get("dbusername")) == null || element.isJsonNull()) ? null : element.getAsString());
			nodeInfo.setDBPassword(((element = jsonObject.get("dbpassword")) == null || element.isJsonNull()) ? null : element.getAsString());
			nodeInfo.setRepUsername(((element = jsonObject.get("repusername")) == null || element.isJsonNull()) ? null : element.getAsString());
			nodeInfo.setRepPassword(((element = jsonObject.get("reppassword")) == null || element.isJsonNull()) ? null : element.getAsString());
			nodeInfo.setLastMonitored(((element = jsonObject.get("lastmonitored")) == null || element.isJsonNull()) ? null : element.getAsString());

			if ((element = jsonObject.get("commands")) != null && !element.isJsonNull()) {
				Commands commands = APIrestful.getGson().fromJson("{\"commands\":" + element.toString() + "}", Commands.class);
				nodeInfo.setCommands(commands);
			}

			if ((element = jsonObject.get("monitorlatest")) != null && !element.isJsonNull()) {
				MonitorLatest monitorLatest = APIrestful.getGson().fromJson(element.toString(), MonitorLatest.class);
				nodeInfo.setMonitorLatest(monitorLatest);
			}

			if ((element = jsonObject.get("task")) != null && !element.isJsonNull() && !element.getAsJsonObject().entrySet().isEmpty()) {
				TaskInfo taskInfo = APIrestful.getGson().fromJson("{\"task\":" + element.toString() + "}", TaskInfo.class);
				TaskRecord taskRecord = taskInfo.getTasksList().get(0);
				nodeInfo.setTask(taskRecord);
			}

		}
		return nodeInfo;
	}
}
