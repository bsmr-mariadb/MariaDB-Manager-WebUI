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

package com.skysql.manager.ui.components;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.component.VEvent;

import com.skysql.manager.ClusterComponent;
import com.skysql.manager.DateConversion;
import com.skysql.manager.ManagerUI;
import com.skysql.manager.ScheduleRecord;
import com.skysql.manager.SystemRecord;
import com.skysql.manager.iCalSupport;
import com.skysql.manager.api.Schedule;
import com.skysql.manager.api.SystemInfo;
import com.skysql.manager.ui.CalendarDialog;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * The Class BackupScheduledLayout.
 */
@SuppressWarnings("serial")
public class BackupScheduledLayout extends VerticalLayout {

	private Schedule schedule;
	private Table scheduledTable;
	private UpdaterThread updaterThread;
	private BackupScheduledLayout thisLayout = this;
	private Button calendarButton;

	/**
	 * Instantiates a new backup scheduled layout.
	 */
	public BackupScheduledLayout() {

		addStyleName("scheduledLayout");
		setSpacing(true);
		setMargin(true);

		HorizontalLayout scheduleRow = new HorizontalLayout();
		scheduleRow.setSpacing(true);
		addComponent(scheduleRow);

		final Label scheduleLabel = new Label("Schedule backups using the");
		scheduleLabel.setSizeUndefined();
		scheduleRow.addComponent(scheduleLabel);
		scheduleRow.setComponentAlignment(scheduleLabel, Alignment.MIDDLE_LEFT);

		calendarButton = new Button("Calendar");
		calendarButton.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(Button.ClickEvent event) {
				if (schedule == null) {
					schedule = getSchedule();
				}
				new CalendarDialog(schedule, thisLayout);
			}
		});
		scheduleRow.addComponent(calendarButton);
		scheduleRow.setComponentAlignment(calendarButton, Alignment.MIDDLE_LEFT);

		final Label immediateLabel = new Label("(To run an immediate backup, select a node first then switch to the Control panel)");
		immediateLabel.setSizeUndefined();
		scheduleRow.addComponent(immediateLabel);
		scheduleRow.setComponentAlignment(immediateLabel, Alignment.MIDDLE_CENTER);

		scheduledTable = new Table("Next Scheduled Backups");
		scheduledTable.setPageLength(5);
		// Start time, node
		scheduledTable.addContainerProperty("Start", String.class, null);
		scheduledTable.addContainerProperty("Node", String.class, null);
		scheduledTable.addContainerProperty("Level", String.class, null);
		scheduledTable.addContainerProperty("User", String.class, null);
		addComponent(scheduledTable);
		setComponentAlignment(scheduledTable, Alignment.MIDDLE_LEFT);

	}

	/**
	 * Gets the schedule.
	 *
	 * @return the schedule
	 */
	private Schedule getSchedule() {
		SystemInfo systemInfo = VaadinSession.getCurrent().getAttribute(SystemInfo.class);
		String systemID = systemInfo.getCurrentID();
		if (SystemInfo.SYSTEM_ROOT.equals(systemID)) {
			ClusterComponent clusterComponent = VaadinSession.getCurrent().getAttribute(ClusterComponent.class);
			systemID = clusterComponent.getID();
		}

		return new Schedule(systemID, null);

	}

	/**
	 * Collect events.
	 *
	 * @return the tree map
	 */
	private TreeMap<String, String> collectEvents() {

		TreeMap<String, String> eventsTree = new TreeMap<String, String>();

		// loop through events from API and add to calendar
		final LinkedHashMap<String, ScheduleRecord> scheduleList = schedule.getScheduleList();
		ListIterator<Map.Entry<String, ScheduleRecord>> iter = new ArrayList<Entry<String, ScheduleRecord>>(scheduleList.entrySet()).listIterator();

		while (iter.hasNext()) {
			Map.Entry<String, ScheduleRecord> entry = iter.next();
			ScheduleRecord scheduleRecord = entry.getValue();

			String iCalString = scheduleRecord.getICal();
			VEvent vEvent = iCalSupport.readVEvent(iCalString);
			addEventsToMap(eventsTree, scheduleRecord.getID(), vEvent);

		}

		return eventsTree;

	}

	/**
	 * Adds the events to map.
	 *
	 * @param eventsMap the events map
	 * @param eventID the event id
	 * @param vEvent the vEvent
	 */
	private void addEventsToMap(TreeMap<String, String> eventsMap, String eventID, VEvent vEvent) {

		Date start = new Date();
		GregorianCalendar calendar = new GregorianCalendar(UI.getCurrent().getLocale());
		calendar.setTime(start);
		calendar.add(GregorianCalendar.MONTH, 1);
		Date end = calendar.getTime();
		PeriodList periodList = vEvent.calculateRecurrenceSet(new Period(new DateTime(start), new DateTime(end)));
		for (Object po : periodList) {
			Period period = (Period) po;
			DateTime startDate = new DateTime(period.getStart());
			eventsMap.put(startDate.toString(), eventID);
		}

	}

	/**
	 * Refresh.
	 */
	public void refresh() {

		ManagerUI.log("ScheduleLayout refresh()");
		updaterThread = new UpdaterThread(updaterThread);
		updaterThread.start();

	}

	/**
	 * The Class UpdaterThread.
	 */
	class UpdaterThread extends Thread {

		/** The old updater thread. */
		UpdaterThread oldUpdaterThread;

		/** The flagged. */
		volatile boolean flagged = false;

		/**
		 * Instantiates a new updater thread.
		 *
		 * @param oldUpdaterThread the old updater thread
		 */
		UpdaterThread(UpdaterThread oldUpdaterThread) {
			this.oldUpdaterThread = oldUpdaterThread;
		}

		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			if (oldUpdaterThread != null && oldUpdaterThread.isAlive()) {
				ManagerUI.log(this.getClass().getName() + " - Old thread is alive: " + oldUpdaterThread);
				oldUpdaterThread.flagged = true;
				oldUpdaterThread.interrupt();
				try {
					ManagerUI.log(this.getClass().getName() + " - Before Join");
					oldUpdaterThread.join();
					ManagerUI.log(this.getClass().getName() + " - After Join");
				} catch (InterruptedException iex) {
					ManagerUI.log(this.getClass().getName() + " - Interrupted Exception");
					return;
				}

			}

			ManagerUI.log(this.getClass().getName() + " - UpdaterThread.this: " + this);
			asynchRefresh(this);
		}
	}

	/**
	 * Asynch refresh.
	 *
	 * @param updaterThread the updater thread
	 */
	private void asynchRefresh(final UpdaterThread updaterThread) {

		ManagerUI managerUI = getSession().getAttribute(ManagerUI.class);

		schedule = getSchedule();
		final LinkedHashMap<String, ScheduleRecord> scheduleList = schedule.getScheduleList();

		managerUI.access(new Runnable() {
			@Override
			public void run() {
				// Here the UI is locked and can be updated

				ManagerUI.log(this.getClass().getName() + " access run(): ");

				SystemInfo systemInfo = VaadinSession.getCurrent().getAttribute(SystemInfo.class);
				String systemID = systemInfo.getCurrentID();
				if (systemID.equals(SystemInfo.SYSTEM_ROOT)) {
					ClusterComponent clusterComponent = VaadinSession.getCurrent().getAttribute(ClusterComponent.class);
					systemID = clusterComponent.getID();
				}
				SystemRecord systemRecord = systemInfo.getSystemRecord(systemID);
				if (systemRecord.getNodes().length == 0) {
					calendarButton.setEnabled(false);
					calendarButton.setDescription("There are no nodes in this system: no backups scheduling is possible.");
				} else {
					calendarButton.setEnabled(true);
					calendarButton.setDescription(null);
				}

				scheduledTable.removeAllItems();
				if (scheduleList != null) {

					DateConversion dateConversion = getSession().getAttribute(DateConversion.class);
					TreeMap<String, String> eventsTree = collectEvents();

					int i = 0;
					for (Map.Entry<String, String> entry : eventsTree.entrySet()) {
						String key = entry.getKey();
						String myDate = null;
						try {
							Date start = new DateTime(key);
							SimpleDateFormat sdfInput = new SimpleDateFormat("E, d MMM y HH:mm:ss Z"); // Mon, 02 Sep 2013 13:08:14 +0000
							myDate = sdfInput.format(start);
						} catch (ParseException e) {
							e.printStackTrace();
						}

						ScheduleRecord scheduleRecord = scheduleList.get(entry.getValue());

						scheduledTable.addItem(new Object[] { dateConversion.adjust(myDate), scheduleRecord.getNodeID(), scheduleRecord.getParams(),
								scheduleRecord.getUserID() }, i++);
					}

				}

			}
		});

	}
}
