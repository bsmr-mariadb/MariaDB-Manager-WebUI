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

import java.util.Arrays;
import java.util.LinkedHashMap;

public class SystemRecord extends ClusterComponent {

	public static String[] systemTypes() {
		String[] array = { "aws", "galera" };
		return array;
	}

	private static final String NOT_AVAILABLE = "n/a";

	private String systemType;
	private String startDate;
	private String lastAccess;
	private String[] nodes;
	private LinkedHashMap<String, String> properties;
	private String lastBackup;

	public String getSystemType() {
		return systemType;
	}

	public void setSystemType(String systemType) {
		this.systemType = systemType;
	}

	public String getStartDate() {
		return startDate;
	}

	protected void setStartDate(String startDate) {
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

	protected void setNodes(String[] nodes) {
		this.nodes = nodes;
	}

	public LinkedHashMap<String, String> getProperties() {
		return properties;
	}

	protected void setProperties(LinkedHashMap<String, String> properties) {
		this.properties = properties;
	}

	public String getLastBackup() {
		return lastBackup;
	}

	public void setLastBackup(String lastBackup) {
		this.lastBackup = lastBackup;
	}

	public SystemRecord() {
		this.type = ClusterComponent.CCType.system;
	}

	public SystemRecord(String ID, String systemType, String name, String health, String connections, String packets, String startDate, String lastAccess,
			String[] nodes, String lastBackup, LinkedHashMap<String, String> properties) {
		this.type = ClusterComponent.CCType.system;
		this.ID = ID;
		this.systemType = systemType;
		this.name = name;
		this.startDate = startDate;
		this.lastAccess = lastAccess;
		this.nodes = nodes;
		this.lastBackup = lastBackup;
		this.properties = properties;
		this.health = health;
		this.connections = connections;
		this.packets = packets;
	}

	public String ToolTip() {

		return "<h2>System</h2>" + "<ul>" + "<li><b>ID:</b> " + this.ID + "</li>" + "<li><b>Type:</b> " + this.systemType + "</li>" + "<li><b>Name:</b> "
				+ this.name + "</li>" + "<li><b>Nodes:</b> " + ((this.nodes == null) ? NOT_AVAILABLE : Arrays.toString(this.nodes)) + "</li>"
				+ "<li><b>Start Date:</b> " + ((this.startDate == null) ? NOT_AVAILABLE : this.startDate) + "</li>" + "<li><b>Last Access:</b> "
				+ ((this.lastAccess == null) ? NOT_AVAILABLE : this.lastAccess) + "</li>" + "<li><b>Last Backup:</b> "
				+ ((this.lastBackup == null) ? NOT_AVAILABLE : this.lastBackup) + "</li>" + "</ul>";

	}

}