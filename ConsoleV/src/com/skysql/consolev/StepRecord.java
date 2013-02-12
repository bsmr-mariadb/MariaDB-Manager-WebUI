package com.skysql.consolev;

public class StepRecord {

		private String script;
		private String icon;
		private String description;
		
		public String getScript() {
			return script;
		}
		public String getIcon() {
			return icon;
		}
		public String getDescription() {
			return description;
		}
		
		public StepRecord(String script, String icon, String description) {
			this.script = script;
			this.icon = icon;
			this.description = description;
		}


}
