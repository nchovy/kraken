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
package org.krakenapps.httpd;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.krakenapps.api.CollectionTypeHint;
import org.krakenapps.api.FieldOption;
import org.krakenapps.confdb.CollectionName;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.Predicates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CollectionName("http_configs")
public class HttpConfiguration {
	@FieldOption(skip = true)
	private final Logger logger = LoggerFactory.getLogger(HttpConfiguration.class.getName());

	private String listenAddress;

	@FieldOption(nullable = false)
	private int listenPort;

	@FieldOption(nullable = false)
	private boolean isSsl;

	private String keyAlias;

	private String trustAlias;

	@FieldOption(nullable = false)
	private int maxContentLength = Integer.MAX_VALUE >> 1; // default 1G

	@CollectionTypeHint(VirtualHost.class)
	private List<VirtualHost> virtualHosts;

	@FieldOption(skip = true)
	private ConfigService conf;

	// nullary constructor for confdb
	public HttpConfiguration() {
	}

	public HttpConfiguration(InetSocketAddress listen) {
		this(listen, new ArrayList<VirtualHost>());
	}

	public HttpConfiguration(InetSocketAddress listen, List<VirtualHost> virtualHosts) {
		this(listen, virtualHosts, null, null);
		isSsl = false;
	}

	public HttpConfiguration(InetSocketAddress listen, List<VirtualHost> virtualHosts, String keyAlias, String trustAlias) {
		this.listenAddress = listen.getAddress().getHostAddress();
		this.listenPort = listen.getPort();
		this.virtualHosts = virtualHosts;
		this.keyAlias = keyAlias;
		this.trustAlias = trustAlias;
		this.isSsl = true;
	}

	public void setConfigService(ConfigService conf) {
		this.conf = conf;
	}

	public InetSocketAddress getListenAddress() {
		return new InetSocketAddress(listenAddress, listenPort);
	}

	public boolean isSsl() {
		return isSsl;
	}

	public void addVirtualHost(VirtualHost host) {
		VirtualHost target = findVirtualHost(host.getHttpContextName());
		if (target != null)
			throw new IllegalStateException("duplicated http context exists: " + host.getHttpContextName());

		virtualHosts.add(host);

		// update confdb
		ConfigDatabase db = conf.ensureDatabase("kraken-httpd");
		Map<String, Object> filter = getFilter();
		Config c = db.findOne(HttpConfiguration.class, Predicates.field(filter));
		if (c != null) {
			db.update(c, this);
		} else {
			logger.error("kraken httpd: cannot find configuration for " + getListenAddress());
		}
	}

	public void removeVirtualHost(String httpContextName) {
		VirtualHost target = findVirtualHost(httpContextName);
		if (target != null) {
			virtualHosts.remove(target);

			// update confdb
			ConfigDatabase db = conf.ensureDatabase("kraken-httpd");
			Map<String, Object> filter = getFilter();
			Config c = db.findOne(HttpConfiguration.class, Predicates.field(filter));
			if (c != null) {
				HttpConfiguration conf = c.getDocument(HttpConfiguration.class);
				conf.removeVirtualHost(httpContextName);
				db.update(c, conf);
			}
		}
	}

	private Map<String, Object> getFilter() {
		Map<String, Object> filter = new HashMap<String, Object>();
		filter.put("listen_address", listenAddress);
		filter.put("listen_port", listenPort);
		return filter;
	}

	private VirtualHost findVirtualHost(String httpContextName) {
		VirtualHost target = null;
		for (VirtualHost h : virtualHosts)
			if (h.getHttpContextName().equals(httpContextName))
				target = h;
		return target;
	}

	public List<VirtualHost> getVirtualHosts() {
		return Collections.unmodifiableList(virtualHosts);
	}

	public String getKeyAlias() {
		return keyAlias;
	}

	public String getTrustAlias() {
		return trustAlias;
	}

	public int getMaxContentLength() {
		return maxContentLength;
	}

	public void setMaxContentLength(int maxContentLength) {
		this.maxContentLength = maxContentLength;
	}

	@Override
	public String toString() {
		String ssl = isSsl ? "(ssl: key " + keyAlias + ", trust " + trustAlias : "";
		String hosts = "\n";
		for (VirtualHost h : virtualHosts)
			hosts += "  " + h + "\n";
		return getListenAddress() + " " + ssl + hosts;
	}
}
