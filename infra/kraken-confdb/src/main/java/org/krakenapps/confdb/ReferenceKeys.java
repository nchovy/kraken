package org.krakenapps.confdb;

import java.util.HashMap;
import java.util.Map;

import org.krakenapps.api.PrimitiveConverter;

public class ReferenceKeys {
	private Map<String, Object> terms;
	private Map<String, String[]> underscoreMap = new HashMap<String, String[]>();

	public ReferenceKeys(String key, Object value) {
		this.terms = new HashMap<String, Object>();
		this.terms.put(key, value);
		underscoreMap.put(key, PrimitiveConverter.toUnderscoreName(key).split("/"));
	}

	public ReferenceKeys(Map<String, Object> terms) {
		this.terms = terms;
		for (String term : terms.keySet())
			underscoreMap.put(term, PrimitiveConverter.toUnderscoreName(term).split("/"));
	}

	public boolean eval(Object doc) {
		if (!(doc instanceof Map))
			return false;

		for (String k : terms.keySet()) {
			Object value = getValue(doc, underscoreMap.get(k));
			Object comp = terms.get(k);

			if (value == null && comp != null)
				return false;

			if (value == null && comp == null)
				continue;

			if (!value.equals(comp))
				return false;
		}

		return true;
	}

	private Object getValue(Object value, String[] keys) {
		for (String k : keys) {
			if (!(value instanceof Map)) {
				return null;
			}

			@SuppressWarnings("unchecked")
			Map<String, Object> m = (Map<String, Object>) value;
			if (!m.containsKey(k)) {
				return null;
			}

			value = m.get(k);
		}
		return value;
	}

}
