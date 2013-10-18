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

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Date;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;

public class iCalSupport {

	public static Calendar createiCal() {
		net.fortuna.ical4j.model.Calendar iCalendar = new net.fortuna.ical4j.model.Calendar();
		iCalendar.getProperties().add(new ProdId("-//SkySQL//MariaDB Manager 1.0//EN"));
		iCalendar.getProperties().add(Version.VERSION_2_0);
		iCalendar.getProperties().add(CalScale.GREGORIAN);

		return iCalendar;
	}

	public static VEvent createiEvent(String name, String description, Date startDate) {
		// Create a TimeZone
		//		TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
		//		TimeZone timezone = registry.getTimeZone("America/Denver");
		//		startDate.setTimeZone(timezone);
		//		endDate.setTimeZone(timezone);

		// Create the event
		String eventName = (name == null ? "" : name);
		DateTime start = new DateTime(startDate);
		start.setUtc(true);
		DateTime end = start;
		VEvent event = new VEvent(start, end, eventName);

		Description desc = new Description(description);
		event.getProperties().add(desc);

		/***
		Recur recur = new Recur(Recur.WEEKLY, null);
		RRule rule = new RRule(recur);
		event.getProperties().add(rule);
		***/
		//event.calculateRecurrenceSet(period);

		// Create a calendar
		//net.fortuna.ical4j.model.Calendar icsCalendar = createiCal();
		// Add the event and print
		//icsCalendar.getComponents().add(meeting);

		return event;

	}

	public static void readiEvent(String iCalString) {

		try {
			StringReader sin = new StringReader(iCalString);
			CalendarBuilder builder = new CalendarBuilder();
			Calendar calendar = builder.build(sin);

			net.fortuna.ical4j.model.Component event = calendar.getComponent("VEVENT");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void addUid(VEvent event, String uid) {
		try {
			event.getProperty("UID").setValue(uid);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}