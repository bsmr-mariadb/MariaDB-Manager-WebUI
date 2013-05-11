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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class AppData {

	private static Gson gson;

	public static Gson getGson() {
		if (gson == null) {
			GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.registerTypeAdapter(APIrestful.class, new APIrestfulDeserializer());
			gsonBuilder.registerTypeAdapter(Backups.class, new BackupsDeserializer());
			gsonBuilder.registerTypeAdapter(BackupStates.class, new BackupStatesDeserializer());
			gsonBuilder.registerTypeAdapter(Commands.class, new CommandsDeserializer());
			gsonBuilder.registerTypeAdapter(CommandStates.class, new CommandStatesDeserializer());
			gsonBuilder.registerTypeAdapter(Monitors.class, new MonitorsDeserializer());
			gsonBuilder.registerTypeAdapter(MonitorData.class, new MonitorDataDeserializer());
			gsonBuilder.registerTypeAdapter(MonitorDataLatest.class, new MonitorDataLatestDeserializer());
			gsonBuilder.registerTypeAdapter(MonitorDataRaw.class, new MonitorDataRawDeserializer());
			gsonBuilder.registerTypeAdapter(NodeInfo.class, new NodeInfoDeserializer());
			gsonBuilder.registerTypeAdapter(NodeStates.class, new NodeStatesDeserializer());
			gsonBuilder.registerTypeAdapter(WriteResponse.class, new ResponseDeserializer());
			gsonBuilder.registerTypeAdapter(RestfulResponse.class, new RestfulResponseDeserializer());
			gsonBuilder.registerTypeAdapter(SettingsValues.class, new SettingsValuesDeserializer());
			gsonBuilder.registerTypeAdapter(Steps.class, new StepsDeserializer());
			gsonBuilder.registerTypeAdapter(SystemInfo.class, new SystemInfoDeserializer());
			gsonBuilder.registerTypeAdapter(TaskInfo.class, new TaskInfoDeserializer());
			gsonBuilder.registerTypeAdapter(UserInfo.class, new UserInfoDeserializer());
			gsonBuilder.registerTypeAdapter(UserObject.class, new UserObjectDeserializer());

			gson = gsonBuilder.create();
		}
		return gson;
	}

	public final class Debug {
		//set to false to allow compiler to identify and eliminate unreachable code
		public static final boolean ON = false;
	}

}
