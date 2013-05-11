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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class APIrestful {

	private static String newAPIurl = "http://localhost/consoleAPI/api/";
	private static final String AUTHORIZATION_ID_SKYSQL_API = "1";
	private static final String AUTHORIZATION_CODE_SKYSQL_API = "1f8d9e040e65d7b105538b1ed0231770";
	private static final SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

	private boolean success;
	private String result;
	private String errors;

	private enum CallType {
		GET, PUT, POST, DELETE;
	}

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

		HttpURLConnection httpConnection = null;

		try {
			URL url = new URL(newAPIurl + uri + ((type == CallType.GET && value != null) ? value : ""));
			URLConnection sc = url.openConnection();
			httpConnection = (HttpURLConnection) sc;
			String date = sdf.format(new Date());

			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] mdbytes = md.digest((uri + AUTHORIZATION_CODE_SKYSQL_API + date).getBytes("UTF-8"));
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < mdbytes.length; i++) {
				sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
			}
			sc.setRequestProperty("Authorization", "api-auth-" + AUTHORIZATION_ID_SKYSQL_API + "-" + sb.toString());
			sc.setRequestProperty("Date", date);
			sc.setRequestProperty("Accept", "application/json");
			sc.setRequestProperty("X-skysql-apiversion", "1");

			switch (type) {
			case GET:
				break;

			case PUT:
			case POST:
				httpConnection.setDoOutput(true);
				httpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				httpConnection.setRequestProperty("charset", "utf-8");
				httpConnection.setRequestProperty("Content-Length", "" + Integer.toString(value.getBytes().length));
				httpConnection.setUseCaches(false);
				httpConnection.setRequestMethod(type.toString());
				OutputStreamWriter out = new OutputStreamWriter(httpConnection.getOutputStream());
				out.write(value);
				out.close();
				break;

			case DELETE:
				httpConnection.setRequestMethod(type.toString());
				break;
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(sc.getInputStream()));
			result = in.readLine();
			in.close();

			APIrestful api = AppData.getGson().fromJson(result, APIrestful.class);

			return api.success;

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not use MD5 to encode HTTP request header");
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new RuntimeException("Bad URL");
		} catch (ConnectException e) {
			e.printStackTrace();

			//			UI.getCurrent().getSession().close();
			//			UI.getCurrent().getPage().setLocation("/error/noapi.html");
			throw new RuntimeException("Could not get response from API");

		} catch (IOException e) {
			int errorCode = 0;
			try {
				errorCode = httpConnection.getResponseCode();
				BufferedReader in = new BufferedReader(new InputStreamReader(httpConnection.getErrorStream()));
				errors = in.readLine();
				in.close();

				APIrestful api = AppData.getGson().fromJson(errors, APIrestful.class);
				errors = api.getErrors();

			} catch (IOException f) {
				f.printStackTrace();
			}

			switch (errorCode) {
			case 400:
			case 404:
				//Notification.show("API Error", errors, Notification.Type.HUMANIZED_MESSAGE);
			case 409:
				String logString = "API returned HTTP error code: " + errorCode + " with error stream: " + errors;
				System.out.println(logString);
				return false;

			default:
				break;
			}
			e.printStackTrace();
			throw new RuntimeException(e + " - " + errors);

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}
}

class APIrestfulDeserializer implements JsonDeserializer<APIrestful> {
	public APIrestful deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

		APIrestful response = new APIrestful();

		if (!json.isJsonObject()) {
			response.setSuccess(false);
			return response;
		}

		JsonObject jsonObject = json.getAsJsonObject();

		if (jsonObject.has("result")) {
			response.setResult(jsonObject.get("result").toString());
			response.setSuccess(true);
		} else if (jsonObject.has("errors")) {
			JsonArray array = jsonObject.get("errors").getAsJsonArray();
			int length = array.size();
			StringBuffer errors = new StringBuffer();
			for (int i = 0; i < length; i++) {
				errors.append(array.get(i).getAsString());
				errors.append(",");
			}
			if (errors.length() > 0) {
				errors.deleteCharAt(errors.length() - 1);
			}
			response.setErrors(errors.toString());
			response.setSuccess(false);
		} else {
			// response does not have either and is valid JSON, so let processing continue
			response.setSuccess(true);
		}

		return response;
	}
}
