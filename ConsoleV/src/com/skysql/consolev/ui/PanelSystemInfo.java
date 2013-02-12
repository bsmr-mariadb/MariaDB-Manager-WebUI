package com.skysql.consolev.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.vaadin.vaadinvisualizations.LineChart;

import com.skysql.consolev.MonitorRecord;
import com.skysql.consolev.SessionData;
import com.skysql.consolev.api.AppData;
import com.skysql.consolev.api.MonitorData;
import com.skysql.consolev.api.Monitors;
import com.skysql.consolev.api.NodeInfo;
import com.skysql.consolev.api.NodeStates;
import com.skysql.consolev.api.SystemInfo;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.VerticalLayout;

public class PanelSystemInfo {

	private static final String NOT_AVAILABLE = "n/a";
	private static final String INTERVAL_5_MIN = "5 minutes";
	private static final String INTERVAL_10_MIN = "10 minutes";
	private static final String INTERVAL_30_MIN = "30 minutes";
	private static final String INTERVAL_1_HOUR = "1 hour";
	private static final String INTERVAL_6_HOURS = "6 hours";
	private static final String INTERVAL_12_HOURS = "12 hours";
	private static final String INTERVAL_1_DAY = "1 day";
	private static final String INTERVAL_1_WEEK = "1 week";
	private static final String INTERVAL_1_MONTH = "1 month";

	private static final String SYSTEM_NODEID = "0";

	private NodeInfo nodeInfo;
	private GridLayout currentGrid;
	private Label[] currentValues;
    private VerticalLayout chartsLayout;
    private HorizontalLayout chartsControls;
    private HorizontalLayout chartsArray;
	private String chartTime, chartInterval = "120", chartPoints = "15";
	private ArrayList<MonitorRecord> monitorsList = Monitors.getMonitorsList();
	private LinkedHashMap<String, LineChart> chartsList;
    private LinkedHashMap<String, MonitorData> monitorDataList;

	private String currentLabels[] = {
            "Status",
			"Availability",
            "Connections",
            "Data Transfer",
            "Last Backup",
            "Start Date",
            "Last Access",
    };

    PanelSystemInfo(HorizontalLayout infoTab, Object userData) {
        // INFO TAB
        //infoTab.setSizeFull();
    	infoTab.setWidth(Sizeable.SIZE_UNDEFINED, 0); // Default

        infoTab.addStyleName("infoTab");
        //infoTab.setSpacing(false);

        // CURRENT INFO
        currentGrid = new GridLayout(2, currentLabels.length);
        currentGrid.addStyleName("currentInfo");
        currentGrid.setSpacing(true);
        //currentGrid.setHeight("200px");

    	currentValues = new Label[currentLabels.length];
        for (int i=0; i < currentLabels.length; i++) {
        	currentGrid.addComponent(new Label(currentLabels[i]), 0, i);
            currentValues[i] = new Label("");
            currentGrid.addComponent(currentValues[i], 1, i);
        }
        infoTab.addComponent(currentGrid);

        chartsLayout = new VerticalLayout();
        chartsControls = new HorizontalLayout();
        chartsLayout.addComponent(chartsControls);
        chartsArray = new HorizontalLayout();
        chartsLayout.addComponent(chartsArray);
        infoTab.addComponent(chartsLayout);

        // Setup charts controls
		{
	        Label spacer = new Label();
	        spacer.setWidth("50px");
	        chartsControls.addComponent(spacer);
		}
 
		final FormLayout layout = new FormLayout();
		        
        final NativeSelect selectInterval = new NativeSelect("Charts Time Span");
        //selectInterval.addStyleName("selectInterval");
        selectInterval.setImmediate(true);
        selectInterval.addItem(INTERVAL_5_MIN);
        selectInterval.addItem(INTERVAL_10_MIN);
        selectInterval.addItem(INTERVAL_30_MIN);
        selectInterval.addItem(INTERVAL_1_HOUR);
        selectInterval.addItem(INTERVAL_6_HOURS);
        selectInterval.addItem(INTERVAL_12_HOURS);
        selectInterval.addItem(INTERVAL_1_DAY);
        selectInterval.addItem(INTERVAL_1_WEEK);
        selectInterval.addItem(INTERVAL_1_MONTH);
        selectInterval.select(INTERVAL_30_MIN);
        selectInterval.setNullSelectionAllowed(false);
		layout.addComponent(selectInterval);
        chartsControls.addComponent(layout);
        selectInterval.addListener(new ValueChangeListener() {
        	private static final long serialVersionUID = 0x4C656F6E6172646FL;
            public void valueChange(ValueChangeEvent event) {
                String value = (String)((NativeSelect)event.getProperty()).getValue();
                int pointsCount = Integer.parseInt(chartPoints);
                int interval = Integer.parseInt(chartInterval);
                if (value.equalsIgnoreCase(INTERVAL_5_MIN)) {
                	interval = 5*60/pointsCount;
                } else if (value.equalsIgnoreCase(INTERVAL_10_MIN)) {
                	interval = 10*60/pointsCount;                	
                } else if (value.equalsIgnoreCase(INTERVAL_30_MIN)) {
                	interval = 30*60/pointsCount;                	
                } else if (value.equalsIgnoreCase(INTERVAL_1_HOUR)) {
                	interval = 1*60*60/pointsCount;                	
                } else if (value.equalsIgnoreCase(INTERVAL_6_HOURS)) {
                	interval = 6*60*60/pointsCount;                	
                } else if (value.equalsIgnoreCase(INTERVAL_12_HOURS)) {
                	interval = 12*60*60/pointsCount;                	
                } else if (value.equalsIgnoreCase(INTERVAL_1_DAY)) {
                	interval = 24*60*60/pointsCount;                	
                } else if (value.equalsIgnoreCase(INTERVAL_1_WEEK)) {
                	interval = 7*24*60*60/pointsCount;                	
                } else if (value.equalsIgnoreCase(INTERVAL_1_MONTH)) {
                	interval = 30*24*60*60/pointsCount;                	
                }
                chartInterval = Integer.toString(interval);
                refresh(nodeInfo);
                ((SessionData)layout.getApplication().getUser()).getICEPush().push();
            }
        });


        // Setup array of charts objects
	    monitorDataList = new LinkedHashMap<String, MonitorData>(monitorsList.size());
	    chartsList = new LinkedHashMap<String, LineChart>(monitorsList.size());
		for (MonitorRecord monitor : monitorsList) {
			LineChart chart = new LineChart(); 
			chart.setWidth("330px");
			chart.setHeight("260px");
			chartsList.put(monitor.getID(), chart);
		}

	}
	
	public boolean refresh(NodeInfo nodeInfo) {		
		boolean refreshed = false;
		this.nodeInfo = nodeInfo;

		SystemInfo systemInfo = new SystemInfo(AppData.getGson());
		String systemID = systemInfo.getSystemID();
		
		// CURRENT VALUES
		String value;
		String values[] = {
			((value = nodeInfo.getStatus()) != null) && (value = NodeStates.getNodeStatesDescriptions().get(value)) != null ? value : "Invalid",
			(value = nodeInfo.getHealth()) != null ? value+"%" : NOT_AVAILABLE,
			(value = nodeInfo.getConnections()) != null ? value : NOT_AVAILABLE,
			(value = nodeInfo.getPackets()) != null ? value+" KB" : NOT_AVAILABLE,
			(value = systemInfo.getLastBackup()) != null ? value : NOT_AVAILABLE,
			(value = systemInfo.getStartDate()) != null ? value : NOT_AVAILABLE,
			(value = systemInfo.getLastAccess()) != null ? value : NOT_AVAILABLE
		};
		
		for (int i = 0; i < values.length && i < currentValues.length; i++) {
			value = values[i];
			if (!((String)currentValues[i].getValue()).equalsIgnoreCase(value)) {
				currentValues[i].setValue(value);
				refreshed = true;
			}
		}

		// MONITOR CHARTS
		for (MonitorRecord monitor : monitorsList) {
			String monitorID = monitor.getID();
			MonitorData newMonitorData = new MonitorData(monitorID, systemID, SYSTEM_NODEID, chartTime, chartInterval, chartPoints);
			MonitorData oldMonitorData = monitorDataList.get(monitorID);
			if ((oldMonitorData == null) || !oldMonitorData.equals(newMonitorData)) {
				monitorDataList.put(monitorID, newMonitorData);

				LineChart newChart = new LineChart();
				newChart.setOption("legend", "bottom");
				//newChart.setOption("vAxis.format", "###%");
				//newChart.setOption("vAxis.maxValue", "100");
				newChart.addXAxisLabel("Time Stamp");
				newChart.addLine(monitor.getName());
				newChart.setWidth("330px");
				newChart.setHeight("260px");
				
				String dataPoints[][] = newMonitorData.getDataPoints();
				if (dataPoints != null) {
					for (int x=0; x < dataPoints.length; x++) {
						String partialTime = dataPoints[x][0].substring(11, 16);
						newChart.add(partialTime, new double[]{Double.valueOf(dataPoints[x][1])});
					}
				}
				
				LineChart oldChart = chartsList.get(monitorID);
				chartsArray.replaceComponent(oldChart, newChart);
				chartsList.put(monitorID, newChart);
				refreshed = true;
			}

		}

		return refreshed;
	}
	
}
