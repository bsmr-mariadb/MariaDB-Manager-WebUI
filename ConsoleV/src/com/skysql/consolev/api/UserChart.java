package com.skysql.consolev.api;

import java.util.ArrayList;
import java.util.HashMap;

import com.skysql.consolev.MonitorRecord;
import com.vaadin.ui.Button;

public class UserChart {

	public static final String LINECHART = "LineChart";
	public static final String AREACHART = "AreaChart";

	public static String[] chartTypes() {
		String[] array = { LINECHART, AREACHART };
		return array;
	}

	private String name;
	private String description;
	private String unit;
	private String type;
	private int points;
	private ArrayList<MonitorRecord> monitors;
	private HashMap<String, Object> monitorsData;
	private Button deleteButton;

	public UserChart(String name, String description, String unit, String type, int points, ArrayList<MonitorRecord> monitors) {
		super();
		this.name = name;
		this.description = description;
		this.unit = unit;
		this.type = type;
		this.points = points;
		this.monitors = monitors;
		this.monitorsData = new HashMap<String, Object>();
	}

	public UserChart(UserChart oldUserChart) {
		this.name = oldUserChart.name;
		this.description = oldUserChart.description;
		this.unit = oldUserChart.unit;
		this.type = oldUserChart.type;
		this.points = oldUserChart.points;
		this.monitors = oldUserChart.monitors;
		this.monitorsData = new HashMap<String, Object>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ArrayList<MonitorRecord> getMonitors() {
		return monitors;
	}

	public void setMonitors(ArrayList<MonitorRecord> monitors) {
		this.monitors = monitors;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public Object getMonitorData(String id) {
		return monitorsData.get(id);
	}

	public void setMonitorData(String key, Object value) {
		this.monitorsData.put(key, value);
	}

	public void clearMonitorData() {
		this.monitorsData = new HashMap<String, Object>();
	}

	public Button getDeleteButton() {
		return deleteButton;
	}

	public void setDeleteButton(Button deleteButton) {
		this.deleteButton = deleteButton;
	}

}
