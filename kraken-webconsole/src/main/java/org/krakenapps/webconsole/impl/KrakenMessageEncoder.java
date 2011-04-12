/*
 * Copyright 2011 Future Systems
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.webconsole.impl;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONWriter;
import org.krakenapps.msgbus.Message;

public class KrakenMessageEncoder {
	private KrakenMessageEncoder() {
	}

	public static String encode(Message msg) {
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("guid", msg.getGuid());
		if (msg.getRequestId() != null)
			headers.put("requestId", msg.getRequestId());

		headers.put("type", msg.getType().toString());
		headers.put("method", msg.getMethod());
		headers.put("session", msg.getSession());
		headers.put("source", msg.getSource());
		headers.put("target", msg.getTarget());

		if (msg.getErrorCode() != null) {
			headers.put("errorCode", msg.getErrorCode());
			headers.put("errorMessage", msg.getErrorMessage());
		}

		return jsonize(headers, msg.getParameters());
	}

	private static String jsonize(Map<String, Object> headers, Map<String, Object> properties) {
		StringWriter writer = new StringWriter(1024);
		JSONWriter jsonWriter = new JSONWriter(writer);

		try {
			jsonWriter.array();

			jsonWriter.object();

			for (String key : headers.keySet()) {
				jsonWriter.key(key).value(headers.get(key));
			}

			jsonWriter.endObject();

			jsonWriter.object();

			for (String key : properties.keySet()) {
				jsonWriter.key(key).value(properties.get(key));
			}

			jsonWriter.endObject();

			jsonWriter.endArray();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return writer.toString();
	}

}
