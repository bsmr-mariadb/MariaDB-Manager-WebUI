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

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Date;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Version;

import com.skysql.manager.ui.CalendarCustomEvent;
import com.skysql.manager.ui.CalendarDialog.Until;

/**
 * The Class iCalSupport. Manipulate iCal entries associated with API's scheduled tasks.
 */
public class iCalSupport {

	/**
	 * Create new ical.
	 *
	 * @return the calendar
	 */
	public static synchronized Calendar createiCal() {
		net.fortuna.ical4j.model.Calendar iCalendar = new net.fortuna.ical4j.model.Calendar();
		iCalendar.getProperties().add(new ProdId("-//SkySQL//MariaDB Manager 1.0//EN"));
		iCalendar.getProperties().add(Version.VERSION_2_0);
		iCalendar.getProperties().add(CalScale.GREGORIAN);

		return iCalendar;
	}

	/**
	 * Create VEvent.
	 *
	 * @param calEvent the cal event
	 * @return the VEvent
	 */
	public static synchronized VEvent createVEvent(CalendarCustomEvent calEvent) {
		// Create a TimeZone
		//		TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
		//		TimeZone timezone = registry.getTimeZone("America/Denver");
		//		startDate.setTimeZone(timezone);
		//		endDate.setTimeZone(timezone);

		// Create the event
		String name = calEvent.getCaption();
		String eventName = (name == null ? "" : name);

		DateTime start = new DateTime(calEvent.getStart());
		start.setUtc(true);
		DateTime end = start;
		VEvent event = new VEvent(start, end, eventName);

		Description desc = new Description(calEvent.getDescription());
		event.getProperties().add(desc);

		String repeat = calEvent.getRepeat();
		if (!repeat.equals(CalendarCustomEvent.RECUR_NONE)) {
			Recur recur = null;
			String untilSelect = calEvent.getUntilSelect();

			if (untilSelect.equals(Until.never.name())) {
				recur = new Recur(repeat, null);

			} else if (untilSelect.equals(Until.count.name())) {
				String count = calEvent.getUntilCount();
				recur = new Recur(repeat, Integer.valueOf(count));

			} else if (untilSelect.equals(Until.date.name())) {
				Date until = calEvent.getUntilDate();
				recur = new Recur(repeat, new DateTime(until.getTime()));

			}

			RRule rule = new RRule(recur);
			event.getProperties().add(rule);
		}

		// Create a calendar
		//net.fortuna.ical4j.model.Calendar icsCalendar = createiCal();
		//icsCalendar.getComponents().add(meeting);

		return event;

	}

	/**
	 * Sets the event RRule by parsing the repeat string.
	 *
	 * @param repeat the repeat
	 * @param event the event
	 */
	public static synchronized void parseRepeat(String repeat, CalendarCustomEvent event) {
		if (repeat == null) {
			event.setRepeat(CalendarCustomEvent.RECUR_NONE);
			event.setUntilSelect(Until.never.name());
			event.setUntilCount("1");
			event.setUntilDate(event.getStart());
		} else {
			try {
				RRule rule = new RRule(repeat);
				Recur recur = rule.getRecur();
				int count = recur.getCount();
				String frequency = recur.getFrequency();
				Date until = recur.getUntil();
				event.setRepeat(frequency);
				if (count > 0) {
					event.setUntilSelect(Until.count.name());
					event.setUntilCount(String.valueOf(count));
				} else if (until != null) {
					event.setUntilSelect(Until.date.name());
					event.setUntilDate(until);
				} else {
					event.setUntilSelect(Until.never.name());
				}

			} catch (ParseException e) {
				ManagerUI.error(e.getMessage());
			}
		}

	}

	/**
	 * Creates VEvent from eventString.
	 *
	 * @param eventString the event string
	 * @return the vEvent
	 */
	public static synchronized VEvent readVEvent(String eventString) {

		try {
			String wholeCal = "BEGIN:VCALENDAR\r\nVERSION:2.0\r\nPRODID:-//SkySQL//MariaDB Manager 1.0//EN\r\n" + eventString + "\r\nEND:VCALENDAR";
			StringReader sin = new StringReader(wholeCal);
			CalendarBuilder builder = new CalendarBuilder();
			Calendar calendar = builder.build(sin);

			return (VEvent) calendar.getComponent("VEVENT");

		} catch (Exception e) {
			ManagerUI.error(e.getMessage());
		}

		return null;
	}

	/**
	 * Adds the uid to a VEvent.
	 *
	 * @param event the VEvent
	 * @param uid the uid
	 */
	public static synchronized void addUid(VEvent event, String uid) {
		try {
			event.getProperty("UID").setValue(uid);
		} catch (IOException e) {
			ManagerUI.error(e.getMessage());
		} catch (URISyntaxException e) {
			ManagerUI.error(e.getMessage());
		} catch (ParseException e) {
			ManagerUI.error(e.getMessage());
		}
	}

	/**
	 * Adds the excluded date.
	 *
	 * @param event the event
	 * @param date the date
	 */
	public static synchronized void addExcludedDate(VEvent event, Date date) {
		final ExDate exDate = new ExDate();
		//exDate.setTimeZone(tz);
		DateTime start = new DateTime(date);
		start.setUtc(true);
		exDate.getDates().add(start);
		event.getProperties().add(exDate);

	}

	/**
	 * Delete future recurring events past the given endDate.
	 *
	 * @param vEvent the VEvent
	 * @param endDate the end date
	 */
	public static synchronized void deleteAllFuture(VEvent vEvent, Date endDate) {
		net.fortuna.ical4j.model.Property rruleProperty = vEvent.getProperty("RRULE");
		String rruleStr = rruleProperty != null ? rruleProperty.getValue() : null;
		try {
			RRule rule = new RRule(rruleStr);
			Recur recur = rule.getRecur();
			recur.setCount(0);
			DateTime newEndDate = new DateTime(endDate.getTime() - 1);
			newEndDate.setUtc(true);
			recur.setUntil(newEndDate);
			vEvent.getProperties().remove(rruleProperty);
			vEvent.getProperties().add(rule);
		} catch (ParseException e) {
			ManagerUI.error(e.getMessage());
		}

	}
}