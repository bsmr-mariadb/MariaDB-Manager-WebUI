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

package com.skysql.consolev.ui;

import java.awt.Color;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;

import com.skysql.consolev.MonitorRecord;
import com.skysql.consolev.api.ClusterComponent;
import com.skysql.consolev.api.MonitorData2;
import com.skysql.consolev.api.Monitors;
import com.skysql.consolev.api.NodeInfo;
import com.skysql.consolev.api.SystemInfo;
import com.vaadin.addon.timeline.Timeline;
import com.vaadin.addon.timeline.Timeline.ChartMode;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

public class TimelineLayout extends VerticalLayout {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	public static final String MAX_TIMESPAN = "31536000"; // 1 year in seconds
	public static final Color[] colorArray = { new Color(0x00, 0xb4, 0xf0), new Color(0xee, 0x7c, 0x08), new Color(0x40, 0xb5, 0x27),
			new Color(0x00, 0x7E, 0xA8), new Color(0xA7, 0x57, 0x06), new Color(0x2D, 0x7F, 0x1B), new Color(0x66, 0x33, 0x00), new Color(0xCC, 0x00, 0x66),
			new Color(0x99, 0x66, 0xFF), new Color(0x99, 0x66, 0x00) };

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private Timeline timeline;
	private LinkedHashMap<MonitorRecord, IndexedContainer> containers;
	private String name;
	private Calendar latestCal;

	public TimelineLayout(String name, ArrayList<String> monitorIDs) {
		this.name = name;

		setSpacing(true);
		initializeContainers(monitorIDs);
	}

	private void initializeContainers(ArrayList<String> monitorIDs) {

		ClusterComponent componentInfo = VaadinSession.getCurrent().getAttribute(ClusterComponent.class);

		String systemID, nodeID;
		switch (componentInfo.getType()) {
		case system:
			systemID = componentInfo.getID();
			nodeID = SystemInfo.SYSTEM_NODEID;
			break;

		case node:
			systemID = ((NodeInfo) componentInfo).getSystemID();
			nodeID = componentInfo.getID();
			break;

		default:
			return;
		}

		containers = new LinkedHashMap<MonitorRecord, IndexedContainer>(monitorIDs.size());
		for (String monitorID : monitorIDs) {
			MonitorRecord monitor = Monitors.getMonitor(monitorID);
			IndexedContainer container = createIndexedContainer();
			MonitorData2 monitorData = new MonitorData2(monitor, systemID, nodeID, null, MAX_TIMESPAN);
			monitorData.fillDataSource(container);
			containers.put(monitor, container);

			String latestTime = monitorData.getLatestTime();
			Calendar cal = Calendar.getInstance();
			try {
				cal.setTime(sdf.parse(latestTime));
				if (latestCal == null || cal.after(latestCal)) {
					latestCal = cal;
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	/**
	 * Creates an indexed container with two properties: value and timestamp.
	 * 
	 * @return a container with "value, timestamp" items.
	 */
	private IndexedContainer createIndexedContainer() {
		IndexedContainer container = new IndexedContainer();
		container.addContainerProperty(Timeline.PropertyId.VALUE, Float.class, new Float(0));
		container.addContainerProperty(Timeline.PropertyId.TIMESTAMP, java.util.Date.class, null);
		return container;
	}

	private Component createTimeline() {
		timeline = new Timeline(name);
		timeline.setSizeFull();
		timeline.setId("timeline");

		timeline.setChartMode(ChartMode.BAR);
		timeline.setVerticalAxisRange(null, null);
		timeline.setVerticalGridLines(null);

		// Set the zoom levels
		timeline.addZoomLevel("1d", 86400000L);
		timeline.addZoomLevel("7d", 7 * 86400000L);
		timeline.addZoomLevel("1m", 2629743830L);
		timeline.addZoomLevel("3m", 3 * 2629743830L);
		timeline.addZoomLevel("6m", 6 * 2629743830L);
		timeline.addZoomLevel("1y", 31556926000L);
		timeline.addZoomLevel("5y", 5 * 31556926000L);
		// 10 years should suffice here ;=)
		timeline.addZoomLevel("Max", 10 * 31556926000L);

		// Add data sources
		int i = 0;
		for (MonitorRecord monitor : containers.keySet()) {
			IndexedContainer container = containers.get(monitor);
			timeline.addGraphDataSource(container, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
			timeline.setGraphCaption(container, monitor.getName());
			timeline.setGraphOutlineColor(container, colorArray[i++]);
			timeline.setGraphFillColor(container, null);
			timeline.setVerticalAxisLegendUnit(container, monitor.getUnit());
			if (i > colorArray.length) {
				i = 0; // reuse colors
			}
		}

		// Set the date range
		Date endTime = latestCal.getTime();
		latestCal.add(Calendar.MONTH, -1);
		Date startTime = latestCal.getTime();
		timeline.setVisibleDateRange(startTime, endTime);
		return timeline;
	}

	public Component getTimeLine() {
		createTimeline();
		return timeline;
	}
}
