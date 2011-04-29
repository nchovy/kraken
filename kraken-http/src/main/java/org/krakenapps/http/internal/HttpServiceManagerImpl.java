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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.krakenapps.http.HttpServiceManager;
import org.krakenapps.http.KrakenHttpService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServiceManagerImpl implements HttpServiceManager {
	final Logger logger = LoggerFactory.getLogger(HttpServiceManagerImpl.class.getName());
	private ConcurrentMap<String, JettyHttpService> serverMap;
	private HttpConfig httpConfig;
	private BundleContext bc;

	// new added
	private HttpServiceController controller;

	public HttpServiceManagerImpl(BundleContext bc) {
		this.bc = bc;
		this.serverMap = new ConcurrentHashMap<String, JettyHttpService>();

		controller = new HttpServiceController(bc);
	}

	public void validate() {
		ServiceReference ref = bc.getServiceReference(PreferencesService.class.getName());
		PreferencesService prefsService = (PreferencesService) bc.getService(ref);
		Preferences prefs = prefsService.getSystemPreferences();

		this.httpConfig = new HttpConfig(prefs);
		restoreHttpServers();
	}

	public void invalidate() {
		for (String serverId : serverMap.keySet()) {
			JettyHttpService server = serverMap.get(serverId);
			server.close();
		}
	}

	private void restoreHttpServers() {
		for (HttpServiceConfig serverConfig : httpConfig.getServers()) {
			try {
				String serverName = serverConfig.getName();
				openHttpService(serverName, serverConfig.getProps());
			} catch (Exception e) {
				logger.warn(e.toString());
			}
		}
	}

	@Override
	public List<String> getHttpServiceList() {
		List<String> serverList = new ArrayList<String>();
		for (String key : serverMap.keySet()) {
			serverList.add(key);
		}
		return serverList;
	}

	@Override
	public KrakenHttpService getHttpService(String name) {
		try {
			String filter = "(httpservice.name=" + name + ")";
			ServiceReference[] refs = bc.getServiceReferences(KrakenHttpService.class.getName(), filter);

			if (refs == null) {
				logger.error("kraken-http: http service [{}] not found", name);
				return null;
			}

			return (KrakenHttpService) bc.getService(refs[0]);
		} catch (InvalidSyntaxException e) {
			return null;
		}
	}

	@Override
	public void openHttpService(String serverId, Map<String, String> config) throws Exception {
		// duplicated id check
		JettyHttpService server = new JettyHttpService(new DispatcherServlet(controller), httpConfig, serverId, config);
		server.open();

		serverMap.put(serverId, server);

		HttpServiceConfig serverConfig = new HttpServiceConfig(serverId);
		serverConfig.setProps(server.getConfig());
		httpConfig.addServer(serverConfig);
	}

	@Override
	public Map<String, String> getConfig(String httpServiceName) {
		try {
			return serverMap.get(httpServiceName).getConfig();
		} catch (NullPointerException npe) {
			return null;
		}
	}

	@Override
	public void closeHttpService(String serverId) {
		JettyHttpService server = serverMap.remove(serverId);
		if (server == null)
			throw new IllegalStateException("http server not found:" + serverId);

		httpConfig.removeServer(serverId);
		server.close();
	}
}
