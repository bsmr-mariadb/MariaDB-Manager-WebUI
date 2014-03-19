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

import java.util.Date;

import com.vaadin.ui.components.calendar.event.BasicEvent;

public class CalendarCustomEvent extends BasicEvent {
	private static final long serialVersionUID = -1L;

	public static final String RECUR_NONE = "none";

	private String repeat;
	private String untilSelect;
	private String untilCount;
	private Date untilDate;
	private Date occurrence;
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

	public Date getOccurrence() {
		return occurrence;
	}

	public void setOccurrence(Date occurrence) {
		this.occurrence = occurrence;
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
