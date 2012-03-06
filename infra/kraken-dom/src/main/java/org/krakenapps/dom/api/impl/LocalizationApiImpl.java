/*
 * Copyright 2011 Future Systems, Inc.
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
package org.krakenapps.dom.api.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.dom.api.LocalizationApi;
import org.krakenapps.dom.api.ResourceKey;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "dom-localization-api")
@Provides
public class LocalizationApiImpl implements LocalizationApi, BundleListener {
	private final Logger logger = LoggerFactory.getLogger(LocalizationApiImpl.class.getName());
	private BundleContext bc;
	private Map<ResourceKey, String> templates;

	public LocalizationApiImpl(BundleContext bc) {
		this.bc = bc;
		this.templates = new ConcurrentHashMap<ResourceKey, String>();
	}

	@Validate
	public void start() {
		bc.addBundleListener(this);
	}

	@Invalidate
	public void stop() {
		if (bc != null)
			bc.removeBundleListener(this);
	}

	@Override
	public String format(ResourceKey key, Object... args) {
		String template = templates.get(key);
		if (template == null)
			return null;

		return String.format(template, args);
	}

	@Override
	public String get(ResourceKey key) {
		return templates.get(key);
	}

	@Override
	public void register(ResourceKey key, String template) {
		templates.put(key, template);
	}

	@Override
	public void unregister(ResourceKey key) {
		templates.remove(key);
	}

	@Override
	public void bundleChanged(BundleEvent event) {
		if (event.getType() == BundleEvent.STARTED) {
			Map<ResourceKey, String> m = getLocalizations(event);
			if (m == null)
				return;

			for (ResourceKey key : m.keySet()) {
				templates.put(key, m.get(key));
				logger.trace("kraken dom: added [{}] resource key", key);
			}

		} else if (event.getType() == BundleEvent.STOPPED) {
			Map<ResourceKey, String> m = getLocalizations(event);
			if (m == null)
				return;

			for (ResourceKey key : m.keySet()) {
				templates.remove(key);
				logger.trace("kraken dom: removed [{}] resource key", key);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Map<ResourceKey, String> getLocalizations(BundleEvent event) {
		Map<ResourceKey, String> m = new HashMap<ResourceKey, String>();

		Bundle bundle = event.getBundle();
		Enumeration<String> e = bundle.getEntryPaths("/OSGI-INF/kraken-dom/");
		if (e == null) {
			logger.trace("kraken dom: localization not found for bundle {}", event.getBundle().getBundleId());
			return null;
		}

		while (e.hasMoreElements()) {
			String path = e.nextElement();
			if (path.startsWith("OSGI-INF/kraken-dom/localization.") && path.endsWith(".properties")) {
				String token = path.replace("OSGI-INF/kraken-dom/localization.", "").replace(".properties", "");
				if (token.length() != 2)
					continue;

				Locale locale = new Locale(token);
				InputStream is = null;
				InputStreamReader reader = null;
				try {
					URL url = bundle.getEntry(path);
					Properties p = new Properties();
					is = url.openStream();
					reader = new InputStreamReader(is, Charset.forName("utf-8"));

					p.load(reader);

					String group = p.getProperty("group");
					for (Object key : p.keySet()) {
						if (key.equals("group"))
							continue;

						ResourceKey resourceKey = new ResourceKey(group, (String) key, locale);
						String template = p.getProperty((String) key);
						m.put(resourceKey, template);
					}
				} catch (IOException ex) {
					logger.error("kraken dom: cannot read localization properties", ex);
				} finally {
					if (reader != null)
						try {
							reader.close();
						} catch (IOException e2) {
						}

					if (is != null)
						try {
							is.close();
						} catch (IOException e1) {
						}
				}
			}
		}

		return m;
	}
}
