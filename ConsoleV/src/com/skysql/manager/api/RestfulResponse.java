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

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class RestfulResponse {

	private boolean success;
	private String result;
	private String errors;

	public boolean isSuccess() {
		return success;
	}

	public String getResult() {
		return result;
	}

	public String getErrors() {
		return errors;
	}

	protected void setSuccess(boolean success) {
		this.success = success;
	}

	protected void setResult(String result) {
		this.result = result;
	}

	protected void setErrors(String errors) {
		this.errors = errors;
	}

	protected RestfulResponse() {
	}

}

class RestfulResponseDeserializer implements JsonDeserializer<RestfulResponse> {
	public RestfulResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

		RestfulResponse response = new RestfulResponse();

		JsonObject jsonObject = json.getAsJsonObject();

		if (jsonObject.has("result")) {
			response.setResult(jsonObject.get("result").toString());
			response.setSuccess(true);
		} else if (jsonObject.has("errors")) {
			response.setErrors("API Errors: " + jsonObject.get("errors").toString());
			response.setSuccess(false);
		} else {
			response.setSuccess(false);
		}

		return response;
	}

}
