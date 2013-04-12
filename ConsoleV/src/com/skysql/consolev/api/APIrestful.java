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

		URL url = new URL("http://localhost/consoleAPI/api/" + uri);
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
			throw new RuntimeException("Could not get response from API");

		} catch (IOException e) {
			BufferedReader in = new BufferedReader(new InputStreamReader(httpConnection.getErrorStream()));
			String inputLine = in.readLine();
			in.close();
			switch (httpConnection.getResponseCode()) {
			case 400:
			case 404:
			case 409:
				Notification.show(inputLine);
				return null;
			}
			throw new RuntimeException(e + " - " + inputLine);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
}
