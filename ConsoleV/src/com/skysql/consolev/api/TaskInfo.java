package com.skysql.consolev.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.skysql.consolev.TaskRecord;

public class TaskInfo {

	ArrayList<TaskRecord> tasksList;

	public ArrayList<TaskRecord> getTasksList() {
		return tasksList;
	}

	public void setTasksList(ArrayList<TaskRecord> tasksList) {
		this.tasksList = tasksList;
	}

	public TaskInfo() {
	}

	public TaskInfo(String taskID, String status, String node) {

		String inputLine = null;
		try {
			URL url = new URI("http", "localhost", "/consoleAPI/taskinfo.php", "task=" + taskID + "&status=" + status + "&node=" + node, null).toURL();
			URLConnection sc = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(sc.getInputStream()));
			inputLine = in.readLine();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Could not get response from API");
		}

		Gson gson = AppData.getGson();
		TaskInfo taskInfo = gson.fromJson(inputLine, TaskInfo.class);
		this.tasksList = taskInfo.tasksList;

	}

}

class TaskInfoDeserializer implements JsonDeserializer<TaskInfo> {
	public TaskInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		TaskInfo taskInfo = new TaskInfo();

		JsonElement jsonElement = json.getAsJsonObject().get("tasks");
		if (jsonElement == null || jsonElement.isJsonNull()) {
			taskInfo.setTasksList(null);
		} else {
			JsonArray array = jsonElement.getAsJsonArray();
			int length = array.size();

			ArrayList<TaskRecord> tasksList = new ArrayList<TaskRecord>(length);
			for (int i = 0; i < length; i++) {
				JsonObject backupJson = array.get(i).getAsJsonObject();
				JsonElement element;
				String id = (element = backupJson.get("id")).isJsonNull() ? null : element.getAsString();
				String node = (element = backupJson.get("node")).isJsonNull() ? null : element.getAsString();
				String command = (element = backupJson.get("command")).isJsonNull() ? null : element.getAsString();
				String params = (element = backupJson.get("params")).isJsonNull() ? null : element.getAsString();
				String index = (element = backupJson.get("index")).isJsonNull() ? null : element.getAsString();
				String status = (element = backupJson.get("status")).isJsonNull() ? null : element.getAsString();
				String user = (element = backupJson.get("user")).isJsonNull() ? null : element.getAsString();
				String start = (element = backupJson.get("start")).isJsonNull() ? null : element.getAsString();
				String end = (element = backupJson.get("end")).isJsonNull() ? null : element.getAsString();
				TaskRecord taskRecord = new TaskRecord(id, node, command, params, index, status, user, start, end);
				tasksList.add(taskRecord);
			}
			taskInfo.setTasksList(tasksList);
		}

		return taskInfo;

	}

}
