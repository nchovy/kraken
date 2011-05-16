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
package org.krakenapps.radius.server.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.krakenapps.radius.server.RadiusConfigMetadata;
import org.krakenapps.radius.server.RadiusConfigurator;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;

public class PreferencesConfigurator implements RadiusConfigurator {
	private PreferencesService prefsvc;
	private String category;
	private String section;
	private Map<String, RadiusConfigMetadata> configMetadataMap;

	public PreferencesConfigurator(PreferencesService prefsvc, String category, String section) {
		this(prefsvc, category, section, new ArrayList<RadiusConfigMetadata>());
	}

	public PreferencesConfigurator(PreferencesService prefsvc, String category, String section,
			Collection<RadiusConfigMetadata> configMetadatas) {
		this.prefsvc = prefsvc;
		this.category = category;
		this.section = section;
		this.configMetadataMap = new ConcurrentHashMap<String, RadiusConfigMetadata>();

		for (RadiusConfigMetadata m : configMetadatas)
			configMetadataMap.put(m.getName(), m);

		try {
			Preferences root = prefsvc.getSystemPreferences();
			root.node(category).node(section);
			root.flush();
			root.sync();
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Set<String> keySet() {
		try {
			Preferences root = prefsvc.getSystemPreferences();
			Preferences p = root.node(category).node(section);
			Set<String> keySet = new HashSet<String>();

			for (String s : p.childrenNames())
				keySet.add(s);

			return keySet;
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object get(String key) {
		Preferences root = prefsvc.getSystemPreferences();
		Preferences p = root.node(category).node(section);

		String value = p.get(key, null);

		// type recovery
		RadiusConfigMetadata metadata = configMetadataMap.get(key);

		if (value == null) {
			if (metadata != null)
				return metadata.getDefaultValue();
			else
				return null;
		}

		if (metadata != null) {
			switch (metadata.getType()) {
			case String:
				return value;
			case Integer:
				return Integer.valueOf(value);
			case Boolean:
				return Boolean.valueOf(value);
			default:
				throw new UnsupportedOperationException("unsupported config type");
			}
		}

		return value;
	}

	@Override
	public String getString(String key) {
		return (String) get(key);
	}

	@Override
	public Integer getInteger(String key) {
		return (Integer) get(key);
	}

	@Override
	public Boolean getBoolean(String key) {
		return (Boolean) get(key);
	}

	@Override
	public void put(String key, Object value) {
		try {
			Preferences root = prefsvc.getSystemPreferences();
			Preferences p = root.node(category).node(section);

			p.put(key, value.toString());

			p.flush();
			p.sync();
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void purge() {
		try {
			Preferences root = prefsvc.getSystemPreferences();
			root.node(category).removeNode();
			root.flush();
			root.sync();
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}
}
