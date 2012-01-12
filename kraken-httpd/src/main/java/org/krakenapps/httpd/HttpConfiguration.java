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
import java.util.List;

public class HttpConfiguration {
	private InetSocketAddress listen;
	private boolean isSsl;
	private String keyAlias;
	private String trustAlias;
	private int maxContentLength = Integer.MAX_VALUE; // default 2G
	private List<VirtualHost> virtualHosts;

	public HttpConfiguration(InetSocketAddress listen) {
		this(listen, new ArrayList<VirtualHost>());
	}

	public HttpConfiguration(InetSocketAddress listen, List<VirtualHost> virtualHosts) {
		this(listen, virtualHosts, null, null);
		isSsl = false;
	}

	public HttpConfiguration(InetSocketAddress listen, List<VirtualHost> virtualHosts, String keyAlias,
			String trustAlias) {
		this.listen = listen;
		this.virtualHosts = virtualHosts;
		this.keyAlias = keyAlias;
		this.trustAlias = trustAlias;
		this.isSsl = true;
	}

	public InetSocketAddress getListenAddress() {
		return listen;
	}

	public boolean isSsl() {
		return isSsl;
	}

	public void addVirtualHost(VirtualHost host) {
		VirtualHost target = findVirtualHost(host.getHttpContextName());
		if (target != null)
			throw new IllegalStateException("duplicated http context exists: " + host.getHttpContextName());

		virtualHosts.add(host);
	}

	public void removeVirtualHost(String httpContextName) {
		VirtualHost target = findVirtualHost(httpContextName);
		if (target != null)
			virtualHosts.remove(target);
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
		return listen + " " + ssl + hosts;
	}
}
