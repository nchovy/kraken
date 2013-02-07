/*
 * Copyright 2011 Future Systems, Inc
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
