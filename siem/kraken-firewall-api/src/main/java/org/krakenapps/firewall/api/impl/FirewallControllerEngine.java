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
package org.krakenapps.firewall.api.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.firewall.api.DefaultFirewallInstanceManagerListener;
import org.krakenapps.firewall.api.FirewallController;
import org.krakenapps.firewall.api.FirewallGroup;
import org.krakenapps.firewall.api.FirewallGroupListener;
import org.krakenapps.firewall.api.FirewallInstance;
import org.krakenapps.firewall.api.FirewallInstanceManager;
import org.krakenapps.firewall.api.FirewallRule;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "firewall-controller")
@Provides(specifications = { FirewallController.class })
public class FirewallControllerEngine extends DefaultFirewallInstanceManagerListener implements FirewallController,
		FirewallGroupListener {
	private final Logger logger = LoggerFactory.getLogger(FirewallControllerEngine.class.getName());
	private static final String RULES = "rules";
	private static final String MEMBERS = "members";
	private static final String INSTANCES = "instances";

	private FirewallInstanceManagerTracker tracker;
	private Unblocker unblocker;
	private Thread unblockerThread;

	private ConcurrentMap<String, FirewallInstanceManager> instanceManagers;
	private ConcurrentMap<String, FirewallInstance> instances;
	private ConcurrentMap<String, FirewallGroup> groups;

	@Requires
	private PreferencesService prefsvc;

	public FirewallControllerEngine(BundleContext bc) {
		instanceManagers = new ConcurrentHashMap<String, FirewallInstanceManager>();
		instances = new ConcurrentHashMap<String, FirewallInstance>();
		groups = new ConcurrentHashMap<String, FirewallGroup>();
		tracker = new FirewallInstanceManagerTracker(bc, this);
		unblocker = new Unblocker();
	}

	@Validate
	public void start() {
		try {
			Preferences root = prefsvc.getSystemPreferences();

			// initialize
			root.node(MEMBERS);
			root.node(RULES);
			root.flush();
			root.sync();

			loadGroups();

			if (getGroup("all") == null)
				createGroup("all");

		} catch (BackingStoreException e) {
			logger.error("kraken firewall api: cannot initialize preferences");
		}

		// begin monitor tracking
		tracker.open();

		// begin unblocker
		unblockerThread = new Thread(unblocker, "Firewall Unblocker");
		unblockerThread.start();
	}

	private void loadGroups() {
		try {
			Preferences memberRoot = getMemberConfig();
			Preferences ruleRoot = getRuleConfig();

			for (String groupName : memberRoot.childrenNames()) {
				FirewallGroup group = new FirewallGroup(this, groupName);
				Preferences memberGroupNode = memberRoot.node(groupName);
				Preferences ruleGroupNode = ruleRoot.node(groupName);

				for (String ip : ruleGroupNode.childrenNames()) {
					try {
						Preferences ruleNode = ruleGroupNode.node(ip);
						InetAddress ipAddress = InetAddress.getByName(ip);
						Date expire = new Date(ruleNode.getLong("expire", 0));
						group.blockSourceIp(ipAddress, expire);

						if (logger.isDebugEnabled())
							logger.debug("kraken firewall api: loading group {}, ip {}, expire {}", new Object[] {
									groupName, ip, expire });
					} catch (UnknownHostException e) {
						logger.error("kraken firewall api: cannot convert ip {}", ip);
					}
				}

				for (String instanceName : memberGroupNode.childrenNames()) {
					try {
						group.join(instanceName);
					} catch (Exception e) {
						logger.trace("kraken firewall api: cannot join group " + group.getName() + ", instance "
								+ instanceName, e);
					}
				}

				groups.put(group.getName(), group);

				// add event listener here to skip join event callback
				group.addEventListener(this);
			}
		} catch (BackingStoreException e) {
			logger.error("kraken firewall api: cannot load groups", e);
		}
	}

	@Invalidate
	public void stop() {
		try {
			if (unblocker != null && unblockerThread != null) {
				// stop unblocker
				unblocker.doStop = true;
				unblockerThread.interrupt();
				unblockerThread.join(5000);
			}
		} catch (InterruptedException e) {
		}

		tracker.close();
	}

	@Override
	public Collection<FirewallInstanceManager> getInstanceManagers() {
		return instanceManagers.values();
	}

	@Override
	public FirewallInstanceManager getInstanceManager(String name) {
		if (name == null)
			return null;

		return instanceManagers.get(name);
	}

	@Override
	public Collection<FirewallGroup> getGroups() {
		return groups.values();
	}

	@Override
	public FirewallGroup getGroup(String groupName) {
		if (groupName == null)
			throw new IllegalArgumentException("group name should be not null");

		return groups.get(groupName);
	}

	@Override
	public FirewallGroup createGroup(String groupName) {
		try {
			Preferences root = getMemberConfig();
			if (root.nodeExists(groupName))
				throw new IllegalStateException(groupName + " already exists");

			root.node(groupName);
			root.flush();
			root.sync();

			FirewallGroup group = new FirewallGroup(this, groupName);
			group.addEventListener(this);
			groups.put(groupName, group);
			return group;
		} catch (BackingStoreException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void removeGroup(String groupName) {
		try {
			Preferences memberRoot = getMemberConfig();
			if (!memberRoot.nodeExists(groupName))
				throw new IllegalStateException(groupName + " not found");

			memberRoot.node(groupName).removeNode();
			memberRoot.flush();
			memberRoot.sync();

			Preferences ruleRoot = getRuleConfig();
			ruleRoot.node(groupName).removeNode();
			ruleRoot.flush();
			ruleRoot.sync();

			FirewallGroup group = groups.remove(groupName);
			group.removeEventListener(this);

		} catch (BackingStoreException e) {
			throw new IllegalStateException(e);
		}
	}

	public void register(FirewallInstanceManager manager) {
		logger.trace("kraken firewall api: registering firewall instance manager {}", manager.getName());

		// add event hook
		manager.addEventListener(this);

		// add to registry
		FirewallInstanceManager old = instanceManagers.putIfAbsent(manager.getName(), manager);
		if (old != null)
			throw new IllegalStateException("instance manager " + manager.getName() + " already exists");

		// load all configured instances
		loadInstancesAutomatically(manager);
	}

	private void loadInstancesAutomatically(FirewallInstanceManager manager) {
		// load configured instances
		try {
			Preferences instanceRoot = getInstanceConfig();
			for (String instanceName : instanceRoot.childrenNames()) {
				Preferences instanceNode = instanceRoot.node(instanceName);
				Preferences configNode = instanceNode.node("config");
				Properties config = parseConfig(configNode);

				String managerName = instanceNode.get("manager", null);
				if (!managerName.equals(manager.getName()))
					continue;

				FirewallInstance instance = instances.get(instanceName);
				if (instance == null) {
					logger.trace("kraken firewall api: creating firewall instance {} automatically", instanceName);
					instance = manager.createInstance(instanceName, config);
					instances.put(instanceName, instance);
				}
			}

			Preferences memberRoot = getMemberConfig();
			for (String groupName : memberRoot.childrenNames()) {
				Preferences groupNode = memberRoot.node(groupName);
				for (String instanceName : groupNode.childrenNames()) {
					FirewallInstance instance = instances.get(instanceName);
					if (instance == null)
						continue;

					FirewallGroup group = groups.get(groupName);
					if (group != null) {
						group.onLoad(manager, instance);
					}
				}
			}
		} catch (BackingStoreException e) {
			logger.error("kraken firewall api: cannot load firewall instances of manager " + manager.getName(), e);
		}
	}

	private Properties parseConfig(Preferences config) {
		Properties props = new Properties();
		try {
			for (String key : config.keys()) {
				String value = config.get(key, null);
				props.setProperty(key, value);
			}
		} catch (BackingStoreException e) {
			logger.error("kraken firewall api: ");
		}
		return props;
	}

	public void unregister(FirewallInstanceManager manager) {
		logger.trace("kraken firewall api: unregistering firewall instance manager {}", manager.getName());

		// remove event hook
		manager.removeEventListener(this);

		// remove all instances from groups
		for (FirewallGroup group : getGroups()) {
			for (FirewallInstance instance : manager.getInstances())
				group.onUnload(manager, instance);
		}

		// remove from instance registry
		for (FirewallInstance instance : manager.getInstances())
			instances.remove(instance.getName());

		// remove from manager registry
		FirewallInstanceManager old = instanceManagers.get(manager.getName());
		if (old == manager)
			instanceManagers.remove(manager.getName());
	}

	@Override
	public Collection<FirewallInstance> getInstances() {
		return new ArrayList<FirewallInstance>(instances.values());
	}

	@Override
	public FirewallInstance getInstance(String name) {
		if (name == null)
			return null;

		return instances.get(name);
	}

	@Override
	public void register(FirewallInstance instance) {
		logger.trace("kraken firewall api: registering firewall instance {}", instance.getName());

		FirewallInstance old = instances.putIfAbsent(instance.getName(), instance);
		if (old != null)
			throw new IllegalStateException("duplicated instance name exists: " + instance.getName());
	}

	@Override
	public void unregister(FirewallInstance instance) {
		logger.trace("kraken firewall api: unregistering firewall instance {}", instance.getName());

		// remove from instance registry
		FirewallInstance old = instances.remove(instance.getName());
		if (old != instance)
			instances.put(old.getName(), old);
	}

	//
	// FirewallInstanceManagerListener
	//

	@Override
	public void onCreate(FirewallInstanceManager manager, FirewallInstance instance) {
		// create persistent config
		try {
			Preferences instanceRoot = getInstanceConfig();
			Preferences instanceNode = instanceRoot.node(instance.getName());

			instanceNode.put("manager", manager.getName());

			instanceRoot.flush();
			instanceRoot.sync();
		} catch (BackingStoreException e) {
			logger.error("kraken firewall api: cannot create persistent instance config for " + instance.getName(), e);
		}

		instances.putIfAbsent(instance.getName(), instance);
		FirewallGroup allGroup = getGroup("all");
		allGroup.join(instance.getName());
		instances.remove(instance.getName());
	}

	@Override
	public void onRemove(FirewallInstanceManager manager, FirewallInstance instance) {
		try {
			// leave all groups
			for (FirewallGroup group : groups.values()) {
				group.leave(instance.getName());
			}

			// remove persistent config
			Preferences instanceRoot = getInstanceConfig();
			instanceRoot.node(instance.getName()).removeNode();
			instanceRoot.flush();
			instanceRoot.sync();

			logger.trace("kraken firewall api: removed persistent instance [{}] config", instance.getName());
		} catch (BackingStoreException e) {
			logger.error("kraken firewall api: cannot remove persistent instance config for " + instance.getName());
		}

		// invoke callbacks
		for (FirewallGroup group : getGroups()) {
			group.leave(instance.getName());
		}
	}

	//
	// FirewallGroupListener
	//

	@Override
	public void onJoin(FirewallGroup group, String instanceName) {
		try {
			logger.trace("kraken firewall api: join group {}, member {}", group.getName(), instanceName);

			Preferences memberRoot = getMemberConfig();
			Preferences p = memberRoot.node(group.getName());
			p.node(instanceName);

			memberRoot.flush();
			memberRoot.sync();
		} catch (BackingStoreException e) {
			logger.error("kraken firewall api: cannot join group {}, {}", group.getName(), instanceName);
		}
	}

	@Override
	public void onLeave(FirewallGroup group, String instanceName) {
		try {
			logger.trace("kraken firewall api: leave group {}, member {}", group.getName(), instanceName);

			Preferences memberRoot = getMemberConfig();
			Preferences p = memberRoot.node(group.getName());
			p.node(instanceName).removeNode();

			memberRoot.flush();
			memberRoot.sync();
		} catch (BackingStoreException e) {
			logger.error("kraken firewall api: cannot leave group {}, {}", group.getName(), instanceName);
		}
	}

	@Override
	public void onBlock(FirewallGroup group, FirewallRule rule) {
		try {
			logger.trace("kraken firewall api: block group {}, rule {}", group.getName(), rule);

			Preferences rootNode = getRuleConfig();
			Preferences groupNode = rootNode.node(group.getName());
			String ip = rule.getSourceIp().getHostAddress();
			Preferences p = groupNode.node(ip);
			p.putLong("expire", rule.getExpire().getTime());

			rootNode.flush();
			rootNode.sync();
		} catch (BackingStoreException e) {
			logger.error("kraken firewall api: cannot block [group {}, ip {}]", group.getName(), rule.getSourceIp());
		}
	}

	@Override
	public void onUnblock(FirewallGroup group, FirewallRule rule) {
		try {
			logger.trace("kraken firewall api: unblock group {}, rule {}", group.getName(), rule);

			Preferences rootNode = getRuleConfig();
			Preferences groupNode = rootNode.node(group.getName());
			String ip = rule.getSourceIp().getHostAddress();
			groupNode.node(ip).removeNode();

			rootNode.flush();
			rootNode.sync();
		} catch (BackingStoreException e) {
			logger.error("kraken firewall api: cannot unblock [group {}, ip {}]", group.getName(), rule.getSourceIp());
		}
	}

	private Preferences getInstanceConfig() {
		if (prefsvc == null)
			throw new IllegalStateException("PreferencesService not found");

		return prefsvc.getSystemPreferences().node(INSTANCES);
	}

	private Preferences getMemberConfig() {
		if (prefsvc == null)
			throw new IllegalStateException("PreferencesService not found");

		return prefsvc.getSystemPreferences().node(MEMBERS);
	}

	private Preferences getRuleConfig() {
		if (prefsvc == null)
			throw new IllegalStateException("PreferencesService not found");

		return prefsvc.getSystemPreferences().node(RULES);
	}

	private class Unblocker implements Runnable {
		private volatile boolean doStop = false;

		@Override
		public void run() {
			while (!doStop) {
				try {
					Thread.sleep(10000);
					Date now = new Date();

					for (FirewallGroup group : groups.values()) {
						for (FirewallRule rule : group.getRules()) {
							logger.debug("kraken firewall api: inspecting expiration of rule [{}]", rule);
							if (now.after(rule.getExpire())) {
								logger.trace("kraken firewall api: unblock rule [{}]", rule);
								group.unblockSourceIp(rule.getSourceIp());
							}
						}
					}
				} catch (InterruptedException e) {
					logger.info("kraken firewall api: interrupted");
				} catch (Exception e) {
					logger.error("kraken firewall api: unblocker error", e);
				}
			}

			doStop = false;
			logger.info("kraken firewall api: unblocker stopped");
		}
	}
}
