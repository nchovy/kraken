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
package org.krakenapps.logstorage.engine;

import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigUtil {
	public static String get(PreferencesService prefsvc, Constants key) {
		Preferences prefs = prefsvc.getSystemPreferences().node("/config");
		return prefs.get(key.getName(), null);
	}

	public static void set(PreferencesService prefsvc, Constants key, String value) {
		Logger logger = LoggerFactory.getLogger(ConfigUtil.class.getName());

		try {
			Preferences root = prefsvc.getSystemPreferences();
			Preferences config = root.node("/config");
			config.put(key.getName(), value);

			root.flush();
			root.sync();
		} catch (BackingStoreException e) {
			logger.error("log storage: cannot save property [{}={}]", key, value);
		}

	}
}
