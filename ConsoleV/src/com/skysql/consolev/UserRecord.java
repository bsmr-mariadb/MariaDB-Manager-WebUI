package com.skysql.consolev;

public class UserRecord {
	private String ID;
	private String name;

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
		
	public UserRecord(String ID, String name) {
		this.ID = ID;
		this.name = name;
	}

}
