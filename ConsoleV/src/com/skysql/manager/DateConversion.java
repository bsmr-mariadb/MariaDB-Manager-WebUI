package com.skysql.manager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class DateConversion {

	long start, end;
	long delta, count;
	long last = -1;

	public DateConversion() {
	}

	public DateConversion(String start, String end, String interval) {
		this.start = dateToCal(start);
		this.end = dateToCal(end);
		count = Long.valueOf(interval) / 60;
		delta = (this.end - this.start) / count;
	}

	public long convert(String date) {
		if (delta != 0) {
			long value = (dateToCal(date) - start) / delta;
			if (value > last) {
			} else {
				value++;
			}
			last = value;
			return value;
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
