/*
 * This file is distributed as part of the MariaDB Manager.  It is free
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
 * Copyright 2012-2014 SkySQL Corporation Ab
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

/**
 * The Class NodeInfo.
 */
public class NodeInfo extends ClusterComponent {

	private static final String NOT_AVAILABLE = "n/a";

	/** The valid commands for this node. */
	private Commands commands;

	/** The last task running or run on this node. */
	private TaskRecord task;

	private String hostname;
	private String privateIP;
	private String publicIP;
	private String port;
	private String instanceID;

	/** The GUI's command task associated with this node */
	private RunningTask commandTask;

	/**
	 * Instantiates a new node info.
	 */
	public NodeInfo() {
	}

	/**
	 * Instantiates a new node info.
	 *
	 * @param systemID the system id
	 * @param systemType the system type
	 */
	public NodeInfo(String systemID, String systemType) {
		this.type = CCType.node;
		this.parentID = systemID;
		this.systemType = systemType;
		this.state = "placeholder";
	}

	/**
	 * Instantiates a new node info.
	 *
	 * @param systemID the system id
	 * @param systemType the system type
	 * @param nodeID the node id
	 */
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

	/**
	 * Gets the commands.
	 *
	 * @return the commands
	 */
	public Commands getCommands() {
		return commands;
	}

	/**
	 * Sets the commands.
	 *
	 * @param commands the new commands
	 */
	protected void setCommands(Commands commands) {
		this.commands = commands;
	}

	/**
	 * Gets the task.
	 *
	 * @return the task
	 */
	public TaskRecord getTask() {
		return task;
	}

	/**
	 * Sets the task.
	 *
	 * @param task the new task
	 */
	public void setTask(TaskRecord task) {
		this.task = task;
	}

	/**
	 * Gets the hostname.
	 *
	 * @return the hostname
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * Sets the hostname.
	 *
	 * @param hostname the new hostname
	 */
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	/**
	 * Gets the public ip.
	 *
	 * @return the public ip
	 */
	public String getPublicIP() {
		return publicIP;
	}

	/**
	 * Sets the public ip.
	 *
	 * @param publicIP the new public ip
	 */
	public void setPublicIP(String publicIP) {
		this.publicIP = publicIP;
	}

	/**
	 * Gets the private ip.
	 *
	 * @return the private ip
	 */
	public String getPrivateIP() {
		return privateIP;
	}

	/**
	 * Sets the private ip.
	 *
	 * @param privateIP the new private ip
	 */
	public void setPrivateIP(String privateIP) {
		this.privateIP = privateIP;
	}

	/**
	 * Gets the port.
	 *
	 * @return the port
	 */
	public String getPort() {
		return port;
	}

	/**
	 * Sets the port.
	 *
	 * @param port the new port
	 */
	public void setPort(String port) {
		this.port = port;
	}

	/**
	 * Gets the instance id.
	 *
	 * @return the instance id
	 */
	public String getInstanceID() {
		return instanceID;
	}

	/**
	 * Sets the instance id.
	 *
	 * @param instanceID the new instance id
	 */
	public void setInstanceID(String instanceID) {
		this.instanceID = instanceID;
	}

	/**
	 * Gets the command task.
	 *
	 * @return the command task
	 */
	public RunningTask getCommandTask() {
		return commandTask;
	}

	/**
	 * Sets the command task.
	 *
	 * @param commandTask the new command task
	 */
	public void setCommandTask(RunningTask commandTask) {
		this.commandTask = commandTask;
	}

	/**
	 * Get updated task record and state associated with node from API.
	 *
	 * @return true, if successful
	 */
	public boolean updateTask() {

		APIrestful api = new APIrestful();
		boolean success = false;

		if (api.get("system/" + this.parentID + "/node/" + ID, "?fields=task,state")) {
			try {
				NodeInfo nodeInfo = APIrestful.getGson().fromJson(api.getResult(), NodeInfo.class);
				this.state = nodeInfo.state;
				this.task = nodeInfo.task;

				return true;

			} catch (NullPointerException e) {
				new ErrorDialog(e, "API did not return expected result for:" + api.errorString());
				throw new RuntimeException("API response");
			} catch (JsonParseException e) {
				new ErrorDialog(e, "JSON parse error in API results for:" + api.errorString());
				throw new RuntimeException("API response");
			}

		}

		return false;

	}

	/**
	 * Get updated list of valid commands associated with node from API.
	 *
	 * @return true, if successful
	 */
	public boolean updateCommands() {

		APIrestful api = new APIrestful();
		boolean success = false;

		if (api.get("system/" + this.parentID + "/node/" + ID, "?fields=commands")) {
			try {
				NodeInfo nodeInfo = APIrestful.getGson().fromJson(api.getResult(), NodeInfo.class);
				this.commands = nodeInfo.commands;

				return true;

			} catch (NullPointerException e) {
				new ErrorDialog(e, "API did not return expected result for:" + api.errorString());
				throw new RuntimeException("API response");
			} catch (JsonParseException e) {
				new ErrorDialog(e, "JSON parse error in API results for:" + api.errorString());
				throw new RuntimeException("API response");
			}

		}

		return false;

	}

	/**
	 * Save node info to API.
	 *
	 * @return true, if successful
	 */
	public boolean save() {

		APIrestful api = new APIrestful();
		boolean success = false;

		try {
			if (ID != null) {
				JSONObject jsonParam = new JSONObject();
				jsonParam.put("name", this.name);
				jsonParam.put("hostname", this.hostname);
				jsonParam.put("instanceid", this.instanceID);
				jsonParam.put("publicip", this.publicIP);
				jsonParam.put("privateip", this.privateIP);
				if (this.dbUsername != null) {
					jsonParam.put("dbusername", this.dbUsername);
				}
				if (this.dbPassword != null) {
					jsonParam.put("dbpassword", this.dbPassword != null ? this.dbPassword : JSONObject.NULL);
				}
				if (this.repUsername != null) {
					jsonParam.put("repusername", this.repUsername);
				}
				if (this.repPassword != null) {
					jsonParam.put("reppassword", this.repPassword != null ? this.repPassword : JSONObject.NULL);
				}
				success = api.put("system/" + parentID + "/node/" + ID, jsonParam.toString());
			} else {
				StringBuffer regParam = new StringBuffer();
				regParam.append("name=" + URLEncoder.encode(this.name, "UTF-8"));
				regParam.append("&hostname=" + URLEncoder.encode(this.hostname, "UTF-8"));
				regParam.append("&instanceid=" + URLEncoder.encode(this.instanceID, "UTF-8"));
				regParam.append("&publicip=" + URLEncoder.encode(this.publicIP, "UTF-8"));
				regParam.append("&privateip=" + URLEncoder.encode(this.privateIP, "UTF-8"));
				if (this.dbUsername != null) {
					regParam.append("&dbusername=" + URLEncoder.encode(this.dbUsername, "UTF-8"));
				}
				if (this.dbPassword != null) {
					regParam.append("&dbpassword=" + URLEncoder.encode(this.dbPassword, "UTF-8"));
				}
				if (this.repUsername != null) {
					regParam.append("&repusername=" + URLEncoder.encode(this.repUsername, "UTF-8"));
				}
				if (this.repPassword != null) {
					regParam.append("&reppassword=" + URLEncoder.encode(this.repPassword, "UTF-8"));
				}
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
			if (writeResponse != null && ID == null && !writeResponse.getInsertKey().isEmpty()) {
				ID = writeResponse.getInsertKey();
				return true;
			} else if (writeResponse != null && ID != null && writeResponse.getUpdateCount() > 0) {
				return true;
			}
		}

		return false;

	}

	/**
	 * Delete node from API.
	 *
	 * @return true, if successful
	 */
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

	/**
	 * Tool tip for node.
	 *
	 * @return the string
	 */
	public String ToolTip() {

		return "<h2>Node</h2>" + "<ul>" + "<li><b>State:</b> "
				+ ((this.state == null) ? NOT_AVAILABLE : this.state + " - " + NodeStates.getDescription(this.systemType, this.state)) + "</li>"
				+ "<li><b>ID:</b> " + this.ID + "</li>" + "<li><b>Name:</b> " + this.name + "</li>" + "<li><b>Hostname:</b> " + this.hostname + "</li>"
				+ "<li><b>Public IP:</b> " + this.publicIP + "</li>" + "<li><b>Private IP:</b> " + this.privateIP + "</li>" + "<li><b>Instance ID:</b> "
				+ this.instanceID + "<li><b>DB Username:</b> " + this.dbUsername + "<li><b>Rep Username:</b> " + this.repUsername
				+ "<li><b>Available Commands:</b> " + ((this.commands == null) ? NOT_AVAILABLE : getCommands().getNames().keySet()) + "</li>"
				+ "<li><b>Command running:</b> " + ((this.task == null || !this.task.getState().equals("running")) ? NOT_AVAILABLE : this.task.getCommand())
				+ "</li>" + "</ul>";

	}

}

// {"node":{"systemid":"1","nodeid":"1","name":"node1","state":"joined","updated":"Thu, 26 Sep 2013 13:21:30 +0000","hostname":"","publicip":"","privateip":"10.0.1.1",     "port":"0","instanceid":"","dbusername":"","dbpassword":"","repusername":"","reppassword":"","commands":[{"command":"stop","description":"Stop Node when Joined","steps":"stop"},{"command":"restart","description":"Restart Node when Joined","steps":"stop,start"},{"command":"isolate","description":"Take Joined Node out of Replication","steps":"isolate"},{"command":"recover","description":"Recover Joined Node","steps":"recover"},{"command":"backup","description":"Backup Joined Node","steps":"isolate,backup,recover"}],           "monitorlatest":{"connections":"11","traffic":null,"availability":null,"capacity":null,"hoststate":null,"nodestate":null,"clustersize":null,"reppaused":null,"parallelism":null,"recvqueue":null,"flowcontrol":null,"sendqueue":null},"task":{"taskid":"1","systemid":"1","nodeid":"1","privateip":"10.0.1.1","username":"admin","command":"restart","parameters":"","steps":"stop,start","started":"Thu, 26 Sep 2013 13:29:16 +0000","pid":"2360","updated":"Thu, 26 Sep 2013 13:29:16 +0000","completed":"Thu, 26 Sep 2013 13:29:16 +0000","stepindex":"1","state":"done"}},"warnings":["Configuration at \/etc\/skysqlmgr\/api.ini does not specify a logging directory","Caching directory \/usr\/local\/skysql\/cache\/api is not writeable, cannot write cache, please check existence, permissions, SELinux"]}
// {"node":{"systemid":"1","nodeid":"1","name":"Node1","state":"joined","updated":"Fri, 11 Apr 2014 03:01:28 +0000","hostname":"","publicip":"","privateip":"107.170.78.97","port":"0","instanceid":"","dbusername":"","dbpassword":"","repusername":"","reppassword":"","commands":[{"command":"backup","description":"Backup Joined Node","steps":"isolate,backup,stop,start"},{"command":"restart","description":"Restart Node when Joined","steps":"stop,start"},{"command":"restore","description":"Restore Joined Node","steps":"isolate,restore"},{"command":"stop","description":"Stop Node when Joined","steps":"stop"},{"command":"isolate","description":"Take Joined Node out of Replication","steps":"isolate"}],"monitorlatest":{"nodestate":null,"ping":1,"clustersize":2,"reppaused":"0","recvqueue":"0","flowcontrol":"0","sendqueue":"0","failed_writesets":"0","aborted_transactions":"0","capacity":3,"connections":4,"traffic":1364256,"availability":100,"com_admin":"0","com_assign_keycache":"0","com_alter_db":"0","com_alter_db_upgrade":"0","com_alter_event":"0","com_alter_function":"0","com_alter_procedure":"0","com_alter_server":"0","com_alter_table":"0","com_alter_tablespace":"0","com_analyze":"0","com_begin":"0","com_binlog":"0","com_call_procedure":"0","com_change_db":24,"com_change_master":"0","com_check":"0","com_checksum":"0","com_commit":2801,"com_create_db":33,"com_create_event":"0","com_create_function":"0","com_create_index":"0","com_create_procedure":"0","com_create_server":"0","com_create_table":33,"com_create_trigger":"0","com_create_udf":"0","com_create_user":"0","com_create_view":"0","com_dealloc_sql":"0","com_delete":"0","com_delete_multi":"0","com_do":"0","com_drop_db":34,"com_drop_event":"0","com_drop_function":"0","com_drop_index":"0","com_drop_procedure":"0","com_drop_server":"0","com_drop_table":"0","com_drop_trigger":"0","com_drop_user":"0","com_drop_view":"0","com_empty_query":"0","com_execute_sql":"0","com_flush":"0","com_grant":"0","com_ha_close":"0","com_ha_open":"0","com_ha_read":"0","com_help":"0","com_insert":9895,"com_insert_select":"0","com_install_plugin":"0","com_kill":"0","com_load":"0","com_lock_tables":"0","com_optimize":"0","com_preload_keys":"0","com_prepare_sql":"0","com_purge":"0","com_purge_before_date":"0","com_release_savepoint":"0","com_rename_table":"0","com_rename_user":"0","com_repair":"0","com_replace":"0","com_replace_select":"0","com_reset":"0","com_resignal":"0","com_revoke":"0","com_revoke_all":"0","com_rollback":"0","com_rollback_to_savepoint":"0","com_savepoint":"0","com_select":3969,"com_set_option":"0","com_signal":"0","com_show_authors":"0","com_show_binlog_events":"0","com_show_binlogs":"0","com_show_charsets":"0","com_show_collations":"0","com_show_contributors":"0","com_show_create_db":"0","com_show_create_event":"0","com_show_create_func":"0","com_show_create_proc":"0","com_show_create_table":"0","com_show_create_trigger":"0","com_show_databases":"0","com_show_engine_logs":"0","com_show_engine_mutex":"0","com_show_engine_status":"0","com_show_events":"0","com_show_errors":"0","com_show_fields":"0","com_show_function_status":"0","com_show_grants":"0","com_show_keys":"0","com_show_master_status":"0","com_show_open_tables":"0","com_show_plugins":"0","com_show_privileges":"0","com_show_procedure_status":"0","com_show_processlist":"0","com_show_profile":"0","com_show_profiles":"0","com_show_relaylog_events":"0","com_show_slave_hosts":"0","com_show_slave_status":"0","com_show_status":4,"com_show_storage_engines":"0","com_show_table_status":"0","com_show_tables":"0","com_show_triggers":"0","com_show_variables":2,"com_show_warnings":"0","com_slave_start":"0","com_slave_stop":"0","com_stmt_close":"0","com_stmt_execute":"0","com_stmt_fetch":"0","com_stmt_prepare":"0","com_stmt_reprepare":"0","com_stmt_reset":"0","com_stmt_send_long_data":"0","com_truncate":"0","com_uninstall_plugin":"0","com_unlock_tables":"0","com_update":"0","com_update_multi":"0","com_xa_commit":"0","com_xa_end":"0","com_xa_prepare":"0","com_xa_recover":"0","com_xa_rollback":"0","com_xa_start":"0","innodb_buffer_pool_pages_data":"0","innodb_buffer_pool_pages_dirty":1,"innodb_buffer_pool_pages_flushed":940,"innodb_buffer_pool_pages_free":"0","innodb_buffer_pool_pages_misc":"0","innodb_buffer_pool_pages_total":"0","innodb_buffer_pool_read_ahead":"0","innodb_buffer_pool_read_ahead_evicted":"0","innodb_buffer_pool_read_requests":892000,"innodb_buffer_pool_reads":"0","innodb_buffer_pool_wait_free":"0","innodb_buffer_pool_write_requests":72756,"innodb_buffer_pool_hit_ratio":100,"innodb_data_fsyncs":20611,"innodb_data_pending_fsyncs":"0","innodb_data_pending_reads":"0","innodb_data_pending_writes":"0","innodb_data_read":"0","innodb_data_reads":"0","innodb_data_writes":21519,"innodb_data_written":55398400,"innodb_dblwr_pages_written":940,"innodb_dblwr_writes":40,"innodb_log_waits":"0","innodb_log_write_requests":42533,"innodb_log_writes":19881,"innodb_os_log_fsyncs":19887,"innodb_os_log_pending_fsyncs":"0","innodb_os_log_pending_writes":"0","innodb_os_log_written":24593408,"innodb_pages_created":747,"innodb_pages_read":"0","innodb_pages_written":940,"innodb_row_lock_current_waits":"0","innodb_row_lock_time":"0","innodb_row_lock_time_avg":"0","innodb_row_lock_time_max":"0","innodb_row_lock_waits":"0","innodb_rows_deleted":"0","innodb_rows_inserted":9884,"innodb_rows_read":725632,"innodb_rows_updated":"0","key_blocks_not_flushed":"0","key_blocks_unused":"0","key_blocks_used":"0","key_read_requests":"0","key_reads":"0","key_write_requests":"0","key_writes":"0","open_files":25,"open_streams":"0","open_table_definitions":35,"open_tables":28,"select_full_join":"0","select_full_range_join":"0","select_range":"0","select_range_check":"0","select_scan":3973,"table_locks_immediate":13918,"table_locks_waited":"0","DDL":13864,"DML":66,"read_write_ratio":100},"lastmonitored":"Fri, 11 Apr 2014 03:01:28 +0000","task":{"taskid":"138","systemid":"1","nodeid":"1","privateip":"107.170.78.97","scheduleid":"","username":"admin","command":"start","parameters":"{}","steps":"upgrade,start","started":"Wed, 09 Apr 2014 07:13:38 +0000","pid":"15305","updated":"Wed, 09 Apr 2014 07:15:00 +0000","completed":"Wed, 09 Apr 2014 07:15:00 +0000","stepindex":"0","state":"done","errormessage":"","finished":true}}}

/**
 * The Class NodeInfoDeserializer.
 */
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
