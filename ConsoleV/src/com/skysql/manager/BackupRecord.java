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

package com.skysql.manager;

public class BackupRecord {

	private String ID;
	private String state;
	private String started;
	private String updated;
	private String level;
	private String node;
	private String size;
	private String storage;
	private String restored;
	private String log;
	private String parent;

	public String getID() {
		return ID;
	}

	public String getState() {
		return state;
	}

	public String getStarted() {
		return started;
	}

	public String getUpdated() {
		return updated;
	}

	public String getLevel() {
		return level;
	}

	public String getNode() {
		return node;
	}

	public String getSize() {
		return size;
	}

	public String getStorage() {
		return storage;
	}

	public String getRestored() {
		return restored;
	}

	public String getLog() {
		return log;
	}

	public String getParent() {
		return parent;
	}

	public BackupRecord() {

	}

	public BackupRecord(String ID, String state, String started, String updated, String level, String node, String size, String storage, String restored,
			String log, String parent) {
		this.ID = ID;
		this.state = state;
		this.started = started;
		this.updated = updated;
		this.level = level;
		this.node = node;
		this.size = size;
		this.storage = storage;
		this.restored = restored;
		this.log = log;
		this.parent = parent;
	}
}