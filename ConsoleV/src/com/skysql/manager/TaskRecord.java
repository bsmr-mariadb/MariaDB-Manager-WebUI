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
 * The Class TaskRecord.
 */
public class TaskRecord {

	/** The id. */
	private String id;

	/** The node. */
	private String node;

	/** The command. */
	private String command;

	/** The params. */
	private String params;

	/** The steps. */
	private String steps;

	/** The pid. */
	private String pid;

	/** The private ip. */
	private String privateIP;

	/** The index. */
	private String index;

	/** The state. */
	private String state;

	/** The finished. */
	private String finished;

	/** The user id. */
	private String userID;

	/** The start. */
	private String start;

	/** The end. */
	private String end;

	/** The error. */
	private String error;

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getID() {
		return id;
	}

	/**
	 * Gets the node.
	 *
	 * @return the node
	 */
	public String getNode() {
		return node;
	}

	/**
	 * Gets the command.
	 *
	 * @return the command
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * Gets the params.
	 *
	 * @return the params
	 */
	public String getParams() {
		return params;
	}

	/**
	 * Gets the steps.
	 *
	 * @return the steps
	 */
	public String getSteps() {
		return steps;
	}

	/**
	 * Gets the pid.
	 *
	 * @return the pid
	 */
	public String getPID() {
		return pid;
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
	 * Gets the index.
	 *
	 * @return the index
	 */
	public String getIndex() {
		return index;
	}

	/**
	 * Gets the state.
	 *
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	/**
	 * Gets the finished.
	 *
	 * @return the finished
	 */
	public String getFinished() {
		return finished;
	}

	/**
	 * Gets the user id.
	 *
	 * @return the user id
	 */
	public String getUserID() {
		return userID;
	}

	/**
	 * Gets the start.
	 *
	 * @return the start
	 */
	public String getStart() {
		return start;
	}

	/**
	 * Gets the end.
	 *
	 * @return the end
	 */
	public String getEnd() {
		return end;
	}

	/**
	 * Gets the error.
	 *
	 * @return the error
	 */
	public String getError() {
		return error;
	}

	/**
	 * Instantiates a new task record.
	 *
	 * @param id the id
	 * @param node the node
	 * @param command the command
	 * @param params the params
	 * @param steps the steps
	 * @param pid the pid
	 * @param privateIP the private ip
	 * @param index the index
	 * @param state the state
	 * @param finished the finished
	 * @param userID the user id
	 * @param start the start
	 * @param end the end
	 * @param error the error
	 */
	public TaskRecord(String id, String node, String command, String params, String steps, String pid, String privateIP, String index, String state,
			String finished, String userID, String start, String end, String error) {
		this.id = id;
		this.node = node;
		this.command = command;
		this.params = params;
		this.steps = steps;
		this.pid = pid;
		this.privateIP = privateIP;
		this.index = index;
		this.state = state;
		this.finished = finished;
		this.userID = userID;
		this.start = start;
		this.end = end;
		this.error = error;
	}

}
