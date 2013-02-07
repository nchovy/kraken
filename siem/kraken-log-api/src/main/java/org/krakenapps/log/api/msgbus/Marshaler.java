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
package org.krakenapps.log.api.msgbus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.krakenapps.log.api.LogParserFactory;
import org.krakenapps.log.api.Logger;
import org.krakenapps.log.api.LoggerConfigOption;
import org.krakenapps.log.api.LoggerFactory;

public class Marshaler {

	public static Map<String, Object> marshal(LoggerFactory factory, Locale locale) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("full_name", factory.getFullName());
		m.put("display_name", factory.getDisplayName(locale));
		m.put("namespace", factory.getNamespace());
		m.put("name", factory.getName());
		m.put("description", factory.getDescription(locale));
		return m;
	}

	public static Object marshal(LogParserFactory f, Locale locale) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("name", f.getName());
		m.put("display_name", f.getDisplayName(locale));
		m.put("description", f.getDescription(locale));
		m.put("options", marshal(f.getConfigOptions(), locale));
		return m;
	}

	public static Map<String, Object> marshal(Logger logger) {
		if (logger == null)
			return null;

		Map<String, Object> m = new HashMap<String, Object>();
		m.put("full_name", logger.getFullName());
		m.put("namespace", logger.getNamespace());
		m.put("name", logger.getName());
		m.put("factory_full_name", logger.getFactoryFullName());
		m.put("description", logger.getDescription());
		m.put("is_passive", logger.isPassive());
		m.put("interval", logger.getInterval());
		m.put("status", logger.isRunning() ? "running" : "stopped");
		m.put("last_start", dateFormatting(logger.getLastStartDate()));
		m.put("last_run", dateFormatting(logger.getLastRunDate()));
		m.put("last_log", dateFormatting(logger.getLastLogDate()));
		m.put("log_count", logger.getLogCount());
		return m;
	}

	public static Map<String, Object> marshal(LoggerConfigOption opt, Locale locale) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("name", opt.getName().replace('.', '_'));
		m.put("description", opt.getDescription(locale));
		m.put("display_name", opt.getDisplayName(locale));
		m.put("type", opt.getType());
		m.put("required", opt.isRequired());
		m.put("default_value", opt.getDefaultValue(locale));
		return m;
	}

	public static List<Object> marshal(Collection<?> list) {
		return marshal(list, null);
	}

	public static List<Object> marshal(Collection<?> list, Locale locale) {
		if (list == null)
			return null;

		List<Object> serializedObjects = new ArrayList<Object>();

		for (Object obj : list) {
			if (obj instanceof Logger)
				serializedObjects.add(marshal((Logger) obj));
			else if (obj instanceof LoggerFactory)
				serializedObjects.add(marshal((LoggerFactory) obj, locale));
			else if (obj instanceof LoggerConfigOption)
				serializedObjects.add(marshal((LoggerConfigOption) obj, locale));
		}

		return serializedObjects;
	}

	private static String dateFormatting(Date date) {
		if (date == null)
			return null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ssZ");
		return dateFormat.format(date);
	}

}
