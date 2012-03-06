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
package org.krakenapps.fluxmon.impl;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.krakenapps.fluxmon.FluxDomain;
import org.krakenapps.fluxmon.FluxHost;

public class FluxDomainImpl implements FluxDomain {
	private String domain;
	private Date createDateTime;
	private Date updateDateTime;
	private Map<InetAddress, FluxHost> hosts;

	public FluxDomainImpl(String domain) {
		this(domain, new Date(), new Date(), new ConcurrentHashMap<InetAddress, FluxHost>());
	}

	public FluxDomainImpl(String domain, Date createDateTime, Date updateDateTime, Map<InetAddress, FluxHost> hosts) {
		this.domain = domain;
		this.createDateTime = createDateTime;
		this.updateDateTime = updateDateTime;
		this.hosts = hosts;
	}

	@Override
	public String getName() {
		return domain;
	}

	@Override
	public Date getCreateDateTime() {
		return createDateTime;
	}

	@Override
	public Date getUpdateDateTime() {
		return updateDateTime;
	}

	@Override
	public Collection<FluxHost> getHosts() {
		return Collections.unmodifiableCollection(hosts.values());
	}

	@Override
	public Collection<FluxHost> getActiveHosts(Date baseline) {
		List<FluxHost> l = new ArrayList<FluxHost>(hosts.size());
		for (FluxHost host : hosts.values())
			if (host.getUpdateDateTime().after(baseline))
				l.add(host);

		return l;
	}

	@Override
	public void updateHost(InetAddress address) {
		updateDateTime = new Date();
		FluxHost host = hosts.get(address);
		if (host == null)
			host = new FluxHostImpl(address);
		else
			host = new FluxHostImpl(address, host.getCreateDateTime(), new Date());

		hosts.put(address, host);
	}
}
