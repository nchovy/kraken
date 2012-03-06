package org.krakenapps.msgbus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class Marshaler {
	private Marshaler() {
	}

	public static Map<String, Object> marshal(Map<String, ? extends Marshalable> source) {
		if (source == null)
			return null;

		Map<String, Object> result = new TreeMap<String, Object>();

		for (Map.Entry<String, ? extends Marshalable> entry : source.entrySet()) {
			result.put(entry.getKey(), entry.getValue().marshal());
		}

		return result;
	}

	public static List<Object> marshal(Collection<? extends Marshalable> list) {
		if (list == null)
			return null;

		List<Object> serializedObjects = new ArrayList<Object>();

		for (Marshalable m : list) {
			serializedObjects.add(m.marshal());
		}

		return serializedObjects;
	}

	public static Map<String, Object> marshal(Map<String, ? extends Localizable> source, Locale locale) {
		if (source == null)
			return null;

		Map<String, Object> result = new TreeMap<String, Object>();

		for (Map.Entry<String, ? extends Localizable> entry : source.entrySet()) {
			result.put(entry.getKey(), entry.getValue().marshal(locale));
		}

		return result;
	}

	public static List<Object> marshal(Collection<? extends Localizable> list, Locale locale) {
		if (list == null)
			return null;

		List<Object> serializedObjects = new ArrayList<Object>();

		for (Localizable m : list) {
			serializedObjects.add(m.marshal(locale));
		}

		return serializedObjects;
	}
}
