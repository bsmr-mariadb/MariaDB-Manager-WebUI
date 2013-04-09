package com.skysql.consolev;

public class BackupRecord {

	private String ID;
	private String status;
	private String started;
	private String updated;
	private String level;
	private String node;
	private String size;
	private String storage;
	private String restored;
	private String log;
	private String parent;

	public String getID() {
		return ID;
	}

	public String getStatus() {
		return status;
	}

	public String getStarted() {
		return started;
	}

	public String getUpdated() {
		return updated;
	}

	public String getLevel() {
		return level;
	}

	public String getNode() {
		return node;
	}

	public String getSize() {
		return size;
	}

	public String getStorage() {
		return storage;
	}

	public String getRestored() {
		return restored;
	}

	public String getLog() {
		return log;
	}

	public String getParent() {
		return parent;
	}

	public BackupRecord(String ID, String status, String started, String updated, String level, String node, String size, String storage, String restored,
			String log, String parent) {
		this.ID = ID;
		this.status = status;
		this.started = started;
		this.updated = updated;
		this.level = level;
		this.node = node;
		this.size = size;
		this.storage = storage;
		this.restored = restored;
		this.log = log;
		this.parent = parent;
	}
}