package org.json;

import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
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
}
