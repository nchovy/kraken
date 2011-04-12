package org.krakenapps.sleepproxy.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.sleepproxy.ConfigStore;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "sleep-proxy-config-provider")
@Provides
public class ConfigProvider implements ConfigStore {
	private final Logger logger = LoggerFactory.getLogger(ConfigProvider.class.getName());

	@Requires
	private PreferencesService prefsvc;

	@Override
	public String get(String key) {
		return get(key, null);
	}

	@Override
	public String get(String key, String def) {
		return getRoot().get(key, def);
	}

	@Override
	public void set(String key, String value) {
		try {
			Preferences root = getRoot();
			root.put(key, value);
			root.flush();
			root.sync();
		} catch (BackingStoreException e) {
			logger.error("sleep proxy: cannot save config [" + key + "]");
		}
	}

	@Override
	public void delete(String key) {
		try {
			Preferences root = getRoot();
			root.remove(key);
			root.flush();
			root.sync();
		} catch (BackingStoreException e) {
			logger.error("sleep proxy: cannot save config [" + key + "]");
		}
	}

	private Preferences getRoot() {
		return prefsvc.getSystemPreferences();
	}
}
