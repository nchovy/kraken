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
package org.krakenapps.webconsole.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.webconsole.PackageApi;

@Component(name = "webconsole-package-api")
@Provides
public class PackageApiImpl implements PackageApi {
	private ConcurrentMap<String, Map<Locale, String>> packages;

	public PackageApiImpl() {
		packages = new ConcurrentHashMap<String, Map<Locale, String>>();
	}

	@Validate
	public void start() {
		Map<Locale, String> names = new HashMap<Locale, String>();
		names.put(Locale.ENGLISH, "System");
		names.put(Locale.KOREAN, "시스템");
		packages.put("system", names);
	}

	@Invalidate
	public void stop() {
		packages.remove("system");
	}

	@Override
	public Collection<String> getPackages() {
		return packages.keySet();
	}

	@Override
	public String getLabel(String id, Locale locale) {
		Map<Locale, String> names = packages.get(id);
		if (names == null)
			return null;

		return names.get(locale);
	}

	@Override
	public void register(String id) {
		Map<Locale, String> labels = new HashMap<Locale, String>();
		packages.putIfAbsent(id, labels);
	}

	@Override
	public void unregister(String id) {
		packages.remove(id);
	}

	@Override
	public void localize(String id, Locale locale, String label) {
		Map<Locale, String> labels = packages.get(id);
		if (labels == null)
			return;

		labels.put(locale, label);
	}
}
