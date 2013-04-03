package com.skysql.consolev.api;

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
