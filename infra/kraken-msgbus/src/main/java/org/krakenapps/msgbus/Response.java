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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Response implements Map<String, Object> {

	private Map<String, Object> m;

	public Response() {
		m = new HashMap<String, Object>();
	}

	@Override
	public void clear() {
		m.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return m.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return m.containsKey(value);
	}

	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		return m.entrySet();
	}

	@Override
	public Object get(Object key) {
		return m.get(key);
	}

	@Override
	public boolean isEmpty() {
		return m.isEmpty();
	}

	@Override
	public Set<String> keySet() {
		return m.keySet();
	}

	@Override
	public Object put(String key, Object value) {
		return m.put(key, value);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		for (String key : m.keySet())
			put(key, m.get(key));
	}

	@Override
	public Object remove(Object key) {
		return m.remove(key);
	}

	@Override
	public int size() {
		return m.size();
	}

	@Override
	public Collection<Object> values() {
		return m.values();
	}
}
