/*
 * Copyright 2009 NCHOVY
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
package org.krakenapps.http.internal;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpConfig {
	final Logger logger = LoggerFactory.getLogger(HttpConfig.class.getName());
	private Preferences prefs;

	public HttpConfig(Preferences prefs) {
		this.prefs = prefs.node("/kraken-http");
	}

	public List<HttpServiceConfig> getServers() {
		List<HttpServiceConfig> servers = new ArrayList<HttpServiceConfig>();

		try {
			Preferences serversNode = prefs.node("http_servers");

			for (String name : serversNode.childrenNames()) {
				HttpServiceConfig config = new HttpServiceConfig(name);

				Preferences n = serversNode.node(name);
				Preferences pp = n.node("properties");

				for (String key : pp.keys()) {
					config.setProperty(key, pp.get(key, null));
				}

				servers.add(config);
			}
		} catch (BackingStoreException e) {
			logger.warn("kraken http: get servers", e);
		}

		return servers;
	}

	public void addServer(HttpServiceConfig config) {
		Preferences servers = prefs.node("http_servers");
		try {
			if (servers.nodeExists(config.getName()))
				throw new IllegalStateException("duplicated http server name");

			Preferences newServer = servers.node(config.getName());
			Preferences props = newServer.node("properties");

			for (Object key : config.getProps().keySet()) {
				props.put((String) key, config.getProps().get(key));
			}

			newServer.flush();
			newServer.sync();

		} catch (BackingStoreException e) {
			logger.warn("kraken http: add server failed", e);
		}
	}

	public void removeServer(String name) {
		try {
			Preferences servers = prefs.node("http_servers");
			if (!servers.nodeExists(name))
				return;

			servers.node(name).removeNode();

			servers.flush();
			servers.sync();
		} catch (BackingStoreException e) {
			logger.warn("kraken http: remove server failed", e);
		}
	}
}
