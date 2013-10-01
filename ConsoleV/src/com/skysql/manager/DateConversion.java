package com.skysql.manager;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.skysql.manager.ui.GeneralSettings;

public class DateConversion {
	public static String DEFAULT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static boolean adjust = false;
	private static String format = DEFAULT_TIME_FORMAT;

	public static void setAdjust(boolean adjust) {
		DateConversion.adjust = adjust;
	}

	public static void setAdjust(String adjust) {
		DateConversion.adjust = (adjust == null ? GeneralSettings.DEFAULT_TIME_ADJUST : Boolean.valueOf(adjust));
	}

	public static void setFormat(String format) {
		DateConversion.format = (format == null ? DEFAULT_TIME_FORMAT : format);
	}

	public static String adjust(String timestamp) {
		if (timestamp == null || adjust == false) {
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

	public static String adjust(Date myDate) {
		if (myDate == null) {
			return null;
		} else {
			SimpleDateFormat sdfOutput = new SimpleDateFormat(format);
			String adjusted = sdfOutput.format(myDate);
			return adjusted;
		}
	}

}
