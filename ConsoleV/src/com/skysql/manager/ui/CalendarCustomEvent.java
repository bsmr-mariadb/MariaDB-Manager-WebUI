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

/**
 * The Class CalendarCustomEvent.
 */
public class CalendarCustomEvent extends BasicEvent {

	public static final String RECUR_NONE = "none";
	private static final long serialVersionUID = -1L;

	private String repeat;
	private String untilSelect;
	private String untilCount;
	private Date untilDate;
	private Date occurrence;
	private String node;
	private Object data;

	/**
	 * Gets the repeat.
	 *
	 * @return the repeat
	 */
	public String getRepeat() {
		return repeat;
	}

	/**
	 * Sets the repeat.
	 *
	 * @param repeat the new repeat
	 */
	public void setRepeat(String repeat) {
		this.repeat = repeat;
		fireEventChange();
	}

	/**
	 * Gets the until select.
	 *
	 * @return the until select
	 */
	public String getUntilSelect() {
		return untilSelect;
	}

	/**
	 * Sets the until select.
	 *
	 * @param untilSelect the new until select
	 */
	public void setUntilSelect(String untilSelect) {
		this.untilSelect = untilSelect;
		fireEventChange();
	}

	/**
	 * Gets the until count.
	 *
	 * @return the until count
	 */
	public String getUntilCount() {
		return untilCount;
	}

	/**
	 * Sets the until count.
	 *
	 * @param untilCount the new until count
	 */
	public void setUntilCount(String untilCount) {
		this.untilCount = untilCount;
		fireEventChange();
	}

	/**
	 * Gets the until date.
	 *
	 * @return the until date
	 */
	public Date getUntilDate() {
		return untilDate;
	}

	/**
	 * Sets the until date.
	 *
	 * @param untilDate the new until date
	 */
	public void setUntilDate(Date untilDate) {
		this.untilDate = untilDate;
		fireEventChange();
	}

	/**
	 * Gets the occurrence.
	 *
	 * @return the occurrence
	 */
	public Date getOccurrence() {
		return occurrence;
	}

	/**
	 * Sets the occurrence.
	 *
	 * @param occurrence the new occurrence
	 */
	public void setOccurrence(Date occurrence) {
		this.occurrence = occurrence;
		fireEventChange();
	}

	/**
	 * Gets the node.
	 *
	 * @return the node
	 */
	public String getNode() {
		return node;
	}

	/**
	 * Sets the node.
	 *
	 * @param node the new node
	 */
	public void setNode(String node) {
		this.node = node;
		fireEventChange();
	}

	/**
	 * Gets the data.
	 *
	 * @return the data
	 */
	public Object getData() {
		return data;
	}

	/**
	 * Sets the data.
	 *
	 * @param data the new data
	 */
	public void setData(Object data) {
		this.data = data;
		fireEventChange();
	}
}
