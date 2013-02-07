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
package org.krakenapps.querybrowser.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.querybrowser.ConnectionStringRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "connection-string-registry")
@Provides
public class ConnectionStringRegistryImpl implements ConnectionStringRegistry {
	private static final String DRIVER = "driver";
	private static final String CONNECTION_STRING = "connection_string";
	private final Logger logger = LoggerFactory.getLogger(ConnectionStringRegistryImpl.class.getName());
	private final Preferences prefs;

	public ConnectionStringRegistryImpl(BundleContext bc) {
		ServiceReference ref = bc.getServiceReference(PreferencesService.class.getName());
		PreferencesService prefsvc = (PreferencesService) bc.getService(ref);
		prefs = prefsvc.getSystemPreferences();
	}

	@Override
	public Collection<String> getNames() {
		Preferences root = getConnectionNode();
		try {
			return Arrays.asList(root.childrenNames());
		} catch (BackingStoreException e) {
			logger.warn("kraken query browser: failed to get connection names", e);
		}
		return null;
	}

	@Override
	public Properties getConnectionString(String name) {
		Properties props = new Properties();
		Preferences root = getConnectionNode();
		try {
			if (!root.nodeExists(name))
				return null;

			Preferences node = root.node(name);
			String conn = node.get(CONNECTION_STRING, null);
			String driver = node.get(DRIVER, null);

			props.put(CONNECTION_STRING, conn);
			props.put(DRIVER, driver);

			return props;
		} catch (BackingStoreException e) {
			logger.warn("kraken query browser: failed to build connection string", e);
		}
		return null;
	}

	@Override
	public void setConnectionString(String name, Properties props) {
		Preferences root = getConnectionNode();
		try {
			if (root.nodeExists(name))
				throw new IllegalStateException("duplicated connection string name");

			Preferences p = root.node(name);
			p.put(CONNECTION_STRING, props.getProperty(CONNECTION_STRING));
			p.put(DRIVER, props.getProperty(DRIVER));

			p.flush();
			p.sync();
		} catch (BackingStoreException e) {
			logger.warn("kraken query browser: failed to persist connection string", e);
		}
	}

	@Override
	public void removeConnectionString(String name) {
		Preferences root = getConnectionNode();

		try {
			if (!root.nodeExists(name))
				throw new IllegalStateException("connection string not found");

			root.node(name).removeNode();
			root.flush();
			root.sync();
		} catch (BackingStoreException e) {
			logger.warn("kraken query browser: failed to remove connection string", e);
		}
	}

	private Preferences getConnectionNode() {
		return prefs.node("connection_strings");
	}
}
