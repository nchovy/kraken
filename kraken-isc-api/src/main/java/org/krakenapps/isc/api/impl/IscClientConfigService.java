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
package org.krakenapps.isc.api.impl;

import java.util.concurrent.CopyOnWriteArraySet;

import org.krakenapps.isc.api.IscClientConfig;
import org.krakenapps.isc.api.IscClientEventListener;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IscClientConfigService implements IscClientConfig {
	private final Logger logger = LoggerFactory.getLogger(IscClientConfigService.class.getName());

	private PreferencesService prefsvc;

	private String apiKey;

	private CopyOnWriteArraySet<IscClientEventListener> callbacks;

	public void start() {
		Preferences p = prefsvc.getSystemPreferences();
		this.apiKey = p.get("apikey", null);
		this.callbacks = new CopyOnWriteArraySet<IscClientEventListener>();
	}

	@Override
	public String getApiKey() {
		return apiKey;
	}

	@Override
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;

		try {
			Preferences p = prefsvc.getSystemPreferences();
			if (apiKey != null)
				p.put("apikey", apiKey);
			else
				p.remove("apikey");
			
			p.flush();
			p.sync();
		} catch (BackingStoreException e) {
			logger.error("kraken isc api: cannot save api key", e);
		}

		for (IscClientEventListener callback : callbacks) {
			try {
				callback.onConfigure(apiKey);
			} catch (Exception e) {
				logger.warn("kraken isc api: callback should not throw any exception", e);
			}
		}
	}

	@Override
	public void addEventListener(IscClientEventListener callback) {
		callbacks.add(callback);
	}

	@Override
	public void removeEventListener(IscClientEventListener callback) {
		callbacks.remove(callback);
	}
}
