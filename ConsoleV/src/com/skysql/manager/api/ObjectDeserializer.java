/*
 * This file is distributed as part of the MariaDB Manager.  It is free
 * software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * version 2.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Copyright 2012-2014 SkySQL Ab
 */

package com.skysql.manager.api;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.skysql.manager.MonitorLatest;

// {"connections":null,"traffic":null,"availability":null,"nodestate":null,"capacity":null,"hoststate":null,"clustersize":null,"reppaused":null,"parallelism":null,"recvqueue":null,"flowcontrol":null,"sendqueue":null}

public class ObjectDeserializer implements JsonDeserializer<MonitorLatest> {
	public MonitorLatest deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException, NullPointerException {
		MonitorLatest ml = new MonitorLatest();

		if (json != null) {
			LinkedHashMap<String, String> objects = new LinkedHashMap<String, String>();
			Set<Entry<String, JsonElement>> set = json.getAsJsonObject().entrySet();
			Iterator<Entry<String, JsonElement>> iter = set.iterator();
			while (iter.hasNext()) {
				Entry<String, JsonElement> entry = iter.next();
				String value = entry.getValue().isJsonNull() ? null : entry.getValue().getAsString();
				objects.put(entry.getKey(), value);
			}
			ml.setData(objects);
		}

		return ml;
	}

}
