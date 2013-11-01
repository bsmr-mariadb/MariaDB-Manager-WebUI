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

package com.skysql.manager;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.vaadin.server.WebBrowser;

public class DateConversion {
	public static String DEFAULT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

	private boolean adjust = false;
	private String format = DEFAULT_TIME_FORMAT;

	public DateConversion(boolean adjust, String format) {
		this.adjust = adjust;
		this.format = (format == null ? DEFAULT_TIME_FORMAT : format);

		WebBrowser webBrowser = new WebBrowser();
		int tzOffset = webBrowser.getTimezoneOffset();
		int tzDSOffset = webBrowser.getRawTimezoneOffset();
		int dstSavings = webBrowser.getDSTSavings();

	}

	public void setAdjuts(boolean adjust) {
		this.adjust = adjust;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String adjust(String timestamp) {
		if (timestamp == null || timestamp.isEmpty() || adjust == false) {
			return timestamp;
		} else {
			String adjusted = null;
			SimpleDateFormat sdfInput = new SimpleDateFormat("E, d MMM y HH:mm:ss Z"); // Mon, 02 Sep 2013 13:08:14 +0000
			try {
				SimpleDateFormat sdfOutput = new SimpleDateFormat(format);
				Date myDate = sdfInput.parse(timestamp);
				adjusted = sdfOutput.format(myDate);
			} catch (Exception e) {
				System.err.println("Execption parsing timestamp: " + timestamp + " with format: " + format);
				e.printStackTrace();
				adjusted = "Format Error";
			}
			return adjusted;
		}
	}

	public String adjust(Date myDate) {
		if (myDate == null) {
			return null;
		} else {
			SimpleDateFormat sdfOutput = new SimpleDateFormat(format);
			String adjusted = sdfOutput.format(myDate);
			return adjusted;
		}
	}

}
