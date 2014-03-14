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

/**
 * The Class BackupRecord stores information about a backup.
 */
public class BackupRecord {

	private String ID;
	private String state;
	private String started;
	private String completed;
	private String level;
	private String node;
	private String size;
	private String storage;
	private String restored;
	private String log;
	private String parent;

	/**
	 * Gets the backup id.
	 *
	 * @return the id
	 */
	public String getID() {
		return ID;
	}

	/**
	 * Gets the backup state.
	 *
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	/**
	 * Gets the time the backup started.
	 *
	 * @return the time started
	 */
	public String getStarted() {
		return started;
	}

	/**
	 * Gets the time the backup was completed
	 *
	 * @return the time completed
	 */
	public String getCompleted() {
		return completed;
	}

	/**
	 * Gets the backup level.
	 *
	 * @return the level
	 */
	public String getLevel() {
		return level;
	}

	/**
	 * Gets the backup source node.
	 *
	 * @return the node
	 */
	public String getNode() {
		return node;
	}

	/**
	 * Gets the backup size.
	 *
	 * @return the size
	 */
	public String getSize() {
		return size;
	}

	/**
	 * Gets the storage used for the backup.
	 *
	 * @return the storage
	 */
	public String getStorage() {
		return storage;
	}

	/**
	 * Gets the last time the backup was restored.
	 *
	 * @return the time restored
	 */
	public String getRestored() {
		return restored;
	}

	/**
	 * Gets the backup log.
	 *
	 * @return the log
	 */
	public String getLog() {
		return log;
	}

	/**
	 * Gets the backup parent.
	 *
	 * @return the parent
	 */
	public String getParent() {
		return parent;
	}

	/**
	 * Instantiates a new backup record.
	 */
	public BackupRecord() {

	}

	/**
	 * Instantiates a new backup record.
	 *
	 * @param ID the backup id
	 * @param state the backup state
	 * @param started the time backup was started
	 * @param completed the time backup was completed
	 * @param level the backup level
	 * @param node the source node
	 * @param size the backup size
	 * @param storage the storage used
	 * @param restored the time last restored
	 * @param log the backup log
	 * @param parent the backup parent
	 */
	public BackupRecord(String ID, String state, String started, String completed, String level, String node, String size, String storage, String restored,
			String log, String parent) {
		this.ID = ID;
		this.state = state;
		this.started = started;
		this.completed = completed;
		this.level = level;
		this.node = node;
		this.size = size;
		this.storage = storage;
		this.restored = restored;
		this.log = log;
		this.parent = parent;
	}
}