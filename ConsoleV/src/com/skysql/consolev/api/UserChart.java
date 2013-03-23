package com.skysql.consolev.api;

import java.util.ArrayList;
import java.util.HashMap;

import com.vaadin.ui.Button;

public class UserChart extends ChartMappings {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private HashMap<String, Object> monitorsData;
	private Button deleteButton;

	public UserChart(String name, String description, String unit, String type, int points, ArrayList<String> monitorIDs) {
		super(name, description, unit, type, points, monitorIDs);
		this.monitorsData = new HashMap<String, Object>();
	}

	public UserChart(UserChart oldUserChart) {
		super(oldUserChart);
		this.monitorsData = oldUserChart.monitorsData;
		this.deleteButton = oldUserChart.deleteButton;
	}

	public UserChart(ChartMappings chartMappings) {
		super(chartMappings);
		this.monitorsData = new HashMap<String, Object>();
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
