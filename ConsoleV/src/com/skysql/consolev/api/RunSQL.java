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

package com.skysql.consolev.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class RunSQL {

	private boolean success;
	private String result;
	private String errors;

	public boolean getSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getResult() {
		return result;
	}

	public String getErrors() {
		return errors;
	}

	public RunSQL() {

	}

	public RunSQL(String SQL, String systemID, String nodeID) {
		APIrestful api = new APIrestful();
		try {
			success = api.get("runsql", "?systemid=" + systemID + "&nodeid=" + nodeID + "&sql=" + URLEncoder.encode(SQL, "UTF-8"));
			result = api.getResult();
			errors = api.getErrors();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
