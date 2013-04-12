/*
 * This file is distributed as import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashMap;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
ould have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Copyright SkySQL Ab
 */

package com.skysql.consolev.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashMap;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class CommandSteps {

	private static CommandSteps commandSteps;
	private static LinkedHashMap<String, String[]> stepsMap;

	public static LinkedHashMap<String, String[]> getStepsMap() {
		GetCommands();
		return CommandSteps.stepsMap;
	}

	private static void GetCommands() {
		if (commandSteps == null) {
			String inputLine = null;
			try {
				URL url = new URL("http://localhost/consoleAPI/commandsteps.php");
				URLConnection sc = url.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(sc.getInputStream()));
				inputLine = in.readLine();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("Could not get response from API");
			}

			Gson gson = AppData.getGson();
			commandSteps = gson.fromJson(inputLine, CommandSteps.class);
		}
	}

	protected void setStepsMap(LinkedHashMap<String, String[]> map) {
		CommandSteps.stepsMap = map;
	}
}

class CommandStepsDeserializer implements JsonDeserializer<CommandSteps> {
	public CommandSteps deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		CommandSteps commandSteps = new CommandSteps();

		JsonElement jsonElement = json.getAsJsonObject().get("command_steps");
		if (jsonElement == null || jsonElement.isJsonNull()) {
			commandSteps.setStepsMap(null);
		} else {
			JsonArray array = jsonElement.getAsJsonArray();
			int length = array.size();

			LinkedHashMap<String, String[]> commandStepsMap = new LinkedHashMap<String, String[]>(length);
			for (int i = 0; i < length; i++) {
				JsonObject pair = array.get(i).getAsJsonObject();
				String command = pair.get("command").getAsString();

				JsonArray stepsJson = pair.get("steps").getAsJsonArray();
				int count = stepsJson.size();
				String steps[] = new String[count];

				for (int j = 0; j < count; j++) {
					steps[j] = stepsJson.get(j).getAsString();
				}

				commandStepsMap.put(command, steps);
			}

			commandSteps.setStepsMap(commandStepsMap);

		}

		return commandSteps;
	}

}
