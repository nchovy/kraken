/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.logstorage;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Deprecated
public class TableMetadata implements Map<String, String> {
	private int id;
	private String tableName;
	private Map<String, String> params;

	public TableMetadata(int id, String tableName, Map<String, String> params) {
		this.id = id;
		this.tableName = tableName;
		this.params = params;
	}

	public int getTableId() {
		return id;
	}

	public String getTableName() {
		return tableName;
	}

	@Override
	public int size() {
		return params.size();
	}

	@Override
	public boolean isEmpty() {
		return params.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return params.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return params.containsValue(value);
	}

	@Override
	public String get(Object key) {
		return (String) params.get(key);
	}

	@Override
	public String put(String key, String value) {
		return (String) params.put(key, value);
	}

	@Override
	public String remove(Object key) {
		return (String) params.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends String> m) {
		params.putAll(m);
	}

	@Override
	public void clear() {
		params.clear();
	}

	@Override
	public Set<String> keySet() {
		return params.keySet();
	}

	@Override
	public Collection<String> values() {
		return params.values();
	}

	@Override
	public Set<Map.Entry<String, String>> entrySet() {
		return params.entrySet();
	}

}
