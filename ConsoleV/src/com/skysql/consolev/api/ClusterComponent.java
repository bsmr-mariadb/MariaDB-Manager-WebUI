package com.skysql.consolev.api;

import com.vaadin.ui.VerticalLayout;

public class ClusterComponent {

	public enum CCType {
		system, node;
	}

	protected String ID;
	protected String name;
	protected VerticalLayout button;
	protected CCType type;
	protected String status;

	public String getID() {
		return ID;
	}

	public void setID(String ID) {
		this.ID = ID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public VerticalLayout getButton() {
		return button;
	}

	public void setButton(VerticalLayout button) {
		this.button = button;
	}

	public CCType getType() {
		return type;
	}

	public void setType(CCType type) {
		this.type = type;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}