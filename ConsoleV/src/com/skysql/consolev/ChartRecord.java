package com.skysql.consolev;

public class ChartRecord {

	private String ID;
	private String name;
	private String description;
	private String unit;
	private String icon;
	private String type;

	public String getID() {
		return ID;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getUnit() {
		return unit;
	}

	public String getIcon() {
		return icon;
	}

	public String getType() {
		return type;
	}

	public ChartRecord(String ID, String name, String description, String unit, String icon, String type) {
		this.ID = ID;
		this.name = name;
		this.description = description;
		this.unit = unit;
		this.icon = icon;
		this.type = type;
	}

}