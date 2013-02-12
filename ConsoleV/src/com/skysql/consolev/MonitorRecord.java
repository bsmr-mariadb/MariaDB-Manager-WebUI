package com.skysql.consolev;

public class MonitorRecord {

		private String ID;
		private String name;
		private String description;
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
		public String getIcon() {
			return icon;
		}
		public String getType() {
			return type;
		}
		
		public MonitorRecord(String ID, String name, String description, String icon, String type) {
			this.ID = ID;
			this.name = name;
			this.description = description;
			this.icon = icon;
			this.type = type;
		}
		//pippo2

}