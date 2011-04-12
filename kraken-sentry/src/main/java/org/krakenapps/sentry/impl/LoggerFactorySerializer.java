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
package org.krakenapps.sentry.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.krakenapps.log.api.Logger;
import org.krakenapps.log.api.LoggerConfigOption;
import org.krakenapps.log.api.LoggerFactory;

public class LoggerFactorySerializer {
	private LoggerFactorySerializer() {
	}

	public static Map<String, Object> toMap(Logger logger) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("logger_namespace", logger.getNamespace());
		m.put("logger_name", logger.getName());
		m.put("description", logger.getDescription());
		m.put("factory_name", logger.getFactoryName());
		m.put("factory_namespace", logger.getFactoryNamespace());
		m.put("is_running", logger.isRunning());
		m.put("interval", logger.getInterval());
		return m;
	}

	public static Map<String, Object> toMap(LoggerFactory f) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("name", f.getName());
		m.put("display_names", getDisplayNames(f));
		m.put("descriptions", getDescriptions(f));
		m.put("config_options", getConfigOptions(f));
		return m;
	}

	private static List<Object> getConfigOptions(LoggerFactory f) {
		List<Object> l = new ArrayList<Object>();
		for (LoggerConfigOption option : f.getConfigOptions()) {
			l.add(getConfigOption(option));
		}

		return l;
	}

	private static Map<String, Object> getConfigOption(LoggerConfigOption o) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("name", o.getName());
		m.put("type", o.getType());
		m.put("is_required", o.isRequired());
		m.put("display_names", getDisplayNames(o));
		m.put("descriptions", getDescriptions(o));
		m.put("default_values", getDefaultValues(o));
		return m;
	}

	private static Map<String, String> getDisplayNames(LoggerConfigOption o) {
		Map<String, String> m = new HashMap<String, String>();
		for (Locale locale : o.getDisplayNameLocales()) {
			m.put(locale.getLanguage(), o.getDisplayName(locale));
		}
		return m;
	}

	private static Map<String, String> getDescriptions(LoggerConfigOption o) {
		Map<String, String> m = new HashMap<String, String>();
		for (Locale locale : o.getDescriptionLocales()) {
			m.put(locale.getLanguage(), o.getDescription(locale));
		}
		return m;
	}
	
	private static Map<String, String> getDefaultValues(LoggerConfigOption o) {
		Map<String, String> m = new HashMap<String, String>();
		for (Locale locale : o.getDefaultValueLocales()) {
			m.put(locale.getLanguage(), o.getDefaultValue(locale));
		}
		return m;
	}

	private static Map<String, String> getDisplayNames(LoggerFactory f) {
		Map<String, String> m = new HashMap<String, String>();
		for (Locale locale : f.getDisplayNameLocales()) {
			String displayName = f.getDisplayName(locale);
			m.put(locale.getLanguage(), displayName);
		}
		return m;
	}

	private static Map<String, String> getDescriptions(LoggerFactory f) {
		Map<String, String> m = new HashMap<String, String>();
		for (Locale locale : f.getDescriptionLocales()) {
			String description = f.getDescription(locale);
			m.put(locale.getLanguage(), description);
		}
		return m;
	}

}