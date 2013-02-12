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
	  public Response deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
	      throws JsonParseException
	  {
		
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
