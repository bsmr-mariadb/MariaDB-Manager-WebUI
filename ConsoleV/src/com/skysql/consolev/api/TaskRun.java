package com.skysql.consolev.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class TaskRun {
	private String task;

	public String getTask() {
		return task;
	}

	public void setTask(String task) {
		this.task = task;
	}

	public TaskRun() {
	}

	public TaskRun(String systemID, String nodeID, String userID, String command, String params) {

		String inputLine = null;
		try {
			URL url = new URI("http", "localhost", "/consoleAPI/taskrun.php", "system=" + systemID + "&node=" + nodeID + "&user=" + userID + "&command="
					+ command + "&params=" + params, null).toURL();
			URLConnection sc = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(sc.getInputStream()));
			inputLine = in.readLine();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Could not get response from API");
		}

		Gson gson = AppData.getGson();
		TaskRun taskRun = gson.fromJson(inputLine, TaskRun.class);
		//this.nodeID = nodeID;
		this.task = taskRun.task;
		taskRun = null;

	}

}

class TaskRunDeserializer implements JsonDeserializer<TaskRun> {
	public TaskRun deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		TaskRun taskRun = new TaskRun();

		JsonObject jsonObject = json.getAsJsonObject();

		JsonElement element;
		taskRun.setTask(((element = jsonObject.get("task")) == null || element.isJsonNull()) ? null : element.getAsString());

		/***
		 * For when we implement server-side enabling/disabling of script
		 * controls element = jsonObject.get("controls"); if (element == null ||
		 * element.isJsonNull()) { taskRun.setPrimitives(null); } else {
		 * JsonArray controlsJson = element.getAsJsonArray(); int length =
		 * controlsJson.size(); String[] controls = new String[length];
		 * taskRun.setControls(controls); for (int i = 0; i < length; i++)
		 * controls[i] = controlsJson.get(i).getAsString(); }
		 ***/

		return taskRun;
	}

}
