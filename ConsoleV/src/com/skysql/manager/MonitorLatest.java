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

import java.util.LinkedHashMap;

/**
 * The Class MonitorLatest, to store what the API returns for the latest monitor values for a node or a system.
 */
public class MonitorLatest {

	/** The data is map of Monitor keys and values. */
	private LinkedHashMap<String, String> data;

	/**
	 * Gets the data.
	 *
	 * @return the data
	 */
	public LinkedHashMap<String, String> getData() {
		return data;
	}

	/**
	 * Sets the data.
	 *
	 * @param data the data
	 */
	public void setData(LinkedHashMap<String, String> data) {
		this.data = data;
	}

}
