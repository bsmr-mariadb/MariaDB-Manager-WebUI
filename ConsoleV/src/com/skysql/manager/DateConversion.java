/*
 * This file is distributed as part of the MariaDB Manager.  It is free
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
 * Copyright 2012-2014 SkySQL Corporation Ab
 */

package com.skysql.manager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import com.vaadin.server.WebBrowser;
import com.vaadin.ui.UI;

/**
 * The Class DateConversion. Converts date received from API into local time zone and custom format.
 */
public class DateConversion {
	public static String DEFAULT_INPUT_FORMAT = "E, d MMM y HH:mm:ss Z"; // as currently returned by the API: Mon, 02 Sep 2013 13:08:14 +0000
	public static String DEFAULT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

	private boolean adjust = false;
	private String format = DEFAULT_TIME_FORMAT;
	private String clientTZname;
	private SimpleTimeZone clientTimeZone;

	/**
	 * Instantiates a new date conversion.
	 *
	 * @param adjust adjust to local tz or not
	 * @param format the output format
	 */
	public DateConversion(boolean adjust, String format) {
		this.adjust = adjust;
		this.format = (format == null ? DEFAULT_TIME_FORMAT : format);

		WebBrowser webBrowser = UI.getCurrent().getPage().getWebBrowser();
		int browserOffset = webBrowser.getRawTimezoneOffset();
		clientTimeZone = new SimpleTimeZone(browserOffset, "Client time zone");
		clientTZname = clientTimeZone.getDisplayName();
	}

	/**
	 * Gets the client tz name.
	 *
	 * @return the client tz name
	 */
	public String getClientTZname() {
		return clientTZname;
	}

	/**
	 * Checks if is adjusted to local TZ.
	 *
	 * @return true, if is adjusted to local
	 */
	public boolean isAdjustedToLocal() {
		return adjust;
	}

	/**
	 * Sets the adjusted to local.
	 *
	 * @param adjust the new adjusted to local
	 */
	public void setAdjustedToLocal(boolean adjust) {
		this.adjust = adjust;
	}

	/**
	 * Gets the output date format.
	 *
	 * @return the format
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * Sets the output date format.
	 *
	 * @param format the new format
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * Adjust the input date to local tz (or not) and apply custom format.
	 *
	 * @param timestamp the timestamp
	 * @return the string
	 */
	public String adjust(String timestamp) {
		if (timestamp == null || timestamp.isEmpty()) {
			return timestamp;
		} else {
			String adjusted = null;
			SimpleDateFormat sdfInput = new SimpleDateFormat(DEFAULT_INPUT_FORMAT);
			try {
				SimpleDateFormat sdfOutput = new SimpleDateFormat(format);
				Date myDate = sdfInput.parse(timestamp);
				if (adjust) {
					sdfOutput.setTimeZone(clientTimeZone);
				} else {
					sdfOutput.setTimeZone(TimeZone.getTimeZone("GMT"));
				}
				adjusted = sdfOutput.format(myDate);
			} catch (Exception e) {
				System.err.println("Execption parsing timestamp: " + timestamp + " with format: " + format);
				e.printStackTrace();
				adjusted = "Format Error";
			}
			return adjusted;
		}
	}

}
