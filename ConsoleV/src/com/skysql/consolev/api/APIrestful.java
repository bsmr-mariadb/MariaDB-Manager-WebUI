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
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.vaadin.ui.Notification;

public class APIrestful {

	public static String newAPIurl = "http://localhost/consoleAPI/api/";
	private static final String AUTHORIZATION_ID_SKYSQL_API = "1";
	private static final String AUTHORIZATION_CODE_SKYSQL_API = "1f8d9e040e65d7b105538b1ed0231770";
	private static final SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

	private enum CallType {
		GET, PUT, POST, DELETE;
	}

	public String get(String uri) throws IOException {

		return call(uri, CallType.GET, null);

	}

	public String put(String uri, String value) throws IOException {

		return call(uri, CallType.PUT, value);

	}

	public String post(String uri, String value) throws IOException {

		return call(uri, CallType.POST, value);

	}

	public String delete(String uri) throws IOException {

		return call(uri, CallType.DELETE, null);

	}

	private String call(String uri, CallType type, String value) throws IOException {

		URL url = new URL(newAPIurl + uri);
		URLConnection sc = url.openConnection();
		HttpURLConnection httpConnection = (HttpURLConnection) sc;
		String date = sdf.format(new Date());
		try {
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
			String inputLine = in.readLine();
			in.close();
			return inputLine;

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Could not use MD5 to encode HTTP request header");

		} catch (ConnectException e) {
			//			UI.getCurrent().getSession().close();
			//			UI.getCurrent().getPage().setLocation("/error/noapi.html");
			throw new RuntimeException("Could not get response from API");

		} catch (IOException e) {
			int errorCode = httpConnection.getResponseCode();
			BufferedReader in = new BufferedReader(new InputStreamReader(httpConnection.getErrorStream()));
			String inputLine = in.readLine();
			in.close();
			switch (errorCode) {
			case 400:
			case 404:
			case 409:
				Notification.show(inputLine);
				String logString = "API returned HTTP error code: " + errorCode + " with error stream: " + inputLine;
				System.out.println(logString);
				return null;
			}
			throw new RuntimeException(e + " - " + inputLine);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
}
