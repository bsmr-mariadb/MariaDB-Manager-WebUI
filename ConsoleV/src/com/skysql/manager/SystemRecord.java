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

package com.skysql.manager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.skysql.manager.api.APIrestful;
import com.skysql.manager.api.WriteResponse;
import com.skysql.manager.ui.ErrorDialog;
import com.vaadin.server.VaadinSession;

public class SystemRecord extends ClusterComponent {

	private static final String NOT_AVAILABLE = "n/a";

	private String startDate;
	private String lastAccess;
	private String[] nodes;
	private LinkedHashMap<String, String> properties;
	private String lastBackup;

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getLastAccess() {
		return lastAccess;
	}

	public void setLastAccess(String lastAccess) {
		this.lastAccess = lastAccess;
	}

	public String[] getNodes() {
		return nodes;
	}

	public void setNodes(String[] nodes) {
		this.nodes = nodes;
	}

	public LinkedHashMap<String, String> getProperties() {
		return properties;
	}

	public void setProperties(LinkedHashMap<String, String> properties) {
		this.properties = properties;
	}

	public String getLastBackup() {
		return lastBackup;
	}

	public void setLastBackup(String lastBackup) {
		this.lastBackup = lastBackup;
	}

	public SystemRecord(String parentID) {
		this.type = ClusterComponent.CCType.system;
		this.parentID = parentID;
	}

	public String ToolTip() {
		DateConversion dateConversion = VaadinSession.getCurrent().getAttribute(DateConversion.class);

		return "<h2>System - (Double-click to open)</h2>" + "<ul>" + "<li><b>ID:</b> " + this.ID + "</li>" + "<li><b>Type:</b> " + this.systemType + "</li>"
				+ "<li><b>Name:</b> " + this.name + "</li>" + "</li>" + "<li><b>State:</b> " + ((this.state == null) ? NOT_AVAILABLE : this.state) + "</li>"
				+ "<li><b>Nodes:</b> " + ((this.nodes == null) ? NOT_AVAILABLE : Arrays.toString(this.nodes)) + "</li>" + "<li><b>Start Date:</b> "
				+ ((this.startDate == null) ? NOT_AVAILABLE : dateConversion.adjust(this.startDate)) + "</li>" + "<li><b>Last Access:</b> "
				+ ((this.lastAccess == null) ? NOT_AVAILABLE : dateConversion.adjust(this.lastAccess)) + "</li>" + "<li><b>Last Backup:</b> "
				+ ((this.lastBackup == null) ? NOT_AVAILABLE : dateConversion.adjust(this.lastBackup)) + "</li>" + "</ul>";
	}

	public boolean save() {

		APIrestful api = new APIrestful();
		boolean success = false;

		try {
			if (getID() != null) {
				JSONObject jsonParam = new JSONObject();
				jsonParam.put("name", getName());
				jsonParam.put("systemtype", getSystemType());
				jsonParam.put("dbusername", this.dbUsername);
				jsonParam.put("dbpassword", this.dbPassword != null ? this.dbPassword : JSONObject.NULL);
				jsonParam.put("repusername", this.repUsername);
				jsonParam.put("reppassword", this.repPassword != null ? this.repPassword : JSONObject.NULL);
				success = api.put("system/" + getID(), jsonParam.toString());
			} else {
				StringBuffer regParam = new StringBuffer();
				regParam.append("name=" + URLEncoder.encode(getName(), "UTF-8"));
				regParam.append("&systemtype=" + URLEncoder.encode(getSystemType(), "UTF-8"));
				regParam.append("&dbusername=" + URLEncoder.encode(this.dbUsername, "UTF-8"));
				regParam.append("&dbpassword=" + URLEncoder.encode(this.dbPassword, "UTF-8"));
				regParam.append("&repusername=" + URLEncoder.encode(this.repUsername, "UTF-8"));
				regParam.append("&reppassword=" + URLEncoder.encode(this.repPassword, "UTF-8"));
				success = api.post("system", regParam.toString());
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
			if (writeResponse != null && getID() == null && !writeResponse.getInsertKey().isEmpty()) {
				setID(writeResponse.getInsertKey());
				return true;
			} else if (writeResponse != null && getID() != null && writeResponse.getUpdateCount() > 0) {
				return true;
			}
		}

		return false;

	}

	public boolean delete() {

		APIrestful api = new APIrestful();
		if (api.delete("system/" + ID)) {
			WriteResponse writeResponse = APIrestful.getGson().fromJson(api.getResult(), WriteResponse.class);
			if (writeResponse != null && writeResponse.getDeleteCount() > 0) {
				return true;
			}
		}
		return false;

	}

}