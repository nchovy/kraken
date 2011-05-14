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
	
	public PreferencesConfigurator(PreferencesService prefsvc, String category, String section,
			Collection<RadiusConfigMetadata> configMetadatas) {
		this.prefsvc = prefsvc;
		this.category = category;
		this.section = section;
		this.configMetadataMap = new ConcurrentHashMap<String, RadiusConfigMetadata>();
		
		for (RadiusConfigMetadata m : configMetadatas)
			configMetadataMap.put(m.getName(), m);
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
		// TODO: type recovery

		return p.get(key, null);
	}

	@Override
	public void put(String key, Object value) {
		// TODO Auto-generated method stub

	}
}
