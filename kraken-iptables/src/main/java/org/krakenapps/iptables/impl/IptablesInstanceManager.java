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

import java.util.Properties;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.firewall.api.DefaultFirewallInstanceManager;
import org.krakenapps.firewall.api.FirewallController;
import org.krakenapps.firewall.api.FirewallGroup;
import org.krakenapps.firewall.api.FirewallInstance;
import org.krakenapps.iptables.Iptables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "iptables-instance-manager")
@Provides
public class IptablesInstanceManager extends DefaultFirewallInstanceManager {
	private final Logger logger = LoggerFactory.getLogger(IptablesInstanceManager.class.getName());

	@Requires
	private FirewallController controller;

	@Requires
	private Iptables iptables;

	@Validate
	public void start() {
		try {
			FirewallInstance instance = createInstance("iptables", new Properties());
			logger.info("kraken iptables: created default [iptables] instance");

			// group cannot receive event because here is service
			// validation/registration phase. You need to enforce to
			// register instance to 'all' group.
			FirewallGroup all = controller.getGroup("all");
			all.join(instance.getName());
		} catch (Exception e) {
			logger.error("kraken iptables: start failed", e);
		}
	}

	@Override
	protected FirewallController getFirewallController() {
		return controller;
	}

	@Override
	public String getName() {
		return "iptables";
	}

	@Override
	protected FirewallInstance onCreate(String instanceName, Properties config) {
		return new IptablesInstance(this, iptables, instanceName, config);
	}

	@Override
	protected void onRemove(String instanceName) {
	}

	@Override
	public String toString() {
		return "iptables";
	}
}
