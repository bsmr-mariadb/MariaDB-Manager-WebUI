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

package com.skysql.consolev.ui.components;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import com.skysql.consolev.ConsoleUI;
import com.skysql.consolev.DateConversion;
import com.skysql.consolev.MonitorRecord;
import com.skysql.consolev.api.ChartMappings;
import com.skysql.consolev.api.ClusterComponent;
import com.skysql.consolev.api.MonitorData;
import com.skysql.consolev.api.MonitorData2;
import com.skysql.consolev.api.MonitorData3;
import com.skysql.consolev.api.Monitors;
import com.skysql.consolev.api.NodeInfo;
import com.skysql.consolev.api.SystemInfo;
import com.skysql.consolev.api.UserChart;
import com.skysql.consolev.api.UserObject;
import com.skysql.consolev.ui.ChartsDialog;
import com.skysql.consolev.ui.TimelineDialog;
import com.sun.xml.bind.DatatypeConverterImpl;
import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.Axis;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.HorizontalAlign;
import com.vaadin.addon.charts.model.Labels;
import com.vaadin.addon.charts.model.ListSeries;
import com.vaadin.addon.charts.model.PlotOptionsLine;
import com.vaadin.addon.charts.model.RangeSeries;
import com.vaadin.addon.charts.model.Series;
import com.vaadin.addon.charts.model.Title;
import com.vaadin.addon.charts.model.VerticalAlign;
import com.vaadin.addon.charts.model.XAxis;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.dd.HorizontalDropLocation;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;

import fi.jasoft.dragdroplayouts.DDCssLayout;
import fi.jasoft.dragdroplayouts.client.ui.Constants;
import fi.jasoft.dragdroplayouts.drophandlers.DefaultCssLayoutDropHandler;
import fi.jasoft.dragdroplayouts.events.LayoutBoundTransferable;
import fi.jasoft.dragdroplayouts.interfaces.DragFilter;

public class ChartsLayout extends DDCssLayout {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private UserObject userObject;
	private boolean isChartsEditing;
	private ChartsLayout chartsLayout = this;

	public ChartsLayout(boolean previewMode) {

		userObject = VaadinSession.getCurrent().getAttribute(UserObject.class);

		addStyleName("chartsArray");
		addStyleName("no-vertical-drag-hints");
		setSizeFull();
		setHeight(null);
		setShim(false);

		if (previewMode != true) {
			addLayoutClickListener(new LayoutClickListener() {
				private static final long serialVersionUID = 0x4C656F6E6172646FL;

				public void layoutClick(LayoutClickEvent event) {

					Component child;
					if (event.isDoubleClick() && (child = event.getChildComponent()) != null && (child instanceof Chart)) {
						// Get the child component which was double-clicked
						Chart chart = (Chart) child;
						UserChart userChart = (UserChart) chart.getData();
						if (isChartsEditing) {
							new ChartsDialog(chartsLayout, chart, false);
						} else {
							new TimelineDialog(userChart);
						}
					}
				}
			});

			// setDropHandler(new DefaultCssLayoutDropHandler());
			setDropHandler(new Custom2CssLayoutDropHandler());

			// Only allow dragging Charts
			setDragFilter(new DragFilter() {
				private static final long serialVersionUID = 0x4C656F6E6172646FL;

				public boolean isDraggable(Component component) {
					return component instanceof Chart;
				}
			});

		}

	}

	public void initializeCharts() {

		//DatatypeConverterImpl.theInstance);
		DatatypeConverter.setDatatypeConverter(DatatypeConverterImpl.theInstance);

		// attempt to retrieve UserProperties
		String propertyCharts = userObject.getProperty(UserObject.PROPERTY_CHARTS);

		if (propertyCharts == null) {
			// FUTURE: if missing, attempt to retrieve SystemProperties
			//			UserProperties userProperties = new UserProperties(null);
			//			propertyCharts = userProperties.getProperty(UserProperties.PROPERTY_CHARTS);
		}

		if (propertyCharts == null) {
			// if missing, retrieve Monitors and create UserProperties 
			// Setup array of charts objects
			for (MonitorRecord monitor : Monitors.getMonitorsList().values()) {
				ArrayList<String> monitorsForChart = new ArrayList<String>();
				monitorsForChart.add(monitor.getID());
				UserChart userChart = new UserChart(monitor.getName(), monitor.getDescription(), monitor.getUnit(), monitor.getChartType(), 15,
						monitorsForChart);
				initializeChart(userChart);
			}
			try {
				saveChartsToProperties();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			try {
				getChartsFromProperties(propertyCharts);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private void getChartsFromProperties(String encodedMappings) throws IOException, ClassNotFoundException {

		if (encodedMappings != null) {
			ArrayList<ChartMappings> chartMappings = (ArrayList<ChartMappings>) ChartMappings.fromString(encodedMappings);
			for (ChartMappings chartMapping : chartMappings) {
				UserChart userChart = new UserChart(chartMapping);
				initializeChart(userChart);
			}
		}

	}

	private void saveChartsToProperties() throws IOException {

		Iterator<Component> iter = getComponentIterator();
		ArrayList<ChartMappings> chartMappings = new ArrayList<ChartMappings>();
		while (iter.hasNext()) {
			Chart chart = (Chart) iter.next();
			UserChart userChart = (UserChart) chart.getData();
			ChartMappings chartMapping = new ChartMappings(userChart);
			chartMappings.add(chartMapping);
		}

		String encodedMappings = ChartMappings.toString(chartMappings);

		userObject.setProperty(UserObject.PROPERTY_CHARTS, encodedMappings);

	}

	/***
	 * private void getChartsFromProperties(String properties) throws
	 * IOException, ClassNotFoundException {
	 * 
	 * if (properties != null) { String[] encodedUserCharts =
	 * properties.split(","); for (String encodedUserChart : encodedUserCharts)
	 * { ChartMappings chartMappings =
	 * ChartMappings.fromString(encodedUserChart); UserChart userChart = new
	 * UserChart(chartMappings); initializeChart(userChart); } }
	 * 
	 * }
	 * 
	 * private void saveChartsToProperties() throws IOException {
	 * 
	 * StringBuilder sb = new StringBuilder(); Iterator<Component> iter =
	 * getComponentIterator(); while (iter.hasNext()) { Chart chart = (Chart)
	 * iter.next(); UserChart userChart = (UserChart) chart.getData();
	 * ChartMappings chartMappings = new ChartMappings(userChart);
	 * sb.append(ChartMappings.toString(chartMappings)); sb.append(","); } if
	 * (sb.length() > 0) sb.deleteCharAt(sb.length() - 1); // remove last comma,
	 * if there's any content
	 * 
	 * SessionData sessionData =
	 * VaadinSession.getCurrent().getAttribute(SessionData.class);
	 * sessionData.getUserLogin().setProperty(UserProperties.PROPERTY_CHARTS,
	 * sb.toString());
	 * 
	 * }
	 ***/
	public Component initializeChart(UserChart userChart) {

		Component chart = createChart(userChart);
		addComponent(chart);
		return (chart);

	}

	public Component addChart() {

		MonitorRecord monitor = (MonitorRecord) Monitors.getMonitorsList().values().toArray()[0];
		ArrayList<String> monitorsForChart = new ArrayList<String>();
		monitorsForChart.add(monitor.getID());
		UserChart userChart = new UserChart(monitor.getName(), monitor.getDescription(), monitor.getUnit(), monitor.getChartType(), UserChart.COUNT_15,
				monitorsForChart);
		Chart chart = (Chart) initializeChart(userChart);
		chart.setEnabled(false);
		Button deleteButton = new Button("X");
		deleteButton.addStyleName("deleteChart");
		deleteButton.setData(chart);
		userChart.setDeleteButton(deleteButton);
		chart.setData(userChart);
		addComponent(deleteButton);
		deleteButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				Button button = event.getButton();
				removeComponent((Component) button.getData());
				removeComponent(button);
			}
		});
		return (chart);

	}

	public void replaceChart(Chart oldChart, UserChart userChart) {

		userChart.clearMonitorData();
		Chart newChart = createChart(userChart);
		newChart.setEnabled(false);
		Button deleteButton = ((UserChart) oldChart.getData()).getDeleteButton();
		deleteButton.setData(newChart);
		userChart.setDeleteButton(deleteButton);
		newChart.setData(userChart);
		replaceComponent(oldChart, newChart);

	}

	public void disableCharts() {
		isChartsEditing = true;

		Iterator<Component> iter = getComponentIterator();
		while (iter.hasNext()) {
			Component chart = iter.next();
			chart.setEnabled(false);
			// chart.setStyleName(chart.getStyleName() + "-draggable");
		}

		for (int index = getComponentCount(); index > 0; index--) {
			Button deleteButton = new Button("X");
			deleteButton.addStyleName("deleteChart");
			Chart chart = (Chart) getComponent(index - 1);
			deleteButton.setData(chart);
			UserChart userChart = (UserChart) chart.getData();
			userChart.setDeleteButton(deleteButton);
			chart.setData(userChart);
			addComponent(deleteButton, index);
			deleteButton.addClickListener(new Button.ClickListener() {
				private static final long serialVersionUID = 0x4C656F6E6172646FL;

				public void buttonClick(ClickEvent event) {
					Button button = event.getButton();
					removeComponent((Component) button.getData());
					removeComponent(button);
				}
			});
		}
	}

	public void enableCharts() {
		isChartsEditing = false;

		for (int index = getComponentCount() - 1; index >= 0; index--) {
			Component component = getComponent(index);
			if (component instanceof Button) {
				removeComponent(component);
			}
		}
		Iterator<Component> iter = getComponentIterator();
		while (iter.hasNext()) {
			Chart chart = (Chart) iter.next();
			chart.setEnabled(true);
			// String styleName = chart.getStyleName();
			// chart.setStyleName(styleName.substring(0,
			// styleName.indexOf("-draggable")));
		}

		try {
			saveChartsToProperties();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Chart createChart(UserChart userChart) {

		Chart chart = new Chart();
		chart.addStyleName("chartObject");
		chart.setHeight("240px");
		chart.setWidth("400px");
		chart.setData(userChart);

		Configuration configuration = new Configuration();
		ChartType chartType = ChartType.LINE; // default to LINE
		if (UserChart.LINECHART.equalsIgnoreCase(userChart.getType())) {
			chartType = ChartType.LINE;
		} else if (UserChart.AREACHART.equalsIgnoreCase(userChart.getType())) {
			chartType = ChartType.AREARANGE;
		} else if (UserChart.TESTCHART.equalsIgnoreCase(userChart.getType())) {
			chartType = ChartType.LINE;
		}

		configuration.getChart().setType(chartType);
		configuration.getTitle().setText(userChart.getName());
		configuration.getSubTitle().setText(userChart.getDescription());
		configuration.getCredits().setEnabled(false);

		Axis yAxis = configuration.getyAxis();
		yAxis.setTitle(new Title(userChart.getUnit()));
		yAxis.getTitle().setVerticalAlign(VerticalAlign.HIGH);

		// configuration.getTooltip().setFormatter("''+ this.series.name +' '+this.x +': '+ this.y +'C'");

		PlotOptionsLine plotOptions = new PlotOptionsLine();
		plotOptions.setDataLabels(new Labels(true));
		configuration.setPlotOptions(plotOptions);

		chart.drawChart(configuration);

		return (chart);

	}

	private String time, interval, count;

	public void refresh() {
		refresh(time, interval, count);
	}

	public void refresh(String time, String interval, String count) {

		this.time = time;
		this.interval = interval;
		this.count = count;

		String systemID, nodeID;
		ClusterComponent componentInfo = VaadinSession.getCurrent().getAttribute(ClusterComponent.class);

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

		Iterator<Component> iter = getComponentIterator();
		while (iter.hasNext()) {
			Component component = iter.next();
			if (component instanceof Chart) {
				Chart chart = (Chart) component;
				UserChart userChart = (UserChart) chart.getData();
				boolean needsRedraw = false;

				if (UserChart.TESTCHART.equalsIgnoreCase(userChart.getType())) {
					Configuration configuration = chart.getConfiguration();
					String[] timeStamps = null;

					for (String monitorID : userChart.getMonitorIDs()) {
						MonitorRecord monitor = Monitors.getMonitor(monitorID);

						ListSeries ls = null, testLS;
						List<Series> lsList = configuration.getSeries();
						Iterator seriesIter = lsList.iterator();
						while (seriesIter.hasNext()) {
							testLS = (ListSeries) seriesIter.next();
							if (testLS.getName().equalsIgnoreCase(monitor.getName())) {
								ls = testLS;
								break;
							}
						}
						if (ls == null) {
							ls = new ListSeries(monitor.getName());
							configuration.addSeries(ls);
						}

						MonitorData monitorData = (MonitorData) userChart.getMonitorData(monitor.getID());
						if (monitorData == null) {
							monitorData = new MonitorData(monitor, systemID, nodeID, time, interval, count);
							needsRedraw = true;
						} else if (monitorData.update(systemID, nodeID, time, interval, count) == true) {
							// data in chart needs to be updated
							needsRedraw = true;
						} else {
							continue; // no update needed
						}

						userChart.setMonitorData(monitor.getID(), monitorData);

						ArrayList<Number> dataList = new ArrayList<Number>();
						double dataPoints[] = monitorData.getDataPoints();
						if (dataPoints != null) {
							//timeStamps = new String[dataPoints.length];
							for (int x = 0; x < dataPoints.length; x++) {
								//timeStamps[x] = dataPoints[x][0].substring(11, 16);
								dataList.add(Double.valueOf(dataPoints[x]));
							}
						}
						ls.setData(dataList);

					}

					// if (timeStamps != null) {
					// configuration.getxAxis().setCategories(timeStamps);
					// }
					if (needsRedraw) {
						chart.drawChart(configuration);
					}

				} else if (UserChart.AREACHART.equalsIgnoreCase(userChart.getType())) {
					Configuration configuration = chart.getConfiguration();

					for (String monitorID : userChart.getMonitorIDs()) {
						MonitorRecord monitor = Monitors.getMonitor(monitorID);

						RangeSeries rs = null, testRS;
						List<Series> lsList = configuration.getSeries();
						Iterator seriesIter = lsList.iterator();
						while (seriesIter.hasNext()) {
							testRS = (RangeSeries) seriesIter.next();
							if (testRS.getName().equalsIgnoreCase(monitor.getName())) {
								rs = testRS;
								break;
							}
						}
						if (rs == null) {
							rs = new RangeSeries(monitor.getName());
							configuration.addSeries(rs);
						}

						MonitorData3 monitorData = (MonitorData3) userChart.getMonitorData(monitor.getID());
						if (monitorData == null) {
							monitorData = new MonitorData3(monitor, systemID, nodeID, time, interval, count);
							needsRedraw = true;
						} else if (monitorData.update(systemID, nodeID, time, interval, count) == true) {
							// data in chart needs to be updated
							needsRedraw = true;
						} else {
							continue; // no update needed
						}

						userChart.setMonitorData(monitor.getID(), monitorData);

						String dataPoints[][] = monitorData.getDataPoints();
						if (dataPoints != null) {
							Number[][] dataList = new Number[dataPoints.length][2];
							String[] timeStamps = new String[dataPoints.length];
							for (int x = 0; x < dataPoints.length; x++) {
								timeStamps[x] = dataPoints[x][2].substring(11, 16);

								//								dataList[x][0] = (Double.valueOf(dataPoints[x][0]));
								String strValue = dataPoints[x][0];
								double value = Double.valueOf(strValue);
								if (value % 1.0 > 0) {
									int index = strValue.indexOf(".");
									int strlen = strValue.length();
									if (value >= 100.0 || value <= -100.0) {
										strValue = strValue.substring(0, index);
										value = Double.valueOf(strValue);
									} else if (value >= 10.0 || value <= -10.0) {
										strValue = strValue.substring(0, (index + 2) >= strlen ? strlen : index + 2);
										value = Double.valueOf(strValue);
									} else {
										strValue = strValue.substring(0, (index + 3) >= strlen ? strlen : index + 3);
										value = Double.valueOf(strValue);
									}
								}
								dataList[x][0] = value;

								//								dataList[x][1] = (Double.valueOf(dataPoints[x][1]));
								strValue = dataPoints[x][1];
								value = Double.valueOf(strValue);
								if (value % 1.0 > 0) {
									int index = strValue.indexOf(".");
									int strlen = strValue.length();
									if (value >= 100.0 || value <= -100.0) {
										strValue = strValue.substring(0, index);
										value = Double.valueOf(strValue);
									} else if (value >= 10.0 || value <= -10.0) {
										strValue = strValue.substring(0, (index + 2) >= strlen ? strlen : index + 2);
										value = Double.valueOf(strValue);
									} else {
										strValue = strValue.substring(0, (index + 3) >= strlen ? strlen : index + 3);
										value = Double.valueOf(strValue);
									}
								}
								dataList[x][1] = value;

							}
							rs.setRangeData(dataList);
						} else {
							rs.setRangeData(new Number[0][0]);
						}
					}

					// if (timeStamps != null) {
					// configuration.getxAxis().setCategories(timeStamps);
					// }
					if (needsRedraw) {
						chart.drawChart(configuration);
					}

				} else if (UserChart.LINECHART.equalsIgnoreCase(userChart.getType())) {
					Configuration configuration = chart.getConfiguration();

					for (String monitorID : userChart.getMonitorIDs()) {
						MonitorRecord monitor = Monitors.getMonitor(monitorID);

						RangeSeries rs = null, testRS;
						List<Series> lsList = configuration.getSeries();
						Iterator seriesIter = lsList.iterator();
						while (seriesIter.hasNext()) {
							testRS = (RangeSeries) seriesIter.next();
							if (testRS.getName().equalsIgnoreCase(monitor.getName())) {
								rs = testRS;
								break;
							}
						}
						if (rs == null) {
							rs = new RangeSeries(monitor.getName());
							configuration.addSeries(rs);
						}

						MonitorData2 monitorData = (MonitorData2) userChart.getMonitorData(monitor.getID());
						if (monitorData == null) {
							monitorData = new MonitorData2(monitor, systemID, nodeID, time, interval);
							needsRedraw = true;
						} else if (monitorData.update(systemID, nodeID, time, interval) == true) {
							// data in chart needs to be updated
							needsRedraw = true;
						} else {
							continue; // no update needed
						}

						userChart.setMonitorData(monitor.getID(), monitorData);

						String dataPoints[][] = monitorData.getDataPoints();
						DateConversion dateConversion = new DateConversion(dataPoints[0][0], dataPoints[dataPoints.length - 1][0]);
						if (dataPoints != null) {
							Number[][] dataList = new Number[dataPoints.length][2];
							String[] timeStamps = new String[dataPoints.length];
							for (int x = 0; x < dataPoints.length; x++) {
								timeStamps[x] = dataPoints[x][0].substring(11, 16);
								dataList[x][0] = dateConversion.convert(dataPoints[x][0]);
								dataList[x][1] = (Double.valueOf(dataPoints[x][1]));
							}

							int pointsTotal = dataList[dataPoints.length - 1][0].intValue();
							String[] expandedTimeStamps = new String[pointsTotal + 1];
							for (int t = 0, x = 0, index = dataList[x][0].intValue(); t <= pointsTotal; t++) {
								if (t == index) {
									expandedTimeStamps[t] = timeStamps[x];
									if (++x < dataPoints.length) {
										index = dataList[x][0].intValue();
									}
								} else {
									expandedTimeStamps[t] = "";
								}
							}
							rs.setRangeData(dataList);
							XAxis xAxis = configuration.getxAxis();
							Labels labels = new Labels();
							labels.setRotation(-45);
							labels.setAlign(HorizontalAlign.RIGHT);
							xAxis.setLabels(labels);
							xAxis.setCategories(expandedTimeStamps);
						} else {
							rs.setRangeData(new Number[0][0]);
						}
					}

					if (needsRedraw) {
						chart.drawChart(configuration);
					}

				} else if (UserChart.TESTCHART2.equalsIgnoreCase(userChart.getType())) {
					Configuration configuration = chart.getConfiguration();
					String[] timeStamps = null;

					for (String monitorID : userChart.getMonitorIDs()) {
						MonitorRecord monitor = Monitors.getMonitor(monitorID);

						ListSeries ls = null, testLS;
						List<Series> lsList = configuration.getSeries();
						Iterator seriesIter = lsList.iterator();
						while (seriesIter.hasNext()) {
							testLS = (ListSeries) seriesIter.next();
							if (testLS.getName().equalsIgnoreCase(monitor.getName())) {
								ls = testLS;
								break;
							}
						}
						if (ls == null) {
							ls = new ListSeries(monitor.getName());
							configuration.addSeries(ls);
						}

						MonitorData2 monitorData = (MonitorData2) userChart.getMonitorData(monitor.getID());
						if (monitorData == null) {
							monitorData = new MonitorData2(monitor, systemID, nodeID, time, interval);
							needsRedraw = true;
						} else if (monitorData.update(systemID, nodeID, time, interval) == true) {
							// data in chart needs to be updated
							needsRedraw = true;
						} else {
							continue; // no update needed
						}

						userChart.setMonitorData(monitor.getID(), monitorData);

						String dataPoints[][] = monitorData.getDataPoints();
						ConsoleUI.log("monitorData2 = length: " + dataPoints.length);
						//Number times[] = { 10, 20, 25, 30, 35 };
						DateConversion dateConversion = new DateConversion();
						if (dataPoints != null) {
							Number[][] dataList = new Number[dataPoints.length][2];
							for (int x = 0; x < dataPoints.length; x++) {
								ConsoleUI.log("[0]: " + dataPoints[x][0] + ", [1]: " + dataPoints[x][1]);

								//timeStamps[x] = dataPoints[x][2].substring(11, 16);
								//dataList[x][0] = times[x]; //(Double.valueOf(dataPoints[x][0]));
								dataList[x][0] = dateConversion.convert(dataPoints[x][0]);
								dataList[x][1] = (Double.valueOf(dataPoints[x][1]));

							}
							//ls.setData(dataList);
						} else {
							//ls.setData(new Number[0][0]);
						}

					}

					// if (timeStamps != null) {
					// configuration.getxAxis().setCategories(timeStamps);
					// }
					if (needsRedraw) {
						chart.drawChart(configuration);
					}

				}

			}

		}

	}

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

	public static class Custom2CssLayoutDropHandler extends DefaultCssLayoutDropHandler {

		@Override
		protected void handleComponentReordering(DragAndDropEvent event) {
			// Component re-ordering
			LayoutBoundTransferable transferable = (LayoutBoundTransferable) event.getTransferable();
			CssLayoutTargetDetails details = (CssLayoutTargetDetails) event.getTargetDetails();
			DDCssLayout layout = (DDCssLayout) details.getTarget();
			Component comp = transferable.getComponent();
			UserChart userChart = (UserChart) ((Chart) comp).getData();
			Component button = userChart.getDeleteButton();
			int idx = details.getOverIndex();
			Component over = details.getOverComponent();

			// Detach
			layout.removeComponent(comp);
			layout.removeComponent(button);

			// Add component
			if (idx >= 0 && idx < layout.getComponentCount()) {
				layout.addComponent(comp, idx);
				layout.addComponent(button, idx + 1);
			} else {
				layout.addComponent(comp);
				layout.addComponent(button);
			}
		}

	}

	public static class CustomCssLayoutDropHandler extends DefaultCssLayoutDropHandler {

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
				// drag, the component was released "on it self" so no
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
				if (HorizontalDropLocation.LEFT.equals(horizontalDropLocation)) {
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