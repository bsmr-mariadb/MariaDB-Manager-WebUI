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

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class WriteResponse {

	private int updateCount;
	private int deleteCount;
	private String insertKey;

	public int getUpdateCount() {
		return updateCount;
	}

	protected void setUpdateCount(int updateCount) {
		this.updateCount = updateCount;
	}

	public int getDeleteCount() {
		return deleteCount;
	}

	protected void setDeleteCount(int deleteCount) {
		this.deleteCount = deleteCount;
	}

	public String getInsertKey() {
		return insertKey;
	}

	protected void setInsertKey(String insertKey) {
		this.insertKey = insertKey;
	}

	protected WriteResponse() {
	}

}

class ResponseDeserializer implements JsonDeserializer<WriteResponse> {
	public WriteResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

		WriteResponse writeResponse = new WriteResponse();

		JsonElement jsonElement = json.getAsJsonObject().get("deletecount");
		if (jsonElement != null && !jsonElement.isJsonNull()) {
			writeResponse.setDeleteCount(jsonElement.getAsInt());
		}

		jsonElement = json.getAsJsonObject().get("updatecount");
		if (jsonElement != null && !jsonElement.isJsonNull()) {
			writeResponse.setUpdateCount(jsonElement.getAsInt());
		}

		jsonElement = json.getAsJsonObject().get("insertkey");
		if (jsonElement != null && !jsonElement.isJsonNull()) {
			writeResponse.setInsertKey(jsonElement.getAsString());
		}

		return writeResponse;
	}
}
