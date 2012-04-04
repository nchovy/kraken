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
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import org.krakenapps.api.CollectionTypeHint;
import org.krakenapps.api.FieldOption;
import org.krakenapps.confdb.CollectionName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CollectionName("http_configs")
public class HttpConfiguration {
	public static final int IDLE_TIMEOUT_DEFAULT = 120;

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

	private int idleTimeout;

	private String defaultHttpContext;

	@CollectionTypeHint(VirtualHost.class)
	private List<VirtualHost> virtualHosts;

	@FieldOption(skip = true)
	private CopyOnWriteArraySet<HttpConfigurationListener> listeners = new CopyOnWriteArraySet<HttpConfigurationListener>();

	// nullary constructor for confdb
	public HttpConfiguration() {
		this.idleTimeout = IDLE_TIMEOUT_DEFAULT;
	}

	public HttpConfiguration(InetSocketAddress listen) {
		this(listen, null, null);
		isSsl = false;
	}

	public HttpConfiguration(InetSocketAddress listen, String keyAlias, String trustAlias) {
		this.listenAddress = listen.getAddress().getHostAddress();
		this.listenPort = listen.getPort();
		this.keyAlias = keyAlias;
		this.trustAlias = trustAlias;
		this.isSsl = true;
		this.virtualHosts = new ArrayList<VirtualHost>();
	}

	public InetSocketAddress getListenAddress() {
		return new InetSocketAddress(listenAddress, listenPort);
	}

	public boolean isSsl() {
		return isSsl;
	}

	public List<VirtualHost> getVirtualHosts() {
		return virtualHosts;
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
		notifyChange("maxContentLength", maxContentLength);
	}

	public int getIdleTimeout() {
		return idleTimeout;
	}

	public void setIdleTimeout(int idleTimeout) {
		this.idleTimeout = idleTimeout;
		notifyChange("idleTimeout", idleTimeout);
	}

	public String getDefaultHttpContext() {
		return defaultHttpContext;
	}

	public void setDefaultHttpContext(String defaultHttpContext) {
		this.defaultHttpContext = defaultHttpContext;
	}

	public CopyOnWriteArraySet<HttpConfigurationListener> getListeners() {
		return listeners;
	}

	public void setListeners(CopyOnWriteArraySet<HttpConfigurationListener> listeners) {
		this.listeners = listeners;
	}

	private void notifyChange(String fieldName, Object value) {
		for (HttpConfigurationListener listener : listeners) {
			try {
				listener.onSet(fieldName, value);
			} catch (Throwable t) {
				logger.error("kraken httpd: http config listener should not throw any exception", t);
			}
		}
	}

	@Override
	public String toString() {
		String ssl = isSsl ? "(ssl: key " + keyAlias + ", trust " + trustAlias + ")" : "";
		String hosts = "\n";
		for (VirtualHost h : virtualHosts)
			hosts += "  " + h + ", idle timeout: " + this.idleTimeout + "seconds";

		String information = getListenAddress() + " " + ssl + hosts;
		if (getDefaultHttpContext() == null)
			return information;

		return information + ", default context:  " + getDefaultHttpContext();
	}
}
