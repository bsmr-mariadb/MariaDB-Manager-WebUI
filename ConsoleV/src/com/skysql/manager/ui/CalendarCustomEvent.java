package com.skysql.manager.ui;

import com.vaadin.ui.components.calendar.event.BasicEvent;

/**
 * Test CalendarEvent implementation.
 * 
 * @see com.vaadin.addon.calendar.test.ui.Calendar.Event
 */
public class CalendarCustomEvent extends BasicEvent {
	private static final long serialVersionUID = -1L;

	private String repeat;
	private String node;
	private Object data;

	public String getRepeat() {
		return repeat;
	}

	public void setRepeat(String repeat) {
		this.repeat = repeat;
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
