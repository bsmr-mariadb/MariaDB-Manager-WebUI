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
 * The Class Commands. Maps a command to its description and set of steps. 
 */
public class Commands {

	public enum Command {
		backup, connect, isolate, probe, promote, provision, rejoin, restart, restore, start, stop;
	}

	private LinkedHashMap<String, String> descriptions;
	private LinkedHashMap<String, String> names;
	private LinkedHashMap<String, String> steps;

	/**
	 * Gets the map of descriptions.
	 *
	 * @return the descriptions
	 */
	public LinkedHashMap<String, String> getDescriptions() {
		return descriptions;
	}

	/**
	 * Gets the map of names.
	 *
	 * @return the names
	 */
	public LinkedHashMap<String, String> getNames() {
		return names;
	}

	/**
	 * Gets the steps.
	 *
	 * @param command the command
	 * @return the steps
	 */
	public String getSteps(String command) {
		return steps.get(command);
	}

	/**
	 * Sets the descriptions.
	 *
	 * @param descriptions the descriptions
	 */
	public void setDescriptions(LinkedHashMap<String, String> descriptions) {
		this.descriptions = descriptions;
	}

	/**
	 * Sets the names.
	 *
	 * @param names the names
	 */
	public void setNames(LinkedHashMap<String, String> names) {
		this.names = names;
	}

	/**
	 * Sets the steps.
	 *
	 * @param pairs the pairs
	 */
	public void setSteps(LinkedHashMap<String, String> steps) {
		this.steps = steps;
	}

}
