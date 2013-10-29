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
