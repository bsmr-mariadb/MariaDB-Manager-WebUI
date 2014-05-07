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

import com.skysql.java.Configuration;
import com.skysql.java.Configuration.DEFAULT_SECTION;

/**
 * The Class AppData. It loads the application initial configuration, consisting of the API uri and keys.
 */
public class AppData {

	public static String appID;
	public static String verbose;

	private static AppData appData;

	/**
	 * New instance.
	 *
	 * @return the app data
	 */
	public static AppData newInstance() {
		appData = new AppData();
		Configuration config = new Configuration();
		appID = config.getConfig(DEFAULT_SECTION.WEBUI).get("apikeyid");
		verbose = config.getConfig(DEFAULT_SECTION.WEBUI).get("verbose");
		appData.setApiKey(config.getConfig(DEFAULT_SECTION.APIKEYS).get(appID));
		appData.setApiURI(config.getConfig(DEFAULT_SECTION.APIHOST).get("uri"));
		if (appData.apiURI != null) {
			return appData;
		} else {
			return null;
		}
	}

	private String apiURI;
	private String apiKey;

	/**
	 * Gets the app ID.
	 *
	 * @return the app ID
	 */
	public String getAppID() {
		return appID;
	}

	/**
	 * Gets the api uri.
	 *
	 * @return the api uri
	 */
	public String getApiURI() {
		return apiURI;
	}

	/**
	 * Sets the api uri.
	 *
	 * @param apiURI the new api uri
	 */
	protected void setApiURI(String apiURI) {
		this.apiURI = apiURI;
	}

	/**
	 * Gets the api key.
	 *
	 * @return the api key
	 */
	public String getApiKey() {
		return apiKey;
	}

	/**
	 * Sets the api key.
	 *
	 * @param apiKey the api key
	 */
	protected void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	/**
	 * The Class Debug is used to toggle extra debugging info.
	 */
	public final class Debug {
		//set to false to allow compiler to identify and eliminate unreachable code
		public static final boolean ON = false;
	}

}
