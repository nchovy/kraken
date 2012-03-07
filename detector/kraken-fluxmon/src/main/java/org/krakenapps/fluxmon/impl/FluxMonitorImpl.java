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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.cron.PeriodicJob;
import org.krakenapps.dns.DnsAnswer;
import org.krakenapps.dns.DnsReply;
import org.krakenapps.dns.DnsResolver;
import org.krakenapps.fluxmon.FluxDatabase;
import org.krakenapps.fluxmon.FluxDomain;
import org.krakenapps.fluxmon.FluxMonitor;

@PeriodicJob("* * * * *")
@Component(name = "flux-monitor")
@Provides
public class FluxMonitorImpl implements FluxMonitor, Runnable {
	@Requires
	private FluxDatabase database;
	private ExecutorService threadPool = Executors.newCachedThreadPool();
	private Set<String> trackingDomains;
	private Map<String, FluxDomain> domainMap;

	@Validate
	public void start() {
		trackingDomains = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		domainMap = new ConcurrentHashMap<String, FluxDomain>();

		for (String domain : database.loadTrackingDomains()) {
			trackingDomains.add(domain);
		}

		for (FluxDomain domain : database.loadFluxDomains()) {
			domainMap.put(domain.getName(), domain);
		}
	}

	@Override
	public void run() {
		try {
			threadPool.invokeAll(getTasks(), 1, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private Collection<DnsScanTask> getTasks() {
		List<DnsScanTask> tasks = new ArrayList<DnsScanTask>(trackingDomains.size());
		for (String domain : trackingDomains) {
			tasks.add(new DnsScanTask(domain));
		}
		return tasks;
	}

	@Override
	public Collection<String> getTrackingDomains() {
		return Collections.unmodifiableCollection(trackingDomains);
	}

	@Override
	public void addTrackingDomain(String domain) {
		database.addTrackingDomain(domain);
		trackingDomains.add(domain);
	}

	@Override
	public void removeTrackingDomain(String domain) {
		trackingDomains.remove(domain);
		database.removeTrackingDomain(domain);
	}

	@Override
	public Collection<FluxDomain> getAllDomains() {
		return Collections.unmodifiableCollection(domainMap.values());
	}

	@Override
	public FluxDomain getDomain(String domain) {
		return domainMap.get(domain);
	}

	@Override
	public Collection<FluxDomain> getActiveDomains(Date baseline) {
		List<FluxDomain> actives = new LinkedList<FluxDomain>();
		
		for (FluxDomain domain : domainMap.values()) {
			if (domain.getUpdateDateTime().after(baseline))
				actives.add(domain);
		}
		
		return actives;
	}

	private class DnsScanTask implements Callable<Void> {
		private String domain;

		public DnsScanTask(String domain) {
			this.domain = domain;
		}

		@Override
		public Void call() throws Exception {
			DnsResolver dnsResolver = new DnsResolver();
			DnsReply reply = dnsResolver.dig(new String[] { "IN", domain });

			FluxDomain fluxDomain = getDomain(domain);
			if (fluxDomain == null) {
				fluxDomain = new FluxDomainImpl(domain);
				domainMap.put(domain, fluxDomain);
			}

			for (DnsAnswer answer : reply.getAnswers()) {
				InetAddress domainAddress = InetAddress.getByName(answer.getDomainAddress());
				fluxDomain.updateHost(domainAddress);
			}

			database.updateFluxDomain(fluxDomain);
			return null;
		}

	}
}
