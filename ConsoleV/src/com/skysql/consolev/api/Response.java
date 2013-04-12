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

public class Response {

	private String response;

	public String getResponse() {
		return response;
	}

	protected void setResponse(String response) {
		this.response = response;
	}

	protected Response() {
	}

}

class ResponseDeserializer implements JsonDeserializer<Response> {
	public Response deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

		Response response = new Response();

		JsonElement jsonElement = json.getAsJsonObject().get("result");
		if (jsonElement == null || jsonElement.isJsonNull()) {
			response.setResponse(null);
		} else {
			String value = jsonElement.getAsString();
			response.setResponse(value);
		}

		return response;
	}

}
