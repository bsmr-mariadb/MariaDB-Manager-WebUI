package com.skysql.manager.ui;

import java.util.Date;

import com.vaadin.ui.components.calendar.event.BasicEvent;

/**
 * Test CalendarEvent implementation.
 * 
 * @see com.vaadin.addon.calendar.test.ui.Calendar.Event
 */
public class CalendarCustomEvent extends BasicEvent {
	private static final long serialVersionUID = -1L;

	public static final String RECUR_NONE = "none";

	private String repeat;
	private String untilSelect;
	private String untilCount;
	private Date untilDate;
	private String node;
	private Object data;

	public String getRepeat() {
		return repeat;
	}

	public void setRepeat(String repeat) {
		this.repeat = repeat;
		fireEventChange();
	}

	public String getUntilSelect() {
		return untilSelect;
	}

	public void setUntilSelect(String untilSelect) {
		this.untilSelect = untilSelect;
		fireEventChange();
	}

	public String getUntilCount() {
		return untilCount;
	}

	public void setUntilCount(String untilCount) {
		this.untilCount = untilCount;
		fireEventChange();
	}

	public Date getUntilDate() {
		return untilDate;
	}

	public void setUntilDate(Date untilDate) {
		this.untilDate = untilDate;
		fireEventChange();
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
		fireEventChange();
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
		fireEventChange();
	}
}
