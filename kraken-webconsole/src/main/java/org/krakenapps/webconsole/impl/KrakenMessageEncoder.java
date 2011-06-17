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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONWriter;
import org.krakenapps.msgbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KrakenMessageEncoder {
	private static Logger logger = LoggerFactory.getLogger(KrakenMessageEncoder.class);

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

			properties = convertDate(properties);
			for (String key : properties.keySet()) {
				jsonWriter.key(key).value(properties.get(key));
			}

			jsonWriter.endObject();

			jsonWriter.endArray();
		} catch (Exception e) {
			logger.error("kraken webconsole: json encode error", e);
		}

		return writer.toString();
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> convertDate(Map<String, Object> properties) {
		Map<String, Object> m = new HashMap<String, Object>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");

		for (String key : properties.keySet()) {
			Object value = properties.get(key);

			if (value instanceof Date)
				m.put(key, dateFormat.format((Date) value));
			else if (value instanceof Map)
				m.put(key, convertDate((Map<String, Object>) value));
			else if (value instanceof Collection) {
				Collection<Object> c = new ArrayList<Object>();
				for (Object v : (Collection<?>) value) {
					if (v instanceof Date)
						c.add(dateFormat.format((Date) v));
					else
						c.add(v);
				}
				m.put(key, c);
			} else
				m.put(key, value);
		}

		return m;
	}
}
