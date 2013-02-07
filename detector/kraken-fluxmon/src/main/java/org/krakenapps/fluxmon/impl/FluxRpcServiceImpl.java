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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.fluxmon.FluxDomain;
import org.krakenapps.fluxmon.FluxHost;
import org.krakenapps.fluxmon.FluxMonitor;
import org.krakenapps.fluxmon.FluxRpcService;
import org.krakenapps.geoip.GeoIpService;
import org.krakenapps.servlet.xmlrpc.XmlRpcMethod;

@Component(name = "flux-rpc-service")
@Provides
public class FluxRpcServiceImpl implements FluxRpcService {
	private FluxMonitor monitor;
	private GeoIpService geoip;

	@XmlRpcMethod(alias = "flux", method = "all_domains")
	public List<Object> getAllDomains() {
		Collection<FluxDomain> fluxDomains = monitor.getAllDomains();
		return marshalDomains(fluxDomains);
	}

	@XmlRpcMethod(alias = "flux", method = "active_domains")
	public List<Object> getActiveDomains(int minutes) {
		Date baseline = getBaseline(minutes);
		Collection<FluxDomain> fluxDomains = monitor.getActiveDomains(baseline);
		return marshalDomains(fluxDomains);
	}

	private Date getBaseline(int minutes) {
		Date baseline = new Date(new Date().getTime() - minutes * 60 * 1000);
		return baseline;
	}

	@XmlRpcMethod(alias = "flux", method = "hosts")
	public List<Object> getHosts(String domain) {
		FluxDomain fluxDomain = monitor.getDomain(domain);
		if (fluxDomain == null)
			return new ArrayList<Object>();
		
		return marshalHosts(fluxDomain.getHosts());
	}

	@XmlRpcMethod(alias = "flux", method = "top_countries")
	public List<Object> getTopCountries(int limit, int minutes) {
		Date baseline = getBaseline(minutes);
		List<FluxCountryStat> stats = FluxStatistics.getSortedCountryStats(monitor, geoip, baseline, null);
		limit = limit > stats.size() ? stats.size() : limit;
		return marshalCountryStat(stats.subList(0, limit));
	}

	@XmlRpcMethod(alias = "flux", method = "top_networks")
	public List<Object> getTopNetworks() {
		return null;
	}
	
	private List<Object> marshalCountryStat(Collection<FluxCountryStat> stats) {
		List<Object> l = new ArrayList<Object>(stats.size());
		for (FluxCountryStat stat : stats) {
			l.add(marshal(stat));
		}
		return l;
	}
	
	private Map<String, Object> marshal(FluxCountryStat stat) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("country", stat.getName());
		m.put("count", stat.getCount());
		return m;
	}
	
	private List<Object> marshalHosts(Collection<FluxHost> hosts) {
		List<Object> l = new ArrayList<Object>(hosts.size());
		for (FluxHost host : hosts) {
			l.add(marshal(host));
		}
		return l;
	}
	
	private Map<String, Object> marshal(FluxHost host) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("ip", host.getAddress().getHostAddress());
		m.put("created_at", dateFormat.format(host.getCreateDateTime()));
		m.put("updated_at", dateFormat.format(host.getUpdateDateTime()));
		return m;
	}

	private List<Object> marshalDomains(Collection<FluxDomain> domains) {
		List<Object> l = new ArrayList<Object>(domains.size());
		for (FluxDomain domain : domains) {
			l.add(marshal(domain));
		}
		return l;
	}

	private Map<String, Object> marshal(FluxDomain domain) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("name", domain.getName());
		m.put("created_at", dateFormat.format(domain.getCreateDateTime()));
		m.put("updated_at", dateFormat.format(domain.getUpdateDateTime()));
		m.put("count", domain.getHosts().size());
		return m;
	}
}
