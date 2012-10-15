package org.json;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONConverter {
	public static String jsonize(Object o) throws JSONException {
		StringWriter writer = new StringWriter();
		JSONWriter jsonWriter = new JSONWriter(writer);
		jsonize(o, jsonWriter);
		return writer.toString();
	}

	private static void jsonize(Object o, JSONWriter jsonWriter) throws JSONException {
		if (o instanceof Map) {
			jsonWriter.object();

			@SuppressWarnings("unchecked")
			Map<String, Object> m = (HashMap<String, Object>) o;

			for (String key : m.keySet())
				jsonWriter.key(key).value(m.get(key));

			jsonWriter.endObject();
			return;
		} else if (o instanceof Collection) {
			jsonWriter.array();

			@SuppressWarnings("unchecked")
			Collection<Object> l = (Collection<Object>) o;
			for (Object child : l)
				jsonize(child, jsonWriter);

			jsonWriter.endArray();
			return;
		}

		throw new IllegalArgumentException("argument should be map or collection, " + o.getClass().getName());
	}

	public static Map<String, Object> parse(JSONObject jsonObject) throws IOException {
		Map<String, Object> m = new HashMap<String, Object>();
		String[] names = JSONObject.getNames(jsonObject);
		if (names == null)
			return m;

		for (String key : names) {
			try {
				Object value = jsonObject.get(key);
				if (value == JSONObject.NULL)
					value = null;
				else if (value instanceof JSONArray)
					value = parse((JSONArray) value);
				else if (value instanceof JSONObject)
					value = parse((JSONObject) value);

				m.put(key, value);
			} catch (JSONException e) {
				throw new IOException(e);
			}
		}

		return m;
	}

	public static Object parse(JSONArray jsonarray) throws IOException {
		List<Object> list = new ArrayList<Object>();
		for (int i = 0; i < jsonarray.length(); i++) {
			try {
				Object o = jsonarray.get(i);
				if (o == JSONObject.NULL)
					list.add(null);
				else if (o instanceof JSONArray)
					list.add(parse((JSONArray) o));
				else if (o instanceof JSONObject)
					list.add(parse((JSONObject) o));
				else
					list.add(o);
			} catch (JSONException e) {
				throw new IOException(e);
			}
		}
		return list;
	}
}
