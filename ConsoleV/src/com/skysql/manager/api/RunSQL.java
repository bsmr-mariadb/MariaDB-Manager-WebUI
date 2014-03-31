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
import java.net.URLEncoder;

import com.skysql.manager.ui.ErrorDialog;

/**
 * The Class RunSQL.
 */
public class RunSQL {

	private boolean success;
	private String result;
	private String errors;

	/**
	 * Gets the success.
	 *
	 * @return the success
	 */
	public boolean getSuccess() {
		return success;
	}

	/**
	 * Sets the success.
	 *
	 * @param success the new success
	 */
	public void setSuccess(boolean success) {
		this.success = success;
	}

	/**
	 * Gets the result.
	 *
	 * @return the result
	 */
	public String getResult() {
		return result;
	}

	/**
	 * Gets the errors.
	 *
	 * @return the errors
	 */
	public String getErrors() {
		return errors;
	}

	/**
	 * Instantiates a new class.
	 */
	public RunSQL() {

	}

	/**
	 * Instantiates a new class and asks the API to validate the sql.
	 *
	 * @param SQL the sql
	 * @param systemID the system id
	 * @param nodeID the node id
	 */
	public RunSQL(String SQL, String systemID, String nodeID) {
		APIrestful api = new APIrestful();
		try {
			success = api.get("runsql", "?systemid=" + systemID + "&nodeid=" + nodeID + "&sql=" + URLEncoder.encode(SQL, "UTF-8"));
			result = api.getResult();
			errors = api.getErrors();
		} catch (UnsupportedEncodingException e) {
			new ErrorDialog(e, "Error encoding API request");
			throw new RuntimeException("Error encoding API request");
		}
	}
}
