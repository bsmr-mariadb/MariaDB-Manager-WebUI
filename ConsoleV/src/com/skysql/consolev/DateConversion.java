package com.skysql.consolev;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class DateConversion {

	long start, end;
	long delta, count = 15;

	public DateConversion() {
	}

	public DateConversion(String start, String end) {
		this.start = dateToCal(start);
		this.end = dateToCal(end);
		delta = (this.end - this.start) / count;
	}

	public long convert(String date) {
		if (delta != 0) {
			return (dateToCal(date) - start) / delta;
		} else {
			return 0;
		}

	}

	private long dateToCal(String date) {
		Calendar cal = new GregorianCalendar();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		try {
			cal.setTime(sdf.parse(date));
			return (cal.getTimeInMillis());
		} catch (Exception e) {
			e.printStackTrace();
			return (0);
		}
	}

}
