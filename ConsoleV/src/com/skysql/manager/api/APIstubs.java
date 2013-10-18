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

package com.skysql.manager.api;

import java.util.Hashtable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.skysql.manager.AppData.Debug;
import com.skysql.manager.ManagerUI;
import com.skysql.manager.ui.DebugPanel;
import com.skysql.manager.ui.ErrorDialog;
import com.vaadin.server.VaadinSession;

public class APIstubs extends APIrestful {

	private static Gson gson;
	private static APIstubs api;

	private String result;

	public static APIstubs newInstance(String URI, Hashtable<String, String> keys) {
		if (api == null) {
			api = new APIstubs();
		}
		return api;
	}

	public static Gson getGson() {
		if (gson == null) {
			GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.registerTypeAdapter(APIrestful.class, new APIrestfulDeserializer());
			//gsonBuilder.registerTypeAdapter(Schedule.class, new ScheduleDeserializer());

			gson = gsonBuilder.create();
		}
		return gson;
	}

	public String getResult() {
		return result;
	}

	public boolean get(String uri) {

		return call(uri, CallType.GET, null);

	}

	public boolean get(String uri, String value) {

		return call(uri, CallType.GET, value);

	}

	public boolean put(String uri, String value) {

		return call(uri, CallType.PUT, value);

	}

	public boolean post(String uri, String value) {

		return call(uri, CallType.POST, value);

	}

	public boolean delete(String uri) {

		return call(uri, CallType.DELETE, null);

	}

	private boolean call(String uri, CallType type, String value) {

		DebugPanel debugPanel = VaadinSession.getCurrent().getAttribute(DebugPanel.class);

		try {
			String url = "APIstubs/" + uri + ((type == CallType.GET && value != null) ? value : "");
			String call = type + " " + url + (type != CallType.GET && value != null ? " parameters: " + value : "");
			ManagerUI.log("API " + call, debugPanel);

			switch (type) {
			case GET:
				/***
				if (uri.endsWith("scheduled")) {
					result = "{\"scheduled\":[{\"id\":\"2013-09-17 02:00:00\",\"state\":\"scheduled\",\"ical\":\"Full\",\"nodeid\":\"1\",\"taskid\":\"2\"},"
							+ "{\"id\":\"2013-09-18 03:00:00\",\"state\":\"scheduled\",\"ical\":\"Incremental\",\"nodeid\":\"1\",\"taskid\":\"2\"},"
							+ "{\"id\":\"2013-09-19 03:00:00\",\"state\":\"scheduled\",\"ical\":\"Incremental\",\"nodeid\":\"1\",\"taskid\":\"2\"},"
							+ "{\"id\":\"2013-09-20 03:00:00\",\"state\":\"scheduled\",\"ical\":\"Incremental\",\"nodeid\":\"1\",\"taskid\":\"2\"},"
							+ "{\"id\":\"2013-09-21 03:00:00\",\"state\":\"scheduled\",\"ical\":\"Incremental\",\"nodeid\":\"1\",\"taskid\":\"2\"},"
							+ "{\"id\":\"2013-09-22 03:00:00\",\"state\":\"scheduled\",\"ical\":\"Incremental\",\"nodeid\":\"1\",\"taskid\":\"2\"}" + "]}";
				}
				***/
				break;

			case PUT:
			case POST:
				break;

			case DELETE:
				break;
			}

			if (Debug.ON) {
				ManagerUI.log("APIstubs: " + result, debugPanel);
			}

			APIrestful api = getGson().fromJson(result, APIrestful.class);
			return true;

		} catch (Exception e) {
			new ErrorDialog(e, "API stubs");
			throw new RuntimeException("Error in API stubs");
		}
	}
}
