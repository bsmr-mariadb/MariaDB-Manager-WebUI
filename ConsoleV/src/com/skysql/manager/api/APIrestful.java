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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.MalformedJsonException;
import com.skysql.manager.AppData.Debug;
import com.skysql.manager.Commands;
import com.skysql.manager.ManagerUI;
import com.skysql.manager.MonitorLatest;
import com.skysql.manager.ui.DebugPanel;
import com.skysql.manager.ui.ErrorDialog;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Notification;

public class APIrestful {

	private static final SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
	private static String apiURI;
	private static String keyID, keyCode;
	private static Gson gson;
	private static APIrestful api;

	protected boolean success;
	private int errorCode = 200;
	private String lastCall;
	private String result;
	private String errors;
	private static String version;

	public static String getURI() {
		return apiURI;
	}

	public static APIrestful newInstance(String URI, Hashtable<String, String> keys) {
		if (api == null) {
			api = new APIrestful();
			apiURI = URI;
			if (keys.size() > 0) {
				// for now take first set - in future loop through and take first one that works
				keyID = (String) keys.keySet().toArray()[0];
				keyCode = keys.get(keyID);
			}
			api.get("apidate");
			String apiDate = api.result.substring(api.result.indexOf(":") + 2);
			apiDate = apiDate.substring(0, apiDate.indexOf("\""));
			version += " (" + apiDate + ")";
		}

		return api;
	}

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
			gsonBuilder.registerTypeAdapter(MonitorLatest.class, new ObjectDeserializer());
			gsonBuilder.registerTypeAdapter(NodeInfo.class, new NodeInfoDeserializer());
			gsonBuilder.registerTypeAdapter(NodeStates.class, new NodeStatesDeserializer());
			gsonBuilder.registerTypeAdapter(Schedule.class, new ScheduleDeserializer());
			gsonBuilder.registerTypeAdapter(SettingsValues.class, new SettingsValuesDeserializer());
			gsonBuilder.registerTypeAdapter(Steps.class, new StepsDeserializer());
			gsonBuilder.registerTypeAdapter(SystemInfo.class, new SystemInfoDeserializer());
			gsonBuilder.registerTypeAdapter(SystemTypes.class, new SystemTypesDeserializer());
			gsonBuilder.registerTypeAdapter(TaskInfo.class, new TaskInfoDeserializer());
			gsonBuilder.registerTypeAdapter(UserInfo.class, new UserInfoDeserializer());
			gsonBuilder.registerTypeAdapter(UserObject.class, new UserObjectDeserializer());
			gsonBuilder.registerTypeAdapter(WriteResponse.class, new ResponseDeserializer());

			//
			gsonBuilder.registerTypeAdapter(ChartProperties.class, new ChartPropertiesDeserializer());

			gson = gsonBuilder.create();
		}
		return gson;
	}

	protected enum CallType {
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

	public String getCall() {
		return lastCall;
	}

	public String getVersion() {
		return version;
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

	public String errorString() {
		return "<p class=\"api-response\">" + getCall() + "</p>" + "HTTP " + errorCode + " - "
				+ (getResult() != null ? "Response: <p class=\"api-response\">" + getResult() + "</p>" : "")
				+ (getErrors() != null ? "Errors: <p class=\"api-response\">" + getErrors() + "</p>" : "");
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
		URL url = null;
		long startTime = System.nanoTime();

		DebugPanel debugPanel = VaadinSession.getCurrent().getAttribute(DebugPanel.class);

		try {
			url = new URL(apiURI + "/" + uri + ((type == CallType.GET && value != null) ? value : ""));
			lastCall = type + " " + url.toString() + (type != CallType.GET && value != null ? " parameters: " + value : "");
			ManagerUI.log("API " + lastCall, debugPanel);
			URLConnection sc = url.openConnection();
			httpConnection = (HttpURLConnection) sc;
			String date = sdf.format(new Date());

			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] mdbytes = md.digest((uri + keyCode + date).getBytes("UTF-8"));
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < mdbytes.length; i++) {
				sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
			}
			sc.setRequestProperty("Authorization", "api-auth-" + keyID + "-" + sb.toString());
			sc.setRequestProperty("Date", date);
			sc.setRequestProperty("Accept", "application/json");
			sc.setRequestProperty("X-SkySQL-API-Version", "1");

			switch (type) {
			case GET:
				//httpConnection.setRequestProperty("Accept-Encoding", "gzip");
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

			int timeout = httpConnection.getConnectTimeout();
			httpConnection.setConnectTimeout(timeout);

			BufferedReader in = new BufferedReader(new InputStreamReader(sc.getInputStream()));
			result = in.readLine();

			if (api.version == null) {
				Map<String, List<String>> headers = httpConnection.getHeaderFields();
				List<String> version = headers.get("X-SkySQL-API-Version");
				api.version = version.get(0);
			}
			in.close();

			long estimatedTime = (System.nanoTime() - startTime) / 1000000;
			ManagerUI.log("Response Time: " + estimatedTime + "ms, inputStream: " + result, debugPanel);

			APIrestful api = getGson().fromJson(result, APIrestful.class);

			return api.success;

		} catch (NoSuchAlgorithmException e) {
			new ErrorDialog(e, "Could not use MD5 to encode HTTP request header");
			throw new RuntimeException();
		} catch (MalformedURLException e) {
			new ErrorDialog(e, "Bad URL: " + url.toString());
			throw new RuntimeException();
		} catch (ConnectException e) {
			new ErrorDialog(e, "API not responding at: " + getURI());
			throw new RuntimeException("API not responding");
		} catch (MalformedJsonException e) {
			new ErrorDialog(e, "API did not return JSON for: " + api.errorString());
			throw new RuntimeException("MalformedJson inputStream");
		} catch (IOException e) {
			try {
				errorCode = httpConnection.getResponseCode();

				BufferedReader in = new BufferedReader(new InputStreamReader(httpConnection.getErrorStream()));
				errors = in.readLine();
				in.close();

				long estimatedTime = (System.nanoTime() - startTime) / 1000000;
				if (Debug.ON) {
					ManagerUI.log("Response Time: " + estimatedTime + "ms, errorStream: " + errors, debugPanel);
				} else {
					System.err.println("API " + lastCall);
					System.err.println("Response Time: " + estimatedTime + "ms, errorStream: " + errors);
				}

				APIrestful api = getGson().fromJson(errors, APIrestful.class);
				errors = api.getErrors();

			} catch (JsonSyntaxException f) {
				new ErrorDialog(f, "API did not return JSON for:" + errorString());
				throw new RuntimeException("JsonSyntax errorStream");
			} catch (MalformedJsonException f) {
				new ErrorDialog(f, "API did not return JSON for:" + errorString());
				throw new RuntimeException("MalformedJson errorStream");
			} catch (Exception f) {
				System.err.println("API call: " + url.toString() + " returned: " + errors);
				new ErrorDialog(f, "API call: " + url.toString());
				throw new RuntimeException("Exception errorStream");
			}

			switch (errorCode) {
			case 400:
			case 404:
			case 409:
				Notification.show(errors, Notification.Type.WARNING_MESSAGE);
				return false;

			default:
				break;
			}
			new ErrorDialog(e, "API returned an error for:" + errorString());
			throw new RuntimeException("API Error");

		} catch (JsonSyntaxException e) {
			new ErrorDialog(e, "API did not return HTTP error nor valid JSON for:" + api.errorString());
			throw new RuntimeException("JsonSyntax inputStream");
		} catch (Exception e) {
			new ErrorDialog(e, "API call: " + url.toString());
			throw new RuntimeException("Exception inputStream");
		}

	}

	public String encryptAES(String input) {
		SecureRandom random = new SecureRandom();
		byte ivBytes[] = new byte[16];
		random.nextBytes(ivBytes);
		byte[] data = input.getBytes();
		byte[] encrypted = null;
		byte[] keyBytes = new BigInteger(keyCode, 16).toByteArray();
		SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
		Cipher cipher;
		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
			encrypted = cipher.doFinal(data);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}

		byte output[] = null;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			outputStream.write(ivBytes);
			outputStream.write(encrypted);
			output = outputStream.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return DatatypeConverter.printBase64Binary(output);
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
