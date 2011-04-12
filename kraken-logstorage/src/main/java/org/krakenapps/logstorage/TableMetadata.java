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

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class TableMetadata implements Map<String, String>{
	private String tableName;
	private String keyPrefix;
	private Properties source;

	public TableMetadata(Properties tableNumbers, String tableName) {
		this.source = tableNumbers;
		this.tableName = tableName;
		this.keyPrefix = tableName + ".";
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	@Override
	public int size() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsKey(Object key) {
		return source.containsKey(getTableMetadataKey(key));
	}

	@Override
	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String get(Object key) {
		return source.getProperty(getTableMetadataKey(key));
	}

	@Override
	public String put(String key, String value) {
		return (String) source.setProperty(getTableMetadataKey(key), value);
	}

	private String getTableMetadataKey(Object key) {
		return keyPrefix + key;
	}

	@Override
	public String remove(Object key) {
		return (String) source.remove(getTableMetadataKey(key));
	}

	@Override
	public void putAll(Map<? extends String, ? extends String> m) {
		for (Entry<? extends String, ? extends String> entry: m.entrySet()) {
			source.put(getTableMetadataKey(entry.getKey()), entry.getValue());
		}
	}

	@Override
	public void clear() {
		LinkedList<Object> candidates = new LinkedList<Object>();
		for (Entry<Object, Object> entry: source.entrySet()) {
			if (entry.getKey().toString().startsWith(keyPrefix)) {
				candidates.add(entry.getKey());
			}
		}
		for (Object obj : candidates) {
			source.remove(obj);
		}
	}

	@Override
	public Set<String> keySet() {
		Set<String> result = Collections.newSetFromMap(new HashMap<String, Boolean>());
		for (Object key : source.keySet()) {
			if (key.toString().startsWith(keyPrefix)) {
				result.add(key.toString());
			}
		}
		return result;
	}

	@Override
	public Collection<String> values() {
		Collection<String> result = new LinkedList<String>();
		for (Object key : source.keySet()) {
			if (key.toString().startsWith(keyPrefix)) {
				result.add(source.getProperty(key.toString()));
			}
		}
		return result;
	}

	@Override
	public Set<java.util.Map.Entry<String, String>> entrySet() {
		Set<Entry<String, String>> result = Collections.newSetFromMap(new HashMap<Entry<String, String>, Boolean>());
		for (Object key : source.keySet()) {
			if (key.toString().startsWith(keyPrefix)) {
				result.add(new SimpleEntry<String, String>(key.toString(), source.get(key).toString()));
			}
		}
		return result;
	}
	
}
