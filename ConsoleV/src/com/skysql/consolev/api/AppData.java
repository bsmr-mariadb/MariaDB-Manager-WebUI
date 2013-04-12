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
			gsonBuilder.registerTypeAdapter(Backups.class, new BackupsDeserializer());
			gsonBuilder.registerTypeAdapter(BackupCommands.class, new BackupCommandsDeserializer());
			gsonBuilder.registerTypeAdapter(BackupStates.class, new BackupStatesDeserializer());
			gsonBuilder.registerTypeAdapter(Commands.class, new CommandsDeserializer());
			gsonBuilder.registerTypeAdapter(CommandStates.class, new CommandStatesDeserializer());
			gsonBuilder.registerTypeAdapter(CommandSteps.class, new CommandStepsDeserializer());
			gsonBuilder.registerTypeAdapter(Monitors.class, new MonitorsDeserializer());
			gsonBuilder.registerTypeAdapter(MonitorData.class, new MonitorDataDeserializer());
			gsonBuilder.registerTypeAdapter(MonitorData2.class, new MonitorDataDeserializer2());
			gsonBuilder.registerTypeAdapter(MonitorData3.class, new MonitorDataDeserializer3());
			gsonBuilder.registerTypeAdapter(NodeInfo.class, new NodeInfoDeserializer());
			gsonBuilder.registerTypeAdapter(NodeStates.class, new NodeStatesDeserializer());
			gsonBuilder.registerTypeAdapter(Response.class, new ResponseDeserializer());
			gsonBuilder.registerTypeAdapter(RestfulResponse.class, new RestfulResponseDeserializer());
			gsonBuilder.registerTypeAdapter(RunSQL.class, new RunSQLDeserializer());
			gsonBuilder.registerTypeAdapter(SettingsValues.class, new SettingsValuesDeserializer());
			gsonBuilder.registerTypeAdapter(Steps.class, new StepsDeserializer());
			gsonBuilder.registerTypeAdapter(SystemInfo.class, new SystemInfoDeserializer());
			gsonBuilder.registerTypeAdapter(TaskInfo.class, new TaskInfoDeserializer());
			gsonBuilder.registerTypeAdapter(TaskRun.class, new TaskRunDeserializer());
			gsonBuilder.registerTypeAdapter(UserInfo.class, new UserInfoDeserializer());
			gsonBuilder.registerTypeAdapter(UserObject.class, new UserObjectDeserializer());

			gson = gsonBuilder.create();
		}
		return gson;
	}

	public static final String APPLICATION_MODE_PRODUCTION = "PRODUCTION";
	public static final String APPLICATION_MODE_DEBUG = "DEBUG";
	public static final String APPLICATION_MODE = APPLICATION_MODE_PRODUCTION;

}
