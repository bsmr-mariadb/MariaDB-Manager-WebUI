package com.skysql.consolev;

public class TaskRecord {

		private String id;
		private String node;
		private String command;
		private String params;
		private String index;
		private String status;
		private String user;
		private String start;
		private String end;
		
		public String getID() {
			return id;
		}
		public String getNode() {
			return node;
		}
		public String getCommand() {
			return command;
		}
		public String getParams() {
			return params;
		}
		public String getIndex() {
			return index;
		}
		public String getStatus() {
			return status;
		}
		public String getUser() {
			return user;
		}
		public String getStart() {
			return start;
		}
		public String getEnd() {
			return end;
		}
		
		public TaskRecord(String id, String node, String command, String params, String index, String status, String user, String start, String end) {
			this.id = id;
			this.node = node;
			this.command = command;
			this.params = params;
			this.index = index;
			this.status = status;
			this.user = user;
			this.start = start;
			this.end = end;
		}

}
