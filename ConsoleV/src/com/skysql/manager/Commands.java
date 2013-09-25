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

import java.util.LinkedHashMap;

public class Commands {

	private LinkedHashMap<String, String> descriptions;
	private LinkedHashMap<String, String> names;
	private LinkedHashMap<String, String> steps;

	public LinkedHashMap<String, String> getDescriptions() {
		return descriptions;
	}

	public LinkedHashMap<String, String> getNames() {
		return names;
	}

	public String getSteps(String command) {
		return steps.get(command);
	}

	public void setDescriptions(LinkedHashMap<String, String> pairs) {
		descriptions = pairs;
	}

	public void setNames(LinkedHashMap<String, String> pairs) {
		names = pairs;
	}

	public void setSteps(LinkedHashMap<String, String> pairs) {
		steps = pairs;
	}

}
