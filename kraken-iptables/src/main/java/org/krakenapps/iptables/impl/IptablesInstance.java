/*
 * Copyright 2011 NCHOVY
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
package org.krakenapps.iptables.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.krakenapps.firewall.api.FirewallInstance;
import org.krakenapps.firewall.api.FirewallInstanceManager;
import org.krakenapps.iptables.Chain;
import org.krakenapps.iptables.Iptables;
import org.krakenapps.iptables.NetworkAddress;
import org.krakenapps.iptables.Rule;
import org.krakenapps.iptables.RulePreset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IptablesInstance implements FirewallInstance {
	private final Logger logger = LoggerFactory.getLogger(IptablesInstance.class.getName());

	private FirewallInstanceManager manager;
	private Iptables iptables;
	private String name;
	private Properties config;

	private Set<InetAddress> blockedSources;

	public IptablesInstance(FirewallInstanceManager manager, Iptables iptables, String name, Properties config) {
		this.manager = manager;
		this.iptables = iptables;
		this.name = name;
		this.config = config;
		this.blockedSources = new HashSet<InetAddress>();
	}

	@Override
	public FirewallInstanceManager getInstanceManager() {
		return manager;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Collection<InetAddress> getBlockedSources() {
		return Collections.unmodifiableCollection(blockedSources);
	}

	@Override
	public synchronized void blockSourceIp(InetAddress ip) {
		try {
			Rule newRule = RulePreset.createSourceBlockRule(new NetworkAddress(ip.getHostAddress()));

			blockedSources.add(ip);
			
			// add block rule only if it doesn't exist
			for (Rule rule : iptables.getRules(Chain.INPUT))
				if (rule.equals(newRule))
					return;

			iptables.addRule(Chain.INPUT, 1, newRule);
			
		} catch (IOException e) {
			logger.error("kraken iptables: cannot block source ip " + ip.getHostAddress(), e);
		}
	}

	@Override
	public synchronized void unblockSourceIp(InetAddress ip) {
		try {
			int found = -1;

			Rule newRule = RulePreset.createSourceBlockRule(new NetworkAddress(ip.getHostAddress()));

			int index = 1;
			for (Rule rule : iptables.getRules(Chain.INPUT)) {
				if (rule.equals(newRule)) {
					found = index;
					break;
				}

				index++;
			}

			if (found >= 0)
				iptables.removeRule(Chain.INPUT, found);

			blockedSources.remove(ip);
		} catch (IOException e) {
			logger.error("kraken iptables: cannot unblock source ip " + ip.getHostAddress(), e);
		}
	}

	@Override
	public Properties getConfig() {
		return config;
	}

	@Override
	public String toString() {
		return String.format("iptables firewall - name [%s], rules [%d]", name, blockedSources.size());
	}

}
