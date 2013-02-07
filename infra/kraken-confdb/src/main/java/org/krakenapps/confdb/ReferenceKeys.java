/*
 * Copyright 2011 Future Systems, Inc.
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
