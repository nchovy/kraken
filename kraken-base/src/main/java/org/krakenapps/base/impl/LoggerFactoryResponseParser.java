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
package org.krakenapps.base.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.krakenapps.base.RemoteLoggerFactoryInfo;
import org.krakenapps.log.api.LoggerConfigOption;

public class LoggerFactoryResponseParser {
	private LoggerFactoryResponseParser() {
	}

	@SuppressWarnings("unchecked")
	public static Map<String, RemoteLoggerFactoryInfo> parseFactories(Object[] factories) {
		Map<String, RemoteLoggerFactoryInfo> m = new HashMap<String, RemoteLoggerFactoryInfo>();
		for (Object factory : factories) {
			RemoteLoggerFactoryInfo info = parseFactory((Map<String, Object>) factory);
			m.put(info.getName(), info);
		}

		return m;
	}

	@SuppressWarnings("unchecked")
	public static RemoteLoggerFactoryInfo parseFactory(Map<String, Object> factory) {
		String name = (String) factory.get("name");
		Collection<LoggerConfigOption> options = parseOptions((Object[]) factory.get("config_options"));
		Map<Locale, String> displayNames = parseLocaleMap((Map<String, String>) factory.get("display_names"));
		Map<Locale, String> descriptions = parseLocaleMap((Map<String, String>) factory.get("descriptions"));

		return new RemoteLoggerFactoryInfoImpl(name, options, displayNames, descriptions);
	}

	@SuppressWarnings("unchecked")
	private static Collection<LoggerConfigOption> parseOptions(Object[] options) {
		List<LoggerConfigOption> l = new ArrayList<LoggerConfigOption>();

		for (Object option : options) {
			Map<String, Object> o = (Map<String, Object>) option;
			String name = (String) o.get("name");
			String type = (String) o.get("type");
			boolean isRequired = (Boolean) o.get("is_required");
			Map<Locale, String> displayNames = parseLocaleMap((Map<String, String>) o.get("display_names"));
			Map<Locale, String> descriptions = parseLocaleMap((Map<String, String>) o.get("descriptions"));
			Map<Locale, String> defaultValues = parseLocaleMap((Map<String, String>) o.get("default_values"));

			RemoteLoggerConfigOption ro = new RemoteLoggerConfigOption(name, type, isRequired, displayNames,
					descriptions, defaultValues);
			l.add(ro);
		}
		return l;
	}

	private static Map<Locale, String> parseLocaleMap(Map<String, String> names) {
		Map<Locale, String> m = new HashMap<Locale, String>();
		for (String name : names.keySet()) {
			m.put(new Locale(name), names.get(name));
		}
		return m;
	}
}
