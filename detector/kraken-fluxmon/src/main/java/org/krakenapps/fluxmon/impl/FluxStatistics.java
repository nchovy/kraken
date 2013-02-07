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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.krakenapps.fluxmon.FluxDomain;
import org.krakenapps.fluxmon.FluxHost;
import org.krakenapps.fluxmon.FluxMonitor;
import org.krakenapps.geoip.GeoIpLocation;
import org.krakenapps.geoip.GeoIpService;

public class FluxStatistics {
	private FluxStatistics() {
	}
	
	public static List<FluxNetworkStat> getSortedNetworkStats(FluxMonitor monitor, Date baseline) {
		Map<String, Integer> m = new HashMap<String, Integer>();
		
		for (FluxDomain domain : monitor.getAllDomains()) {
			m.put(domain.getName(), domain.getActiveHosts(baseline).size());
		}
		
		List<FluxNetworkStat> stats = new ArrayList<FluxNetworkStat>(m.size());
		for (String domain : m.keySet()) {
			stats.add(new FluxNetworkStat(domain, m.get(domain)));
		}
		
		Collections.sort(stats);
		return stats;
	}

	public static List<FluxCountryStat> getSortedCountryStats(FluxMonitor monitor, GeoIpService geoip, Date baseline, String domainFilter) {
		Map<String, Integer> m = new HashMap<String, Integer>();

		if (domainFilter != null) {
			FluxDomain fluxDomain = monitor.getDomain(domainFilter);
			countHosts(monitor, geoip, m, baseline, fluxDomain);
		}
		else
			for (FluxDomain fluxDomain : monitor.getAllDomains())
				countHosts(monitor, geoip, m, baseline, fluxDomain);

		List<FluxCountryStat> stats = new ArrayList<FluxCountryStat>(m.size());
		for (String code : m.keySet()) {
			stats.add(new FluxCountryStat(code, m.get(code)));
		}

		Collections.sort(stats);
		return stats;
	}

	private static void countHosts(FluxMonitor monitor, GeoIpService geoip, Map<String, Integer> m, Date limit, FluxDomain fluxDomain) {
		if (fluxDomain == null)
			return;
		
		for (FluxHost host : fluxDomain.getHosts()) {
			if (host.getUpdateDateTime().before(limit))
				continue;
			
			GeoIpLocation location = geoip.locate(host.getAddress());
			if (location == null)
				continue;

			String country = location.getCountry();
			if (m.containsKey(country)) {
				m.put(country, m.get(country) + 1);
			} else {
				m.put(country, 1);
			}
		}
	}

}
