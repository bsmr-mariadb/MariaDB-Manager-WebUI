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

import java.lang.reflect.Type;
import java.util.LinkedHashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.skysql.manager.BackupRecord;
import com.skysql.manager.ui.ErrorDialog;

public class Backups {

	LinkedHashMap<String, BackupRecord> backupsList;

	public LinkedHashMap<String, BackupRecord> getBackupsList() {
		return backupsList;
	}

	public void setBackupsList(LinkedHashMap<String, BackupRecord> backupsList) {
		this.backupsList = backupsList;
	}

	public LinkedHashMap<String, BackupRecord> getBackupsForNode(String nodeID) {
		LinkedHashMap<String, BackupRecord> backupsForNode = new LinkedHashMap();

		for (String key : backupsList.keySet()) {
			BackupRecord record = backupsList.get(key);
			if (record.getNode().equals(nodeID)) {
				backupsForNode.put(key, record);
			}
		}
		return backupsForNode;
	}

	public Backups() {

	}

	public Backups(String system, String date) {

		APIrestful api = new APIrestful();
		// TODO: incorporate or eliminate date parameter
		if (api.get("system/" + system + "/backup")) {
			try {
				Backups backups = APIrestful.getGson().fromJson(api.getResult(), Backups.class);
				this.backupsList = backups.backupsList;
			} catch (NullPointerException e) {
				new ErrorDialog(e, "API did not return expected result for:" + api.errorString());
				throw new RuntimeException("API response");
			} catch (JsonParseException e) {
				new ErrorDialog(e, "JSON parse error in API results for:" + api.errorString());
				throw new RuntimeException("API response");
			}
		}
	}

}

// {"total":"0","backups":[],"warnings":["Caching directory \/usr\/local\/skysql\/cache\/api is not writeable, cannot write cache, please check existence, permissions, SELinux"]}
// {"total":"1","backups":[{"systemid":"1","backupid":"1","nodeid":"1","level":"1","parentid":null,"state":"5","started":"Tue, 20 Aug 2013 08:00:00 +0000","updated":"Tue, 20 Aug 2013 08:05:00 +0000","restored":null,"size":"10000","storage":"storage","binlog":"binlog","log":null}],"warnings":["Caching directory \/usr\/local\/skysql\/cache\/api is not writeable, cannot write cache, please check existence, permissions, SELinux"]}

class BackupsDeserializer implements JsonDeserializer<Backups> {
	public Backups deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException, NullPointerException {

		Backups backups = new Backups();

		JsonElement jsonElement = json.getAsJsonObject().get("backups");
		if (jsonElement.isJsonNull()) {
			backups.setBackupsList(null);
		} else {

			JsonArray array = jsonElement.getAsJsonArray();
			int length = array.size();

			LinkedHashMap<String, BackupRecord> backupsList = new LinkedHashMap<String, BackupRecord>(length);
			for (int i = 0; i < length; i++) {
				JsonObject backupJson = array.get(i).getAsJsonObject();
				JsonElement element;
				String id = (element = backupJson.get("backupid")).isJsonNull() ? null : element.getAsString();
				String node = (element = backupJson.get("nodeid")).isJsonNull() ? null : element.getAsString();
				String level = (element = backupJson.get("level")).isJsonNull() ? null : element.getAsString();
				String parent = (element = backupJson.get("parentid")).isJsonNull() ? null : element.getAsString();
				String status = (element = backupJson.get("state")).isJsonNull() ? null : element.getAsString();
				String started = (element = backupJson.get("started")).isJsonNull() ? null : element.getAsString();

				String updated = (element = backupJson.get("updated")).isJsonNull() ? null : element.getAsString();
				String restored = (element = backupJson.get("restored")).isJsonNull() ? null : element.getAsString();
				String size = (element = backupJson.get("size")).isJsonNull() ? null : element.getAsString();
				String storage = (element = backupJson.get("storage")).isJsonNull() ? null : element.getAsString();
				String log = (element = backupJson.get("log")).isJsonNull() ? null : element.getAsString();
				BackupRecord backupRecord = new BackupRecord(id, status, started, updated, level, node, size, storage, restored, log, parent);
				backupsList.put(id, backupRecord);
			}
			backups.setBackupsList(backupsList);
		}
		return backups;

	}

}
