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

package com.skysql.manager.ui;

import java.net.SocketException;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.util.UidGenerator;

import com.skysql.manager.BackupRecord;
import com.skysql.manager.ClusterComponent;
import com.skysql.manager.ManagerUI;
import com.skysql.manager.ScheduleRecord;
import com.skysql.manager.iCalSupport;
import com.skysql.manager.api.NodeInfo;
import com.skysql.manager.api.Schedule;
import com.skysql.manager.api.SystemInfo;
import com.skysql.manager.api.UserObject;
import com.skysql.manager.ui.components.BackupScheduledLayout;
import com.skysql.manager.ui.components.ParametersLayout;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Calendar;
import com.vaadin.ui.Calendar.TimeFormat;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.FormFieldFactory;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Select;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.DateClickEvent;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.EventClick;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.EventClickHandler;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.RangeSelectEvent;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.RangeSelectHandler;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.WeekClick;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.WeekClickHandler;
import com.vaadin.ui.components.calendar.event.BasicEvent;
import com.vaadin.ui.components.calendar.event.BasicEventProvider;
import com.vaadin.ui.components.calendar.event.CalendarEvent;
import com.vaadin.ui.components.calendar.handler.BasicDateClickHandler;
import com.vaadin.ui.components.calendar.handler.BasicWeekClickHandler;

/**
 *  Calendar component test application.
 */
public class CalendarDialog implements Window.CloseListener {

	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private static final String DEFAULT_ITEMID = "DEFAULT";

	private enum Mode {
		MONTH, WEEK, DAY;
	}

	public enum Repeat {
		none, day, week, month, year, custom;
	}

	public enum Until {
		never, count, date;
	}

	private GregorianCalendar calendar;
	private Calendar calendarComponent;
	private Date currentMonthsFirstDate, visibleFirstDate;
	private final Label captionLabel = new Label("");
	private Button monthButton;
	private Button weekButton;
	private Button nextButton;
	private Button prevButton;
	private Select timeZoneSelect;
	private Select formatSelect;
	private Select localeSelect;
	private CheckBox hideWeekendsButton;
	private CheckBox allScheduleButton;
	private Window scheduleEventPopup, deleteSchedulePopup;
	private final Form scheduleEventForm = new Form();
	private Button deleteEventButton;
	private Button applyEventButton;
	private Button editOriginalButton;
	private Mode viewMode = Mode.MONTH;
	private BasicEventProvider dataSource;
	private Button addNewEvent;
	private String calendarHeight = null;
	private String calendarWidth = null;
	private Integer firstHour;
	private Integer lastHour;
	private Integer firstDay;
	private Integer lastDay;
	private boolean showWeeklyView;
	private boolean useSecondResolution;
	private Window dialogWindow;
	private NativeSelect repeatSelectField;
	private NativeSelect untilSelectField;
	private DateField startDateField;
	private DateField untilDateField;
	private TextField untilCountField;
	private ArrayList<NodeInfo> nodes;
	private Schedule schedule;
	private BackupScheduledLayout bsLayout;
	private LinkedHashMap<String, ArrayList> eventsMap;

	/**
	 * Instantiates a new calendar dialog.
	 *
	 * @param schedule the schedule
	 * @param bsLayout the bs layout
	 */
	public CalendarDialog(Schedule schedule, BackupScheduledLayout bsLayout) {
		this.schedule = schedule;
		this.bsLayout = bsLayout;

		dialogWindow = new ModalWindow("Backups", "700px");
		dialogWindow.setHeight("720px");
		dialogWindow.addCloseListener(this);
		UI.getCurrent().addWindow(dialogWindow);

		initContent();

	}

	/**
	 * Instantiates a new calendar dialog.
	 *
	 * @param mode the mode
	 */
	public CalendarDialog(String mode) {

		// mode: month, week, day

	}

	/**
	 *  Handle Close button click and close the window.
	 *
	 * @param event the event
	 */
	public void closeButtonClick(Button.ClickEvent event) {
		/* Windows are managed by the application object. */
		dialogWindow.close();
	}

	/**
	 *  In case the window is closed otherwise.
	 *
	 * @param e the event
	 */
	public void windowClose(CloseEvent e) {
		// anything special goes here
		bsLayout.refresh();
	}

	/**
	 * Sets the locale.
	 *
	 * @param locale the new locale
	 */
	private void setLocale(Locale locale) {
		UI.getCurrent().setLocale(locale);
	}

	/**
	 * Inits the content.
	 */
	public void initContent() {
		OverviewPanel overviewPanel = VaadinSession.getCurrent().getAttribute(OverviewPanel.class);
		nodes = overviewPanel.getNodes();

		if (nodes == null || nodes.isEmpty()) {
			SystemInfo systemInfo = VaadinSession.getCurrent().getAttribute(SystemInfo.class);
			String systemID = systemInfo.getCurrentID();
			String systemType = systemInfo.getCurrentSystem().getSystemType();
			if (systemID.equals(SystemInfo.SYSTEM_ROOT)) {
				ClusterComponent clusterComponent = VaadinSession.getCurrent().getAttribute(ClusterComponent.class);
				systemID = clusterComponent.getID();
				systemType = clusterComponent.getSystemType();
			}
			nodes = new ArrayList<NodeInfo>();
			for (String nodeID : systemInfo.getSystemRecord(systemID).getNodes()) {
				NodeInfo nodeInfo = new NodeInfo(systemID, systemType, nodeID);
				nodes.add(nodeInfo);
			}

		}

		// Set default Locale for this application
		setLocale(Locale.getDefault());

		// Initialize locale, timezone and timeformat selects.
		//localeSelect = createLocaleSelect();
		timeZoneSelect = createTimeZoneSelect();
		//formatSelect = createCalendarFormatSelect();

		initCalendar();
		initLayoutContent();

		addInitialEvents();
	}

	/**
	 * Adds the initial events.
	 */
	private void addInitialEvents() {
		Date originalDate = calendar.getTime();

		eventsMap = new LinkedHashMap<String, ArrayList>();

		// loop through events from API and add to calendar
		final LinkedHashMap<String, ScheduleRecord> scheduleList = schedule.getScheduleList();
		ListIterator<Map.Entry<String, ScheduleRecord>> iter = new ArrayList<Entry<String, ScheduleRecord>>(scheduleList.entrySet()).listIterator();

		while (iter.hasNext()) {
			Map.Entry<String, ScheduleRecord> entry = iter.next();
			ScheduleRecord scheduleRecord = entry.getValue();

			String iCalString = scheduleRecord.getICal();
			VEvent vEvent = iCalSupport.readVEvent(iCalString);
			addEventsToMap(scheduleRecord.getID(), vEvent, scheduleRecord.getNodeID());

		}

		calendar.setTime(originalDate);
	}

	/**
	 * Adds the events to map.
	 *
	 * @param eventID the event id
	 * @param vEvent the vEvent
	 * @param nodeID the node id
	 */
	private void addEventsToMap(String eventID, VEvent vEvent, String nodeID) {

		String summary = vEvent.getSummary() != null ? vEvent.getSummary().getValue() : null;
		String description = vEvent.getDescription() != null ? vEvent.getDescription().getValue() : null;
		net.fortuna.ical4j.model.Property rruleProperty = vEvent.getProperty("RRULE");
		String rrule = rruleProperty != null ? rruleProperty.getValue() : null;
		ManagerUI.log("RRULE: " + rrule);

		ArrayList<CalendarCustomEvent> eventsList = new ArrayList<CalendarCustomEvent>();
		eventsMap.put(eventID, eventsList);

		Date startDate = calendarComponent.getStartDate();
		if (startDate.before(getToday())) {
			startDate = getToday();
		}
		Date endDate = calendarComponent.getEndDate();
		if (startDate.before(endDate)) {
			PeriodList periodList = vEvent.calculateRecurrenceSet(new Period(new DateTime(startDate), new DateTime(endDate)));
			for (Object po : periodList) {
				Period period = (Period) po;
				ManagerUI.log(period.toString());

				DateTime start = new DateTime(period.getStart());
				DateTime end = new DateTime(period.getEnd());
				CalendarCustomEvent event = getNewEvent(summary, description, start, end, rrule, nodeID);
				event.setData(eventID);
				Date masterStart = vEvent.getStartDate().getDate();
				if (!start.equals(masterStart)) {
					event.setOccurrence(masterStart);
				}
				dataSource.addEvent(event);
				eventsList.add(event);
			}
		}

	}

	/**
	 * Inits the layout content.
	 */
	private void initLayoutContent() {
		initNavigationButtons();
		initAddNewEventButton();
		initHideWeekEndButton();
		//initAllScheduleButton();

		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setSpacing(true);
		hl.setMargin(new MarginInfo(false, false, true, false));
		hl.addComponent(prevButton);
		hl.addComponent(captionLabel);
		hl.addComponent(monthButton);
		hl.addComponent(weekButton);
		hl.addComponent(nextButton);
		hl.setComponentAlignment(prevButton, Alignment.MIDDLE_LEFT);
		hl.setComponentAlignment(captionLabel, Alignment.MIDDLE_CENTER);
		hl.setComponentAlignment(monthButton, Alignment.MIDDLE_CENTER);
		hl.setComponentAlignment(weekButton, Alignment.MIDDLE_CENTER);
		hl.setComponentAlignment(nextButton, Alignment.MIDDLE_RIGHT);

		monthButton.setVisible(viewMode == Mode.WEEK);
		weekButton.setVisible(viewMode == Mode.DAY);

		HorizontalLayout controlPanel = new HorizontalLayout();
		controlPanel.setSpacing(true);
		controlPanel.setMargin(new MarginInfo(false, false, true, false));
		controlPanel.setWidth("100%");
		//controlPanel.addComponent(localeSelect);
		//controlPanel.setComponentAlignment(localeSelect, Alignment.MIDDLE_LEFT);
		controlPanel.addComponent(timeZoneSelect);
		controlPanel.setComponentAlignment(timeZoneSelect, Alignment.MIDDLE_LEFT);
		//controlPanel.addComponent(formatSelect);
		//controlPanel.setComponentAlignment(formatSelect, Alignment.MIDDLE_LEFT);
		controlPanel.addComponent(addNewEvent);
		controlPanel.setComponentAlignment(addNewEvent, Alignment.BOTTOM_LEFT);
		controlPanel.addComponent(hideWeekendsButton);
		controlPanel.setComponentAlignment(hideWeekendsButton, Alignment.BOTTOM_LEFT);
		//controlPanel.addComponent(allScheduleButton);
		//controlPanel.setComponentAlignment(allScheduleButton, Alignment.MIDDLE_LEFT);

		VerticalLayout layout = (VerticalLayout) dialogWindow.getContent();
		layout.addComponent(controlPanel);
		layout.addComponent(hl);
		layout.addComponent(calendarComponent);
		layout.setExpandRatio(calendarComponent, 1);
	}

	/**
	 * Inits the navigation buttons.
	 */
	private void initNavigationButtons() {
		monthButton = new Button("Month view", new ClickListener() {

			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				switchToMonthView();
			}
		});

		weekButton = new Button("Week view", new ClickListener() {

			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				// simulate week click
				WeekClickHandler handler = (WeekClickHandler) calendarComponent.getHandler(WeekClick.EVENT_ID);
				handler.weekClick(new WeekClick(calendarComponent, calendar.get(GregorianCalendar.WEEK_OF_YEAR), calendar.get(GregorianCalendar.YEAR)));
			}
		});

		nextButton = new Button("Next", new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				handleNextButtonClick();
			}
		});

		prevButton = new Button("Prev", new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				handlePreviousButtonClick();
			}
		});
	}

	/**
	 * Inits the hide weekend button.
	 */
	private void initHideWeekEndButton() {
		hideWeekendsButton = new CheckBox("Hide weekends");
		hideWeekendsButton.setImmediate(true);
		hideWeekendsButton.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(ValueChangeEvent event) {
				setWeekendsHidden((Boolean) event.getProperty().getValue());
			}
		});
	}

	/**
	 * Sets the weekends hidden.
	 *
	 * @param weekendsHidden the new weekends hidden
	 */
	private void setWeekendsHidden(boolean weekendsHidden) {
		if (weekendsHidden) {
			//int firstToShow = (GregorianCalendar.MONDAY - calendar.getFirstDayOfWeek()) % 7;
			//calendarComponent.setVisibleDaysOfWeek(firstToShow + 1, firstToShow + 5);
			calendarComponent.setFirstVisibleDayOfWeek(2);
			calendarComponent.setLastVisibleDayOfWeek(6);
		} else {
			//calendarComponent.setVisibleDaysOfWeek(1, 7);
			calendarComponent.setFirstVisibleDayOfWeek(1);
			calendarComponent.setLastVisibleDayOfWeek(7);
		}

	}

	/**
	 * Inits the all schedule button.
	 */
	private void initAllScheduleButton() {
		allScheduleButton = new CheckBox("All Scheduled Commands");
		allScheduleButton.setImmediate(true);
		allScheduleButton.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(ValueChangeEvent event) {
				calendarComponent.setReadOnly((Boolean) event.getProperty().getValue());
			}
		});
	}

	/**
	 * Inits the add new event button.
	 */
	public void initAddNewEventButton() {
		addNewEvent = new Button("Add Backup");
		addNewEvent.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = -1L;

			public void buttonClick(ClickEvent event) {
				Date start = getToday();
				showEventPopup(getNewEvent("Backup", "New backup event", start), true);
			}
		});
	}

	/**
	 * Inits the calendar.
	 */
	private void initCalendar() {
		dataSource = new BasicEventProvider();

		calendarComponent = new Calendar(dataSource);
		calendarComponent.setLocale(UI.getCurrent().getLocale());
		calendarComponent.setImmediate(true);

		if (calendarWidth != null || calendarHeight != null) {
			if (calendarHeight != null) {
				calendarComponent.setHeight(calendarHeight);
			}
			if (calendarWidth != null) {
				calendarComponent.setWidth(calendarWidth);
			}
		} else {
			calendarComponent.setSizeFull();
		}

		if (firstHour != null && lastHour != null) {
			calendarComponent.setFirstVisibleHourOfDay(firstHour);
			calendarComponent.setLastVisibleHourOfDay(lastHour);
		}

		if (firstDay != null && lastDay != null) {
			calendarComponent.setFirstVisibleDayOfWeek(firstDay);
			calendarComponent.setLastVisibleDayOfWeek(lastDay);
		}

		Date today = getToday();
		calendar = new GregorianCalendar(UI.getCurrent().getLocale());
		calendar.setTime(today);

		updateCaptionLabel();

		if (!showWeeklyView) {
			int rollAmount = calendar.get(GregorianCalendar.DAY_OF_MONTH) - 1;
			calendar.add(GregorianCalendar.DAY_OF_MONTH, -rollAmount);
			resetTime(false);
			currentMonthsFirstDate = calendar.getTime();
			calendar.add(java.util.Calendar.DAY_OF_WEEK, -(calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1));
			visibleFirstDate = calendar.getTime();
			calendarComponent.setStartDate(visibleFirstDate);
			calendar.add(GregorianCalendar.DAY_OF_YEAR, 34);
			resetTime(true);
			Date lastm = calendar.getTime();
			calendarComponent.setEndDate(calendar.getTime());
		}

		addCalendarEventListeners();
	}

	/**
	 * Gets today.
	 *
	 * @return today
	 */
	private Date getToday() {
		return new Date();
	}

	/**
	 * Adds the calendar event listeners.
	 */
	@SuppressWarnings("serial")
	private void addCalendarEventListeners() {
		// Register week clicks by changing the schedules start and end dates.
		calendarComponent.setHandler(new BasicWeekClickHandler() {

			@Override
			public void weekClick(WeekClick event) {
				// let BasicWeekClickHandler handle calendar dates, and update
				// only the other parts of UI here
				super.weekClick(event);
				updateCaptionLabel();
				switchToWeekView();
			}
		});

		calendarComponent.setHandler(new EventClickHandler() {

			public void eventClick(EventClick event) {
				showEventPopup(event.getCalendarEvent(), false);
			}
		});

		calendarComponent.setHandler(new BasicDateClickHandler() {

			@Override
			public void dateClick(DateClickEvent event) {
				// let BasicDateClickHandler handle calendar dates, and update
				// only the other parts of UI here
				super.dateClick(event);
				switchToDayView();
			}
		});

		calendarComponent.setHandler(new RangeSelectHandler() {

			public void rangeSelect(RangeSelectEvent event) {
				handleRangeSelect(event);
			}
		});
	}

	/**
	 * Creates the time zone select.
	 *
	 * @return the select
	 */
	@SuppressWarnings("deprecation")
	private Select createTimeZoneSelect() {
		Select s = new Select("Timezone");
		s.setNullSelectionAllowed(false);
		s.addContainerProperty("caption", String.class, "");
		s.setItemCaptionPropertyId("caption");
		s.setFilteringMode(Select.FILTERINGMODE_CONTAINS);

		Item i = s.addItem(DEFAULT_ITEMID);
		i.getItemProperty("caption").setValue("Default (" + TimeZone.getDefault().getID() + ")");
		for (String id : TimeZone.getAvailableIDs()) {
			if (!s.containsId(id)) {
				i = s.addItem(id);
				i.getItemProperty("caption").setValue(id);
			}
		}

		s.select(DEFAULT_ITEMID);
		s.setImmediate(true);
		s.addListener(new ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(ValueChangeEvent event) {

				updateCalendarTimeZone(event.getProperty().getValue());
			}
		});

		return s;
	}

	/**
	 * Creates the calendar format select.
	 *
	 * @return the select
	 */
	@SuppressWarnings("deprecation")
	private Select createCalendarFormatSelect() {
		Select s = new Select("Calendar format");
		s.addContainerProperty("caption", String.class, "");
		s.setItemCaptionPropertyId("caption");

		Item i = s.addItem(DEFAULT_ITEMID);
		i.getItemProperty("caption").setValue("Default by locale");
		i = s.addItem(TimeFormat.Format12H);
		i.getItemProperty("caption").setValue("12H");
		i = s.addItem(TimeFormat.Format24H);
		i.getItemProperty("caption").setValue("24H");

		s.select(DEFAULT_ITEMID);
		s.setImmediate(true);
		s.addListener(new ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(ValueChangeEvent event) {
				updateCalendarFormat(event.getProperty().getValue());
			}
		});

		return s;
	}

	/**
	 * Creates the locale select.
	 *
	 * @return the select
	 */
	@SuppressWarnings("deprecation")
	private Select createLocaleSelect() {
		Select s = new Select("Locale");
		s.addContainerProperty("caption", String.class, "");
		s.setItemCaptionPropertyId("caption");
		s.setFilteringMode(Select.FILTERINGMODE_CONTAINS);

		for (Locale l : Locale.getAvailableLocales()) {
			if (!s.containsId(l)) {
				Item i = s.addItem(l);
				i.getItemProperty("caption").setValue(getLocaleItemCaption(l));
			}
		}

		s.select(UI.getCurrent().getLocale());
		s.setImmediate(true);
		s.addListener(new ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(ValueChangeEvent event) {
				updateCalendarLocale((Locale) event.getProperty().getValue());
			}
		});

		return s;
	}

	/**
	 * Update calendar time zone.
	 *
	 * @param timezoneId the timezone id
	 */
	private void updateCalendarTimeZone(Object timezoneId) {
		TimeZone tz = null;
		if (!DEFAULT_ITEMID.equals(timezoneId)) {
			tz = TimeZone.getTimeZone((String) timezoneId);
		}

		// remember the week that was showing, so we can re-set it later
		Date startDate = calendarComponent.getStartDate();
		calendar.setTime(startDate);
		int weekNumber = calendar.get(java.util.Calendar.WEEK_OF_YEAR);
		calendarComponent.setTimeZone(tz);
		calendar.setTimeZone(calendarComponent.getTimeZone());

		if (viewMode == Mode.WEEK) {
			calendar.set(java.util.Calendar.WEEK_OF_YEAR, weekNumber);
			calendar.set(java.util.Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());

			calendarComponent.setStartDate(calendar.getTime());
			calendar.add(java.util.Calendar.DATE, 6);
			calendarComponent.setEndDate(calendar.getTime());
		} else if (viewMode == Mode.MONTH) {
			rollMonth(0);
		}

	}

	/**
	 * Update calendar format.
	 *
	 * @param format the format
	 */
	private void updateCalendarFormat(Object format) {
		TimeFormat calFormat = null;
		if (format instanceof TimeFormat) {
			calFormat = (TimeFormat) format;
		}

		calendarComponent.setTimeFormat(calFormat);
	}

	/**
	 * Gets the locale item caption.
	 *
	 * @param l the locale
	 * @return the locale item caption
	 */
	private String getLocaleItemCaption(Locale l) {
		String country = l.getDisplayCountry(UI.getCurrent().getLocale());
		String language = l.getDisplayLanguage(UI.getCurrent().getLocale());
		StringBuilder caption = new StringBuilder(country);
		if (caption.length() != 0) {
			caption.append(", ");
		}
		caption.append(language);
		return caption.toString();
	}

	/**
	 * Update calendar locale.
	 *
	 * @param l the locale
	 */
	private void updateCalendarLocale(Locale l) {
		int oldFirstDayOfWeek = calendar.getFirstDayOfWeek();
		setLocale(l);
		calendarComponent.setLocale(l);
		calendar = new GregorianCalendar(l);
		int newFirstDayOfWeek = calendar.getFirstDayOfWeek();

		// we are showing 1 week, and the first day of the week has changed
		// update start and end dates so that the same week is showing
		if (viewMode == Mode.WEEK && oldFirstDayOfWeek != newFirstDayOfWeek) {
			calendar.setTime(calendarComponent.getStartDate());
			calendar.add(java.util.Calendar.DAY_OF_WEEK, 2);
			// starting at the beginning of the week
			calendar.set(GregorianCalendar.DAY_OF_WEEK, newFirstDayOfWeek);
			Date start = calendar.getTime();

			// ending at the end of the week
			calendar.add(GregorianCalendar.DATE, 6);
			Date end = calendar.getTime();

			calendarComponent.setStartDate(start);
			calendarComponent.setEndDate(end);

			// Week days depend on locale so this must be refreshed
			setWeekendsHidden(hideWeekendsButton.booleanValue());
		}

	}

	/**
	 * Handle next button click.
	 */
	private void handleNextButtonClick() {
		switch (viewMode) {
		case MONTH:
			nextMonth();
			break;
		case WEEK:
			nextWeek();
			break;
		case DAY:
			nextDay();
			break;
		}

		for (String scheduleID : eventsMap.keySet()) {
			ArrayList<CalendarCustomEvent> eventsList = eventsMap.get(scheduleID);
			for (CalendarCustomEvent removeEvent : eventsList) {
				if (dataSource.containsEvent(removeEvent)) {
					dataSource.removeEvent(removeEvent);
				}
			}
		}
		eventsMap.clear();

		// loop through events from API and add to calendar
		final LinkedHashMap<String, ScheduleRecord> scheduleList = schedule.getScheduleList();
		ListIterator<Map.Entry<String, ScheduleRecord>> iter = new ArrayList<Entry<String, ScheduleRecord>>(scheduleList.entrySet()).listIterator();

		while (iter.hasNext()) {
			Map.Entry<String, ScheduleRecord> entry = iter.next();
			ScheduleRecord scheduleRecord = entry.getValue();

			String iCalString = scheduleRecord.getICal();
			VEvent vEvent = iCalSupport.readVEvent(iCalString);
			addEventsToMap(scheduleRecord.getID(), vEvent, scheduleRecord.getNodeID());

		}

	}

	/**
	 * Handle previous button click.
	 */
	private void handlePreviousButtonClick() {
		switch (viewMode) {
		case MONTH:
			previousMonth();
			break;
		case WEEK:
			previousWeek();
			break;
		case DAY:
			previousDay();
			break;
		}

		for (String scheduleID : eventsMap.keySet()) {
			ArrayList<CalendarCustomEvent> eventsList = eventsMap.get(scheduleID);
			for (CalendarCustomEvent removeEvent : eventsList) {
				if (dataSource.containsEvent(removeEvent)) {
					dataSource.removeEvent(removeEvent);
				}
			}
		}
		eventsMap.clear();

		// loop through events from API and add to calendar
		final LinkedHashMap<String, ScheduleRecord> scheduleList = schedule.getScheduleList();
		ListIterator<Map.Entry<String, ScheduleRecord>> iter = new ArrayList<Entry<String, ScheduleRecord>>(scheduleList.entrySet()).listIterator();

		while (iter.hasNext()) {
			Map.Entry<String, ScheduleRecord> entry = iter.next();
			ScheduleRecord scheduleRecord = entry.getValue();

			String iCalString = scheduleRecord.getICal();
			VEvent vEvent = iCalSupport.readVEvent(iCalString);
			addEventsToMap(scheduleRecord.getID(), vEvent, scheduleRecord.getNodeID());

		}

	}

	/**
	 * Handle range select.
	 *
	 * @param event the event
	 */
	private void handleRangeSelect(RangeSelectEvent event) {
		Date start = event.getStart();
		Date end = event.getEnd();

		/*
		 * If a range of dates is selected in monthly mode, we want it to end at
		 * the end of the last day.
		 */
		if (event.isMonthlyMode()) {
			//TODO: LF - fix this 
			//end = Calendar.getEndOfDay(calendar, end);
		}

		showEventPopup(getNewEvent("Backup", "New backup event", start), true);
	}

	/**
	 * Show event popup.
	 *
	 * @param event the event
	 * @param newEvent the new event
	 */
	private void showEventPopup(CalendarEvent event, boolean newEvent) {
		if (event == null) {
			return;
		}

		updateCalendarEventPopup((CalendarCustomEvent) event, newEvent);
		updateCalendarEventForm(event);

		if (!UI.getCurrent().getWindows().contains(scheduleEventPopup)) {
			UI.getCurrent().addWindow(scheduleEventPopup);
		}
	}

	/**
	 * Initializes a modal window to edit schedule event.
	 *
	 * @param event the event
	 * @param newEvent the new event
	 */
	private void createCalendarEventPopup(CalendarCustomEvent event, boolean newEvent) {
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);

		scheduleEventPopup = new Window(null, layout);
		scheduleEventPopup.setWidth("400px");
		scheduleEventPopup.setModal(true);
		scheduleEventPopup.center();

		Date occurrence = event.getOccurrence();
		if (!newEvent && occurrence != null) {
			Form form = new Form();
			form.setCaption("This is a repeat occurrence");
			layout.addComponent(form);

			DateField dateField = new DateField("Occurrence Start");
			if (useSecondResolution) {
				dateField.setResolution(Resolution.SECOND);
			} else {
				dateField.setResolution(Resolution.MINUTE);
			}
			dateField.setValue(event.getStart());
			dateField.setEnabled(false);
			form.addField("dateField", dateField);
			form.setFooter(null);

			HorizontalLayout editLayout = new HorizontalLayout();
			editLayout.setSpacing(true);
			layout.addComponent(editLayout);

			final Label label = new Label("Click to change the original event below:");
			editLayout.addComponent(label);
			editLayout.setComponentAlignment(label, Alignment.BOTTOM_LEFT);

			editOriginalButton = new Button("Edit", new ClickListener() {

				private static final long serialVersionUID = 1L;

				public void buttonClick(ClickEvent clickEvent) {
					scheduleEventForm.setEnabled(true);
					applyEventButton.setEnabled(true);
					label.setValue("Editing original event:");
					editOriginalButton.setVisible(false);
				}
			});
			editLayout.addComponent(editOriginalButton);

			scheduleEventForm.setEnabled(false);
		} else {
			scheduleEventForm.setEnabled(true);
		}

		layout.addComponent(scheduleEventForm);

		applyEventButton = new Button("Add", new ClickListener() {

			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				commitCalendarEvent();
			}
		});
		Button cancel = new Button("Cancel", new ClickListener() {

			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				discardCalendarEvent();
			}
		});
		deleteEventButton = new Button("Delete", new ClickListener() {

			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				deleteCalendarEvent();
			}
		});
		scheduleEventPopup.addListener(new CloseListener() {
			private static final long serialVersionUID = 1L;

			public void windowClose(CloseEvent e) {
				discardCalendarEvent();
			}
		});

		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setSpacing(true);
		buttons.addComponent(deleteEventButton);
		buttons.addComponent(cancel);
		buttons.addComponent(applyEventButton);
		layout.addComponent(buttons);
		layout.setComponentAlignment(buttons, Alignment.BOTTOM_RIGHT);
	}

	/**
	 * Update calendar event popup.
	 *
	 * @param event the event
	 * @param newEvent the new event
	 */
	private void updateCalendarEventPopup(CalendarCustomEvent event, boolean newEvent) {
		createCalendarEventPopup(event, newEvent);

		if (newEvent) {
			scheduleEventPopup.setCaption("Add Backup");
		} else {
			scheduleEventPopup.setCaption("Edit Backup");
		}

		deleteEventButton.setVisible(!newEvent);
		deleteEventButton.setEnabled(!calendarComponent.isReadOnly());
		applyEventButton.setCaption(newEvent ? "Add" : "Save");
		applyEventButton.setEnabled(!calendarComponent.isReadOnly() && event.getOccurrence() == null);
	}

	/**
	 * Update calendar event form.
	 *
	 * @param event the event
	 */
	@SuppressWarnings("deprecation")
	private void updateCalendarEventForm(CalendarEvent event) {
		// Lets create a CalendarEvent BeanItem and pass it to the form's data
		// source.
		BeanItem<CalendarEvent> item = new BeanItem<CalendarEvent>(event);
		scheduleEventForm.setBuffered(true);
		scheduleEventForm.setItemDataSource(item);
		scheduleEventForm.setFormFieldFactory(new FormFieldFactory() {

			private static final long serialVersionUID = 1L;

			public Field createField(Item item, Object propertyId, Component uiContext) {
				if (propertyId.equals("caption")) {
					TextField f = createTextField("Caption");
					f.focus();
					return f;

				} else if (propertyId.equals("description")) {
					TextField f = createTextField("Description");
					//f.setRows(3);
					return f;

				} else if (propertyId.equals("styleName")) {
					return createStyleNameSelect();

				} else if (propertyId.equals("start")) {
					startDateField = createDateField("Start");
					return startDateField;

				} else if (propertyId.equals("end")) {
					return createDateField("End");

				} else if (propertyId.equals("allDay")) {
					CheckBox cb = createCheckBox("All-day");

					cb.addListener(new Property.ValueChangeListener() {

						private static final long serialVersionUID = -7104996493482558021L;

						public void valueChange(ValueChangeEvent event) {
							Object value = event.getProperty().getValue();
							if (value instanceof Boolean && Boolean.TRUE.equals(value)) {
								setFormDateResolution(Resolution.DAY);

							} else {
								setFormDateResolution(Resolution.MINUTE);
							}
						}

					});
					return cb;

				} else if (propertyId.equals("repeat")) {
					repeatSelectField = createRepeatSelect();
					repeatSelectField.addValueChangeListener(new ValueChangeListener() {
						private static final long serialVersionUID = 1L;

						public void valueChange(ValueChangeEvent event) {
							if (untilSelectField != null) {
								boolean isRepeat = !(event.getProperty().getValue().equals(CalendarCustomEvent.RECUR_NONE));
								untilSelectField.setVisible(isRepeat);
								untilSelectField.markAsDirty();
							}
						}
					});
					return repeatSelectField;

				} else if (propertyId.equals("untilSelect")) {
					untilSelectField = createUntilSelect();
					if (repeatSelectField != null && repeatSelectField.getValue().equals(CalendarCustomEvent.RECUR_NONE)) {
						untilSelectField.setVisible(false);
					}
					untilSelectField.addValueChangeListener(new ValueChangeListener() {
						private static final long serialVersionUID = 1L;

						public void valueChange(ValueChangeEvent event) {
							Until until = Until.valueOf((String) event.getProperty().getValue());
							boolean untilCount = false;
							boolean untilDate = false;
							switch (until) {
							case never:
								break;
							case count:
								untilCount = true;
								break;
							case date:
								untilDate = true;
								break;
							}

							if (untilCountField != null) {
								untilCountField.setVisible(untilCount);
								untilCountField.markAsDirty();
							}
							if (untilDateField != null) {
								untilDateField.setVisible(untilDate);
								untilDateField.markAsDirty();

							}

						}
					});
					return untilSelectField;

				} else if (propertyId.equals("untilCount")) {
					untilCountField = createTextField("Times");
					if (!untilSelectField.getValue().equals(Until.count.name())) {
						untilCountField.setVisible(false);
					}
					return untilCountField;

				} else if (propertyId.equals("untilDate")) {
					untilDateField = createDateField("On Date");
					if (!untilSelectField.getValue().equals(Until.date.name())) {
						untilDateField.setVisible(false);
					}
					return untilDateField;

				} else if (propertyId.equals("node")) {
					NativeSelect ns = createNodeSelect();
					ns.addValueChangeListener(new ValueChangeListener() {
						private static final long serialVersionUID = 1L;

						public void valueChange(ValueChangeEvent event) {
							String nodeID = ((String) event.getProperty().getValue());
						}
					});
					return ns;
				}
				return null;
			}

			private CheckBox createCheckBox(String caption) {
				CheckBox cb = new CheckBox(caption);
				cb.setImmediate(true);
				return cb;
			}

			private TextField createTextField(String caption) {
				TextField f = new TextField(caption);
				f.setNullRepresentation("");
				return f;
			}

			private DateField createDateField(String caption) {
				DateField f = new DateField(caption);
				if (useSecondResolution) {
					f.setResolution(Resolution.SECOND);
				} else {
					f.setResolution(Resolution.MINUTE);
				}
				return f;
			}

			private NativeSelect createStyleNameSelect() {
				NativeSelect s = new NativeSelect("Color");
				s.addContainerProperty("c", String.class, "");
				s.setItemCaptionPropertyId("c");
				Item i = s.addItem("color1");
				i.getItemProperty("c").setValue("Green");
				i = s.addItem("color2");
				i.getItemProperty("c").setValue("Blue");
				i = s.addItem("color3");
				i.getItemProperty("c").setValue("Red");
				i = s.addItem("color4");
				i.getItemProperty("c").setValue("Orange");
				return s;
			}

			private NativeSelect createRepeatSelect() {
				NativeSelect s = new NativeSelect("Repeat");
				s.setImmediate(true);
				s.setNullSelectionAllowed(false);
				s.addContainerProperty("r", String.class, "");
				s.setItemCaptionPropertyId("r");
				Item i = s.addItem(CalendarCustomEvent.RECUR_NONE);
				i.getItemProperty("r").setValue("None");
				i = s.addItem(Recur.HOURLY);
				i.getItemProperty("r").setValue("Every Hour");
				i = s.addItem(Recur.DAILY);
				i.getItemProperty("r").setValue("Every Day");
				i = s.addItem(Recur.WEEKLY);
				i.getItemProperty("r").setValue("Every Week");
				i = s.addItem(Recur.MONTHLY);
				i.getItemProperty("r").setValue("Every Month");
				i = s.addItem(Recur.YEARLY);
				i.getItemProperty("r").setValue("Every Year");
				return s;
			}

			private NativeSelect createUntilSelect() {
				NativeSelect s = new NativeSelect("End");
				s.setImmediate(true);
				s.setNullSelectionAllowed(false);
				s.addContainerProperty("u", String.class, "");
				s.setItemCaptionPropertyId("u");
				Item i = s.addItem(Until.never.name());
				i.getItemProperty("u").setValue("Never");
				i = s.addItem(Until.count.name());
				i.getItemProperty("u").setValue("After");
				i = s.addItem(Until.date.name());
				i.getItemProperty("u").setValue("On Date");
				return s;
			}

			private NativeSelect createNodeSelect() {
				//Select s = new Select("Node");
				final NativeSelect s = new NativeSelect("Node");
				s.setImmediate(true);
				s.setNullSelectionAllowed(false);
				s.addContainerProperty("n", String.class, "");
				s.setItemCaptionPropertyId("n");
				for (NodeInfo node : nodes) {
					Item i = s.addItem(node.getID());
					i.getItemProperty("n").setValue(node.getName());
				}
				return s;
			}

		});

		scheduleEventForm
				.setVisibleItemProperties(new Object[] { "caption", "description", "node", "start", "repeat", "untilSelect", "untilCount", "untilDate" });

		Date occurrence = ((CalendarCustomEvent) event).getOccurrence();
		if (occurrence != null) {
			startDateField.setValue(occurrence);
		}
	}

	/**
	 * Sets the form date resolution.
	 *
	 * @param resolution the new form date resolution
	 */
	private void setFormDateResolution(Resolution resolution) {
		if (scheduleEventForm.getField("start") != null && scheduleEventForm.getField("end") != null) {
			((DateField) scheduleEventForm.getField("start")).setResolution(resolution);
			((DateField) scheduleEventForm.getField("start")).requestRepaint();
			((DateField) scheduleEventForm.getField("end")).setResolution(resolution);
			((DateField) scheduleEventForm.getField("end")).requestRepaint();
		}
	}

	/**
	 * Show delete popup.
	 *
	 * @param event the event
	 */
	private void showDeletePopup(final CalendarCustomEvent event) {
		if (event == null) {
			return;
		}

		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);

		deleteSchedulePopup = new Window("Delete Recurring Event", layout);
		deleteSchedulePopup.setWidth("740px");
		deleteSchedulePopup.setModal(true);
		deleteSchedulePopup.center();
		deleteSchedulePopup.setContent(layout);
		deleteSchedulePopup.addCloseListener(new CloseListener() {
			private static final long serialVersionUID = 1L;

			public void windowClose(CloseEvent e) {
				UI.getCurrent().removeWindow(deleteSchedulePopup);
			}
		});

		Label warning = new Label("Do you want to delete the original event, or this and all future occurrences of the event, or only the selected occurrence?");
		layout.addComponent(warning);

		Button cancel = new Button("Cancel", new ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				UI.getCurrent().removeWindow(deleteSchedulePopup);
			}
		});

		Button deleteAll = new Button("Delete Original Event", new ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent dummy) {
				String scheduleID = (String) event.getData();
				Schedule.delete(scheduleID);

				schedule.getScheduleList().remove(scheduleID);

				ArrayList<CalendarCustomEvent> eventsList = eventsMap.remove(scheduleID);
				for (CalendarCustomEvent removeEvent : eventsList) {
					if (dataSource.containsEvent(removeEvent)) {
						dataSource.removeEvent(removeEvent);
					}
				}

				UI.getCurrent().removeWindow(deleteSchedulePopup);

				UI.getCurrent().removeWindow(scheduleEventPopup);

			}
		});

		Button deleteFuture = new Button("Delete All Future Events", new ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent dummy) {
				String scheduleID = (String) event.getData();
				ScheduleRecord scheduleRecord = schedule.getScheduleList().get(scheduleID);
				VEvent vEvent = iCalSupport.readVEvent(scheduleRecord.getICal());
				ManagerUI.log("before Delete All Future Events\n" + vEvent);
				iCalSupport.deleteAllFuture(vEvent, event.getStart());
				ManagerUI.log("after Delete All Future Events\n" + vEvent);
				scheduleRecord.setICal(vEvent.toString());

				Schedule.update(scheduleID, vEvent.toString());
				ArrayList<CalendarCustomEvent> eventsList = eventsMap.remove(scheduleID);
				for (CalendarCustomEvent removeEvent : eventsList) {
					if (dataSource.containsEvent(removeEvent)) {
						dataSource.removeEvent(removeEvent);
					}
				}

				schedule.getScheduleList().put(scheduleID, scheduleRecord);

				addEventsToMap(scheduleID, vEvent, event.getNode());
				eventsList = eventsMap.get(scheduleID);
				for (CalendarCustomEvent addEvent : eventsList) {
					if (!dataSource.containsEvent(addEvent)) {
						dataSource.addEvent(addEvent);
					}
				}

				UI.getCurrent().removeWindow(deleteSchedulePopup);

				UI.getCurrent().removeWindow(scheduleEventPopup);

			}
		});

		Button deleteSelected = new Button("Delete Only This Event", new ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent dummy) {
				String scheduleID = (String) event.getData();
				ScheduleRecord scheduleRecord = schedule.getScheduleList().get(scheduleID);
				VEvent vEvent = iCalSupport.readVEvent(scheduleRecord.getICal());
				ManagerUI.log("before Exclude\n" + vEvent);
				iCalSupport.addExcludedDate(vEvent, event.getStart());
				ManagerUI.log("after Exclude\n" + vEvent);
				scheduleRecord.setICal(vEvent.toString());

				Schedule.update(scheduleID, vEvent.toString());
				ArrayList<CalendarCustomEvent> eventsList = eventsMap.remove(scheduleID);
				for (CalendarCustomEvent removeEvent : eventsList) {
					if (dataSource.containsEvent(removeEvent)) {
						dataSource.removeEvent(removeEvent);
					}
				}

				schedule.getScheduleList().put(scheduleID, scheduleRecord);

				addEventsToMap(scheduleID, vEvent, event.getNode());
				eventsList = eventsMap.get(scheduleID);
				for (CalendarCustomEvent addEvent : eventsList) {
					if (!dataSource.containsEvent(addEvent)) {
						dataSource.addEvent(addEvent);
					}
				}

				UI.getCurrent().removeWindow(deleteSchedulePopup);

				UI.getCurrent().removeWindow(scheduleEventPopup);

			}
		});

		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setSpacing(true);
		buttons.addComponent(cancel);
		buttons.addComponent(deleteAll);
		buttons.addComponent(deleteFuture);
		buttons.addComponent(deleteSelected);
		deleteSelected.focus();

		layout.addComponent(buttons);
		layout.setComponentAlignment(buttons, Alignment.BOTTOM_RIGHT);

		if (!UI.getCurrent().getWindows().contains(deleteSchedulePopup)) {
			UI.getCurrent().addWindow(deleteSchedulePopup);
		}
	}

	/**
	 * Removes the event from the data source and fires change event.
	 */
	private void deleteCalendarEvent() {
		CalendarCustomEvent event = (CalendarCustomEvent) getFormCalendarEvent();

		if (event.getRepeat().equals(CalendarCustomEvent.RECUR_NONE)) {
			String scheduleID = (String) event.getData();
			Schedule.delete(scheduleID);

			schedule.getScheduleList().remove(scheduleID);

			ArrayList<CalendarCustomEvent> eventsList = eventsMap.remove(scheduleID);
			for (CalendarCustomEvent removeEvent : eventsList) {
				if (dataSource.containsEvent(removeEvent)) {
					dataSource.removeEvent(removeEvent);
				}
			}

			UI.getCurrent().removeWindow(scheduleEventPopup);
		} else {
			showDeletePopup(event);
		}
	}

	/**
	 * Adds/updates the event in the data source and fires change event.
	 */
	private void commitCalendarEvent() {
		scheduleEventForm.commit();
		CalendarCustomEvent event = (CalendarCustomEvent) getFormCalendarEvent();
		VEvent vEvent = iCalSupport.createVEvent(event);
		ManagerUI.log("" + vEvent);
		String scheduleID = (String) event.getData();
		ScheduleRecord scheduleRecord;
		if (scheduleID == null) {
			ClusterComponent systemRecord = VaadinSession.getCurrent().getAttribute(ClusterComponent.class);
			UserObject userObject = VaadinSession.getCurrent().getAttribute(UserObject.class);
			Map<String, String> params = new HashMap<String, String>();
			params.put(ParametersLayout.PARAM_BACKUP_TYPE, BackupRecord.BACKUP_TYPE_FULL);
			Schedule schedule = new Schedule(systemRecord.getID(), event.getNode(), userObject.getUserID(), "backup", params, null, vEvent.toString());
			scheduleRecord = schedule.getScheduleList().entrySet().iterator().next().getValue();
			scheduleID = scheduleRecord.getID();
			event.setData(scheduleID);
			// Generate a UID for the event..
			UidGenerator ug;
			try {
				ug = new UidGenerator(scheduleID);
				Uid uid = ug.generateUid();
				// override 'proper' UID with just scheduleID for now
				uid.setValue(scheduleID);
				vEvent.getProperties().add(uid);
			} catch (SocketException e) {
				ManagerUI.error(e.getMessage());
			}

		} else {
			scheduleRecord = schedule.getScheduleList().get(scheduleID);
			scheduleRecord.setICal(vEvent.toString());
			Schedule.update(scheduleID, vEvent.toString());
			ArrayList<CalendarCustomEvent> eventsList = eventsMap.remove(scheduleID);
			for (CalendarCustomEvent removeEvent : eventsList) {
				if (dataSource.containsEvent(removeEvent)) {
					dataSource.removeEvent(removeEvent);
				}
			}

		}

		schedule.getScheduleList().put(scheduleID, scheduleRecord);

		addEventsToMap(scheduleID, vEvent, event.getNode());
		ArrayList<CalendarCustomEvent> eventsList = eventsMap.get(scheduleID);
		for (CalendarCustomEvent addEvent : eventsList) {
			if (!dataSource.containsEvent(addEvent)) {
				dataSource.addEvent(addEvent);
			}
		}

		UI.getCurrent().removeWindow(scheduleEventPopup);
	}

	/**
	 * Discard calendar event.
	 */
	private void discardCalendarEvent() {
		scheduleEventForm.discard();
		UI.getCurrent().removeWindow(scheduleEventPopup);
	}

	/**
	 * Gets the form calendar event.
	 *
	 * @return the form calendar event
	 */
	@SuppressWarnings("unchecked")
	private BasicEvent getFormCalendarEvent() {
		BeanItem<CalendarEvent> item = (BeanItem<CalendarEvent>) scheduleEventForm.getItemDataSource();
		CalendarEvent event = item.getBean();
		return (BasicEvent) event;
	}

	/**
	 * Next month.
	 */
	private void nextMonth() {
		rollMonth(1);
	}

	/**
	 * Previous month.
	 */
	private void previousMonth() {
		rollMonth(-1);
	}

	/**
	 * Next week.
	 */
	private void nextWeek() {
		rollWeek(1);
	}

	/**
	 * Previous week.
	 */
	private void previousWeek() {
		rollWeek(-1);
	}

	/**
	 * Next day.
	 */
	private void nextDay() {
		rollDate(1);
	}

	/**
	 * Previous day.
	 */
	private void previousDay() {
		rollDate(-1);
	}

	/**
	 * Roll month.
	 *
	 * @param direction the direction
	 */
	private void rollMonth(int direction) {
		calendar.setTime(currentMonthsFirstDate);
		calendar.add(GregorianCalendar.MONTH, direction);
		resetTime(false);
		currentMonthsFirstDate = calendar.getTime();
		updateCaptionLabel();
		calendar.add(java.util.Calendar.DAY_OF_WEEK, -(calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1));
		visibleFirstDate = calendar.getTime();
		calendarComponent.setStartDate(visibleFirstDate);
		calendar.add(GregorianCalendar.DAY_OF_YEAR, 34);
		resetCalendarTime(true);
	}

	/**
	 * Roll week.
	 *
	 * @param direction the direction
	 */
	private void rollWeek(int direction) {
		calendar.add(GregorianCalendar.WEEK_OF_YEAR, direction);
		calendar.set(GregorianCalendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
		resetCalendarTime(false);
		resetTime(true);
		calendar.add(GregorianCalendar.DATE, 6);
		calendarComponent.setEndDate(calendar.getTime());
	}

	/**
	 * Roll date.
	 *
	 * @param direction the direction
	 */
	private void rollDate(int direction) {
		calendar.add(GregorianCalendar.DATE, direction);
		resetCalendarTime(false);
		resetCalendarTime(true);
	}

	/**
	 * Update caption label.
	 */
	private void updateCaptionLabel() {
		DateFormatSymbols s = new DateFormatSymbols(UI.getCurrent().getLocale());
		String month = s.getShortMonths()[calendar.get(GregorianCalendar.MONTH)];
		captionLabel.setValue(month + " " + calendar.get(GregorianCalendar.YEAR));
	}

	/**
	 * Gets the new event.
	 *
	 * @param caption the caption
	 * @param description the description
	 * @param start the start
	 * @return the new event
	 */
	private CalendarCustomEvent getNewEvent(String caption, String description, Date start) {
		return getNewEvent(caption, description, start, null, null, nodes.get(0).getID());
	}

	/**
	 * Gets the new event.
	 *
	 * @param caption the caption
	 * @param description the description
	 * @param start the start
	 * @param end the end
	 * @param repeat the repeat
	 * @param node the node
	 * @return the new event
	 */
	private CalendarCustomEvent getNewEvent(String caption, String description, Date start, Date end, String repeat, String node) {
		CalendarCustomEvent event = new CalendarCustomEvent();
		event.setCaption(caption);
		event.setDescription(description);
		event.setStart(start);
		java.util.Calendar cal = java.util.Calendar.getInstance();
		cal.setTime(start);
		cal.add(java.util.Calendar.HOUR_OF_DAY, 1);
		//event.setEnd(cal.getTime());
		event.setEnd(end);
		iCalSupport.parseRepeat(repeat, event);
		event.setNode(node);
		//event.setOccurrence(occurrence);
		//event.setStyleName("color1");

		return event;
	}

	/**
	 * Switch the view to week view.
	 */
	public void switchToWeekView() {
		viewMode = Mode.WEEK;
		weekButton.setVisible(false);
		monthButton.setVisible(true);
	}

	/**
	 * Switch the Calendar component's start and end date range to the target
	 * month only. (sample range: 01.01.2010 00:00.000 - 31.01.2010 23:59.999)
	 */
	public void switchToMonthView() {
		viewMode = Mode.MONTH;
		monthButton.setVisible(false);
		weekButton.setVisible(false);

		calendar.setTime(currentMonthsFirstDate);
		updateCaptionLabel();
		calendar.add(java.util.Calendar.DAY_OF_WEEK, -(calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1));
		visibleFirstDate = calendar.getTime();
		calendarComponent.setStartDate(visibleFirstDate);
		calendar.add(GregorianCalendar.DAY_OF_YEAR, 34);
		resetCalendarTime(true);

	}

	/**
	 * Switch to day view (week view with a single day visible).
	 */
	public void switchToDayView() {
		viewMode = Mode.DAY;
		monthButton.setVisible(true);
		weekButton.setVisible(true);
	}

	/**
	 * Reset calendar time.
	 *
	 * @param resetEndTime the reset end time
	 */
	private void resetCalendarTime(boolean resetEndTime) {
		Date first = calendarComponent.getStartDate();
		Date last = calendarComponent.getEndDate();
		resetTime(resetEndTime);
		if (resetEndTime) {
			calendarComponent.setEndDate(calendar.getTime());
		} else {
			calendarComponent.setStartDate(calendar.getTime());
			updateCaptionLabel();
		}
	}

	/**
	 * Resets the calendar time (hour, minute second and millisecond) either to
	 * zero or maximum value.
	 *
	 * @param max the max value
	 */
	private void resetTime(boolean max) {
		if (max) {
			calendar.set(GregorianCalendar.HOUR_OF_DAY, calendar.getMaximum(GregorianCalendar.HOUR_OF_DAY));
			calendar.set(GregorianCalendar.MINUTE, calendar.getMaximum(GregorianCalendar.MINUTE));
			calendar.set(GregorianCalendar.SECOND, calendar.getMaximum(GregorianCalendar.SECOND));
			calendar.set(GregorianCalendar.MILLISECOND, calendar.getMaximum(GregorianCalendar.MILLISECOND));
		} else {
			calendar.set(GregorianCalendar.HOUR_OF_DAY, 0);
			calendar.set(GregorianCalendar.MINUTE, 0);
			calendar.set(GregorianCalendar.SECOND, 0);
			calendar.set(GregorianCalendar.MILLISECOND, 0);
		}
	}

}
