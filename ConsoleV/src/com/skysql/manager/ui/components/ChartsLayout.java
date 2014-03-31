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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import com.skysql.manager.ChartMappings;
import com.skysql.manager.ClusterComponent;
import com.skysql.manager.ManagerUI;
import com.skysql.manager.MonitorRecord;
import com.skysql.manager.UserChart;
import com.skysql.manager.api.ChartProperties;
import com.skysql.manager.api.MonitorData;
import com.skysql.manager.api.Monitors;
import com.skysql.manager.api.SystemInfo;
import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.HorizontalAlign;
import com.vaadin.addon.charts.model.Labels;
import com.vaadin.addon.charts.model.ListSeries;
import com.vaadin.addon.charts.model.RangeSeries;
import com.vaadin.addon.charts.model.Series;
import com.vaadin.addon.charts.model.XAxis;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.dd.HorizontalDropLocation;
import com.vaadin.ui.Component;

import fi.jasoft.dragdroplayouts.DDCssLayout;
import fi.jasoft.dragdroplayouts.client.ui.Constants;
import fi.jasoft.dragdroplayouts.drophandlers.DefaultCssLayoutDropHandler;
import fi.jasoft.dragdroplayouts.events.LayoutBoundTransferable;
import fi.jasoft.dragdroplayouts.interfaces.DragFilter;

/**
 * The Class ChartsLayout.
 */
public class ChartsLayout extends DDCssLayout {

	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private boolean isChartsEditing;
	private ChartProperties chartProperties;
	private String systemType;
	private String time, interval;

	/**
	 * Instantiates a new charts layout.
	 *
	 * @param previewMode the preview mode
	 * @param time the time
	 * @param interval the interval
	 */
	public ChartsLayout(boolean previewMode, String time, String interval) {
		this.time = time;
		this.interval = interval;

		//		addStyleName("chartsArray");
		//		addStyleName("no-vertical-drag-hints");
		setSizeFull();
		setHeight(null);
		setShim(false);

		if (previewMode != true) {
			setDropHandler(new CustomCssLayoutDropHandler());

			// Only allow dragging Charts
			setDragFilter(new DragFilter() {
				private static final long serialVersionUID = 0x4C656F6E6172646FL;

				public boolean isDraggable(Component component) {
					return component instanceof ChartButton;
				}
			});

		}

	}

	/**
	 * Gets the time.
	 *
	 * @return the time
	 */
	public String getTime() {
		return time;
	}

	/**
	 * Gets the interval.
	 *
	 * @return the interval
	 */
	public String getInterval() {
		return interval;
	}

	/**
	 * Initialize charts.
	 *
	 * @param chartProperties the chart properties
	 * @param systemType the system type
	 * @return true, if successful
	 */
	public boolean initializeCharts(ChartProperties chartProperties, String systemType) {
		this.chartProperties = chartProperties;
		this.systemType = systemType;

		ArrayList<ChartMappings> chartMappings = chartProperties.getChartMappings(systemType);
		if (chartMappings != null) {
			boolean isDirty = false;
			for (ChartMappings chartMapping : chartMappings.toArray(new ChartMappings[chartMappings.size()])) {
				isDirty |= stripMissingMonitors(chartMapping);
				if (!chartMapping.getMonitorIDs().isEmpty()) {
					UserChart userChart = new UserChart(chartMapping);
					ChartButton chartButton = new ChartButton(userChart);
					chartButton.setChartsLayout(this);
					addComponent(chartButton);
				} else {
					chartMappings.remove(chartMapping);
				}
			}
			if (isDirty) {
				chartProperties.setChartMappings(systemType, chartMappings);
			}
			return isDirty;
		}

		return false;

	}

	/**
	 * Strip missing monitors.
	 *
	 * @param chartMapping the chart mapping
	 * @return true, if successful
	 */
	private boolean stripMissingMonitors(ChartMappings chartMapping) {
		boolean isDirty = false;
		ArrayList<String> monitorIDs = chartMapping.getMonitorIDs();
		for (String monitorID : monitorIDs.toArray(new String[monitorIDs.size()])) {
			if (!Monitors.getMonitorsList().containsKey(monitorID)) {
				monitorIDs.remove(monitorID);
				isDirty |= true;
			}
		}

		return isDirty;
	}

	/**
	 * Save charts to properties.
	 */
	private void saveChartsToProperties() {
		ArrayList<ChartMappings> chartMappings = new ArrayList<ChartMappings>();
		Iterator<Component> iter = iterator();
		while (iter.hasNext()) {
			ChartButton chartButton = (ChartButton) iter.next();
			UserChart userChart = (UserChart) chartButton.getData();
			ChartMappings chartMapping = new ChartMappings(userChart);
			chartMappings.add(chartMapping);
		}
		chartProperties.setChartMappings(systemType, chartMappings);
	}

	/**
	 * Delete chart.
	 *
	 * @param chartButton the chart button
	 */
	public void deleteChart(Component chartButton) {
		removeComponent(chartButton);
	}

	/**
	 * Replace chart.
	 *
	 * @param oldChartButton the old chart button
	 * @param userChart the user chart
	 */
	public void replaceChart(ChartButton oldChartButton, UserChart userChart) {

		userChart.clearMonitorData();
		ChartButton newChart = new ChartButton(userChart);
		newChart.setEditable(isChartsEditing);
		replaceComponent(oldChartButton, newChart);

	}

	/**
	 * Sets the editable.
	 *
	 * @param editable the new editable
	 */
	public void setEditable(boolean editable) {
		isChartsEditing = editable;
		VaadinSession.getCurrent().setAttribute("isChartsEditing", isChartsEditing);

		Iterator<Component> iter = iterator();
		while (iter.hasNext()) {
			ChartButton chartButton = (ChartButton) iter.next();
			chartButton.setEditable(editable);
		}

		// when getting out of editable mode, save mappings
		if (editable == false) {
			saveChartsToProperties();
		}
	}

	/**
	 * Hide charts.
	 */
	public void hideCharts() {
		Iterator<Component> iter = iterator();
		while (iter.hasNext()) {

			Component component = iter.next();
			if (component instanceof ChartButton) {
				ChartButton chartButton = (ChartButton) component;
				chartButton.setVisible(false);
			}
		}
	}

	/**
	 * Stop refresh.
	 */
	public void stopRefresh() {
		//		if (updaterThread != null && updaterThread.isAlive()) {
		//			ManagerUI.log(this.getClass().getName() + "Stopping thread: " + updaterThread);
		//			updaterThread.flagged = true;
		//			updaterThread.interrupt();
		//		}
	}

	/**
	 * Refresh.
	 *
	 * @param time the time
	 * @param interval the interval
	 */
	public void refresh(String time, String interval) {
		this.time = time;
		this.interval = interval;

		//		if (isChartsEditing) {
		//			return;
		//		}
		VaadinSession session = getSession();
		if (session == null) {
			session = VaadinSession.getCurrent();
		}
		boolean isChartsEditing2 = (Boolean) session.getAttribute("isChartsEditing");
		if (isChartsEditing2) {
			return;
		}

		ManagerUI.log("ChartsLayout refresh()");

		Iterator<Component> iter = iterator();
		while (iter.hasNext()) {

			Component component = iter.next();
			if (component instanceof ChartButton) {
				ChartButton chartButton = (ChartButton) component;

				UpdaterThread updaterThread = new UpdaterThread(chartButton);
				updaterThread.start();
			}
		}

	}

	/**
	 * The Class UpdaterThread.
	 */
	class UpdaterThread extends Thread {

		/** The chart button. */
		ChartButton chartButton;

		/** The flagged. */
		volatile boolean flagged = false;

		/**
		 * Instantiates a new updater thread.
		 *
		 * @param chartButton the chart button
		 */
		UpdaterThread(ChartButton chartButton) {
			this.chartButton = chartButton;
		}

		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			asynchRefresh(this);
		}
	}

	/**
	 * Asynch refresh.
	 *
	 * @param updaterThread the updater thread
	 */
	private void asynchRefresh(UpdaterThread updaterThread) {

		VaadinSession.getCurrent().setAttribute("ChartsRefresh", true);
		refreshCode(updaterThread.chartButton, updaterThread);
		VaadinSession.getCurrent().setAttribute("ChartsRefresh", false);

	}

	/**
	 * Refresh code.
	 *
	 * @param chartButton the chart button
	 * @param updaterThread the updater thread
	 */
	public void refreshCode(ChartButton chartButton, UpdaterThread updaterThread) {
		String systemID, nodeID;

		VaadinSession session = getSession();
		if (session == null) {
			session = VaadinSession.getCurrent();
		}
		ManagerUI managerUI = session.getAttribute(ManagerUI.class);
		ClusterComponent componentInfo = session.getAttribute(ClusterComponent.class);

		switch (componentInfo.getType()) {
		case system:
			systemID = componentInfo.getID();
			nodeID = SystemInfo.SYSTEM_NODEID;
			break;

		case node:
			systemID = componentInfo.getParentID();
			nodeID = componentInfo.getID();
			break;

		default:
			return;
		}

		final UserChart userChart = (UserChart) chartButton.getData();
		final Chart chart = chartButton.getChart();
		boolean needsRedraw = false;
		String[] timeStamps = null;
		final Configuration configuration = chart.getConfiguration();

		for (String monitorID : userChart.getMonitorIDs()) {

			if (updaterThread != null && updaterThread.flagged) {
				ManagerUI.log(this.getClass().getName() + " - flagged is set before API call");
				return;
			}

			ManagerUI.log("ChartsLayout - redraw loop MonitorID: " + monitorID);
			final MonitorRecord monitor = Monitors.getMonitor(monitorID);
			if (monitor == null) {
				// monitor was removed from the system: skip
				ManagerUI.log("monitor was removed from the system");
				continue;
			}

			MonitorData monitorData = (MonitorData) userChart.getMonitorData(monitor.getID());
			if (monitorData == null) {
				String method;
				if (UserChart.ChartType.LineChart.name().equals(userChart.getType())) {
					method = MonitorData.METHOD_AVG;
				} else if (UserChart.ChartType.AreaChart.name().equals(userChart.getType())) {
					method = MonitorData.METHOD_MINMAX;
				} else {
					continue; // unknown chart type, skip
				}
				monitorData = new MonitorData(monitor, systemID, nodeID, time, interval, userChart.getPoints(), method);
				needsRedraw = true;
			} else if (monitorData.update(systemID, nodeID, time, interval, userChart.getPoints()) == true) {
				// data in chart needs to be updated
				needsRedraw = true;
			} else {
				continue; // no update needed
			}

			if (timeStamps == null) {
				ArrayList<Long> unixTimes = monitorData.getTimeStamps();
				if (unixTimes != null) {
					timeStamps = new String[unixTimes.size()];
					int timeSpacer = unixTimes.size() / 15;
					for (int x = 0; x < unixTimes.size(); x++) {
						timeStamps[x] = (x % timeSpacer != 0) ? "\u00A0" : stampToString(unixTimes.get(x)).substring(11, 16);
					}
				}
			}

			if (updaterThread != null && updaterThread.flagged) {
				ManagerUI.log("ChartsLayout - flagged is set before UI redraw");
				return;
			}

			final MonitorData finalMonitorData = monitorData;
			final String finalMonitorID = monitorID;

			managerUI.access(new Runnable() {
				@Override
				public void run() {
					// Here the UI is locked and can be updated

					ManagerUI.log("ChartsLayout access run() monitorID: " + finalMonitorID);

					if (UserChart.ChartType.LineChart.name().equals(userChart.getType())) {

						ListSeries ls = null, testLS;
						List<Series> lsList = configuration.getSeries();
						Iterator seriesIter = lsList.iterator();
						while (seriesIter.hasNext()) {
							testLS = (ListSeries) seriesIter.next();
							if (testLS.getName().equals(monitor.getName())) {
								ls = testLS;
								break;
							}
						}
						if (ls == null) {
							ls = new ListSeries(monitor.getName());
							configuration.addSeries(ls);
						}

						userChart.setMonitorData(monitor.getID(), finalMonitorData);

						ArrayList<Number> avgList = finalMonitorData.getAvgPoints();
						ls.setData(avgList);

					} else if (UserChart.ChartType.AreaChart.name().equals(userChart.getType())) {

						RangeSeries rs = null, testRS;
						List<Series> lsList = configuration.getSeries();
						Iterator seriesIter = lsList.iterator();
						while (seriesIter.hasNext()) {
							testRS = (RangeSeries) seriesIter.next();
							if (testRS.getName().equals(monitor.getName())) {
								rs = testRS;
								break;
							}
						}
						if (rs == null) {
							rs = new RangeSeries(monitor.getName());
							configuration.addSeries(rs);
						}

						userChart.setMonitorData(monitor.getID(), finalMonitorData);

						ArrayList<Number> minList = finalMonitorData.getMinPoints();
						ArrayList<Number> maxList = finalMonitorData.getMaxPoints();

						if (minList != null && maxList != null && minList.size() > 0 && maxList.size() > 0 && minList.size() == maxList.size()) {
							Object[] minArray = finalMonitorData.getMinPoints().toArray();
							Object[] maxArray = finalMonitorData.getMaxPoints().toArray();

							Number[][] dataList = new Number[minList.size()][2];
							for (int x = 0; x < minList.size(); x++) {
								dataList[x][0] = (Number) minArray[x];
								dataList[x][1] = (Number) maxArray[x];
							}

							rs.setRangeData(dataList);
						} else {
							rs.setRangeData(new Number[0][0]);
						}

					}

				}
			});

		} // for

		final boolean finalNeedsRedraw = needsRedraw;
		final String[] finalTimeStamps = timeStamps;
		final ChartButton finalChartButton = chartButton;

		managerUI.access(new Runnable() {
			@Override
			public void run() {
				// Here the UI is locked and can be updated

				ManagerUI.log("ChartsLayout access run() xaxis");

				if (finalNeedsRedraw) {
					ManagerUI.log("ChartsLayout needsRedraw");

					if (finalTimeStamps != null) {
						XAxis xAxis = configuration.getxAxis();
						Labels labels = new Labels();
						labels.setRotation(-45);
						labels.setAlign(HorizontalAlign.RIGHT);
						xAxis.setLabels(labels);
						xAxis.setCategories(finalTimeStamps);
					}

					chart.drawChart(configuration);
					finalChartButton.setVisible(true);
				}

			}
		});

	}

	/**
	 * Stamp to string.
	 *
	 * @param timestamp the timestamp
	 * @return the string
	 */
	private String stampToString(Long timestamp) {

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp * 1000L);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String timeString = sdf.format(cal.getTime());

		return timeString;
	}

	/* (non-Javadoc)
	 * @see fi.jasoft.dragdroplayouts.DDCssLayout#paintContent(com.vaadin.server.PaintTarget)
	 */
	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);

		// Override paintContent (vaadin serialization api) to set the
		// horizontal drop ratio to 50% so that we don't have a "CENTER"
		// position to drop things. See
		// VDDCssLayout#getHorizontalDropLocation() and
		// VDDCssLayout#handleCellDropRatioUpdate() for details
		target.addAttribute(Constants.ATTRIBUTE_HORIZONTAL_DROP_RATIO, 0.5f);
	}

	/**
	 * The Class CustomCssLayoutDropHandler.
	 */
	public static class CustomCssLayoutDropHandler extends DefaultCssLayoutDropHandler {

		/* (non-Javadoc)
		 * @see fi.jasoft.dragdroplayouts.drophandlers.DefaultCssLayoutDropHandler#handleComponentReordering(com.vaadin.event.dd.DragAndDropEvent)
		 */
		@Override
		protected void handleComponentReordering(DragAndDropEvent event) {
			LayoutBoundTransferable transferable = (LayoutBoundTransferable) event.getTransferable();
			CssLayoutTargetDetails details = (CssLayoutTargetDetails) event.getTargetDetails();

			// Get the components
			DDCssLayout layout = (DDCssLayout) details.getTarget();
			Component comp = transferable.getComponent();
			Component over = details.getOverComponent();

			if (over == comp) {
				// If the component and the target are the same, ignore the
				// drag, the component was released "on itself" so no
				// reordering is required.
				return;
			}

			// We are using a CSS layout with float:left; we only care about
			// horizontal positioning
			HorizontalDropLocation horizontalDropLocation = details.getHorizontalDropLocation();

			// Detach - remove current component first, then calculate index.
			layout.removeComponent(comp);
			int indexOfDropTarget = layout.getComponentIndex(over);

			// The layout has the component on top of which the drop occurred
			if (indexOfDropTarget > -1) {

				// If drop location is to the LEFT, add component before
				if (HorizontalDropLocation.LEFT.equals(horizontalDropLocation) || HorizontalDropLocation.CENTER.equals(horizontalDropLocation)) {
					layout.addComponent(comp, indexOfDropTarget);
				} else {

					// If drop target is RIGHT, add after
					indexOfDropTarget++;

					if (indexOfDropTarget < layout.getComponentCount()) {
						layout.addComponent(comp, indexOfDropTarget);
					} else {
						layout.addComponent(comp);
					}
				}

			} else {
				// The current layout doesn't have the component on top of which
				// it was dropped, most likely the drop was on the underlying
				// layout itself. In this case we could look at the vertical
				// drop position to determine if we should add the component at
				// the top or the bottom in the layout.
				layout.addComponent(comp);

				// The else could be left out if we want to force the user to
				// drop the component on another component, i.e. dropping on
				// layout is not supported.
			}
		}
	}

}
