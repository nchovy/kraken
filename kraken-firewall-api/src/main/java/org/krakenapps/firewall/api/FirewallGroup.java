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
package org.krakenapps.firewall.api;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FirewallGroup {
	private final Logger logger = LoggerFactory.getLogger(FirewallGroup.class.getName());
	private FirewallController controller;
	private String name;
	private Set<String> instanceNames;
	private ConcurrentMap<InetAddress, FirewallRule> blockedSources;
	private Set<FirewallGroupListener> callbacks;

	public FirewallGroup(FirewallController controller, String name) {
		this(controller, name, new ArrayList<String>(), new HashMap<InetAddress, FirewallRule>());
	}

	public FirewallGroup(FirewallController controller, String name, Collection<String> instanceNames,
			Map<InetAddress, FirewallRule> blockedSources) {
		this.controller = controller;
		this.name = name;
		this.instanceNames = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		this.instanceNames.addAll(instanceNames);
		this.blockedSources = new ConcurrentHashMap<InetAddress, FirewallRule>(blockedSources);
		this.callbacks = Collections.newSetFromMap(new ConcurrentHashMap<FirewallGroupListener, Boolean>());
	}

	public String getName() {
		return name;
	}

	public Collection<FirewallRule> getRules() {
		return new ArrayList<FirewallRule>(blockedSources.values());
	}

	public FirewallRule getRule(InetAddress ip) {
		return blockedSources.get(ip);
	}

	public boolean hasRule(InetAddress ip) {
		return blockedSources.containsKey(ip);
	}

	public void blockSourceIp(InetAddress ip) {
		Date expire = new Date(Long.MAX_VALUE);
		blockSourceIp(ip, expire);
	}

	public void blockSourceIp(InetAddress ip, int minutes) {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MINUTE, minutes);
		Date expire = c.getTime();
		blockSourceIp(ip, expire);
	}

	public void blockSourceIp(InetAddress ip, Date expire) {
		if (ip == null)
			throw new IllegalArgumentException("ip should not be null");

		FirewallRule rule = new FirewallRule(ip, expire);
		blockedSources.put(ip, rule);

		for (String instanceName : instanceNames) {
			logger.trace("kraken firewall api: add {} rule to {} instance", ip.getHostAddress(), instanceName);
			blockSourceIp(instanceName, ip);
		}

		for (FirewallGroupListener callback : callbacks) {
			try {
				callback.onBlock(this, rule);
			} catch (Exception e) {
				logger.warn("kraken firewall api: firewall group listener should not throw any exception", e);
			}
		}
	}

	private void blockSourceIp(String instanceName, InetAddress ip) {
		FirewallInstance instance = controller.getInstance(instanceName);
		if (instance == null)
			return;

		try {
			instance.blockSourceIp(ip);
		} catch (Exception e) {
			logger.warn("kraken firewall api: cannot block source ip " + ip.getHostAddress() + " to instance "
					+ instanceName, e);
		}
	}

	public void unblockSourceIp(InetAddress ip) {
		if (ip == null)
			throw new IllegalArgumentException("ip should not be null");

		FirewallRule rule = blockedSources.remove(ip);
		if (rule == null)
			return;

		for (String instanceName : instanceNames) {
			unblockSourceIp(instanceName, ip);
		}

		for (FirewallGroupListener callback : callbacks) {
			try {
				callback.onUnblock(this, rule);
			} catch (Exception e) {
				logger.warn("kraken firewall api: firewall group listener should not throw any exception", e);
			}
		}
	}

	private void unblockSourceIp(String instanceName, InetAddress ip) {
		FirewallInstance instance = controller.getInstance(instanceName);
		if (instance == null)
			return;

		try {
			if (!hasAlternativeRule(instanceName, ip))
				instance.unblockSourceIp(ip);
		} catch (Exception e) {
			logger.warn("kraken firewall api: cannot unblock source ip " + ip.getHostAddress() + " from instance "
					+ instanceName, e);
		}
	}

	public Collection<String> getMembers() {
		return Collections.unmodifiableCollection(instanceNames);
	}

	public boolean isMember(String instanceName) {
		return instanceNames.contains(instanceName);
	}

	public void join(String instanceName) {
		FirewallInstance instance = controller.getInstance(instanceName);

		if (instance == null)
			throw new IllegalStateException(instanceName + " instance not found");

		if (!instanceNames.add(instanceName)) {
			logger.trace("kraken firewall api: instance {} is already member of {}", instanceName, name);
			return;
		}

		applyBlacklist(instance);

		for (FirewallGroupListener callback : callbacks) {
			try {
				callback.onJoin(this, instanceName);
			} catch (Exception e) {
				logger.warn("kraken firewall api: firewall group listener should not throw any exception", e);
			}
		}
	}

	private void applyBlacklist(FirewallInstance instance) {
		logger.trace("kraken firewall api: applying [{}] blacklist to instance {}, rule count {}", new Object[] {
				getName(), instance.getName(), getRules().size() });

		// apply blocked source to this instance
		for (FirewallRule rule : getRules()) {
			instance.blockSourceIp(rule.getSourceIp());
		}
	}

	public void leave(String instanceName) {
		if (controller.getInstance(instanceName) == null)
			throw new IllegalStateException(instanceName + " instance not found");

		if (!instanceNames.remove(instanceName))
			return;

		for (FirewallRule rule : blockedSources.values()) {
			if (!hasAlternativeRule(instanceName, rule.getSourceIp()))
				unblockSourceIp(instanceName, rule.getSourceIp());
		}

		for (FirewallGroupListener callback : callbacks) {
			try {
				callback.onLeave(this, instanceName);
			} catch (Exception e) {
				logger.warn("kraken firewall api: firewall group listener should not throw any exception", e);
			}
		}
	}

	private boolean hasAlternativeRule(String instanceName, InetAddress ip) {
		for (FirewallGroup group : controller.getGroups()) {
			if (group == this)
				continue;

			if (group.isMember(instanceName) && group.hasRule(ip))
				return true;
		}

		return false;
	}

	public void onLoad(FirewallInstanceManager manager, FirewallInstance instance) {
		instanceNames.add(instance.getName());
		applyBlacklist(instance);
	}

	public void onUnload(FirewallInstanceManager manager, FirewallInstance instance) {
		instanceNames.remove(instance.getName());
	}

	public void addEventListener(FirewallGroupListener callback) {
		if (callback == null)
			throw new IllegalArgumentException("firewall group listener should be not null");

		callbacks.add(callback);
	}

	public void removeEventListener(FirewallGroupListener callback) {
		if (callback == null)
			throw new IllegalArgumentException("firewall group listener should be not null");

		callbacks.remove(callback);
	}

	@Override
	public String toString() {
		return String.format("name=%s, rules=%d", name, blockedSources.size());
	}
}
