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
import java.util.Collection;
import java.util.Properties;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.firewall.api.FirewallController;
import org.krakenapps.firewall.api.FirewallGroup;
import org.krakenapps.firewall.api.FirewallInstance;
import org.krakenapps.firewall.api.FirewallInstanceManager;
import org.krakenapps.firewall.api.FirewallRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FirewallScript implements Script {
	private final Logger logger = LoggerFactory.getLogger(FirewallScript.class.getName());

	private FirewallController controller;
	private ScriptContext context;

	public FirewallScript(FirewallController controller) {
		this.controller = controller;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void groups(String[] args) {
		String filter = null;
		if (args.length > 0)
			filter = args[0];

		Collection<FirewallGroup> groups = controller.getGroups();

		context.println("Firewall Groups");
		context.println("--------------------");
		for (FirewallGroup group : groups) {
			if (filter != null && !filter.equals(group.getName()))
				continue;

			context.println(group.getName() + " (rule count: " + group.getRules().size() + ")");
			for (String instanceName : group.getMembers()) {
				FirewallInstance instance = controller.getInstance(instanceName);
				if (instance == null)
					continue;

				context.println("\t" + instanceName + ": " + instance);
			}
		}
	}

	@ScriptUsage(description = "create group", arguments = { @ScriptArgument(name = "group name", type = "string", description = "group name") })
	public void createGroup(String[] args) {
		try {
			String groupName = args[0];
			controller.createGroup(groupName);
			context.println(groupName + " created");
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("kraken firewall api: cannot create group", e);
		}
	}

	@ScriptUsage(description = "remove group", arguments = { @ScriptArgument(name = "group name", type = "string", description = "group name") })
	public void removeGroup(String[] args) {
		try {
			String groupName = args[0];
			controller.removeGroup(groupName);
			context.println(groupName + " removed");
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("kraken firewall api: cannot remove group", e);
		}
	}

	@ScriptUsage(description = "join the group", arguments = {
			@ScriptArgument(name = "group name", type = "string", description = "group name"),
			@ScriptArgument(name = "instance name", type = "string", description = "instance name") })
	public void join(String[] args) {
		String groupName = args[0];
		String instanceName = args[1];

		try {
			if (groupName.equals("all"))
				throw new IllegalArgumentException("'all' group should not be changed");

			FirewallGroup group = controller.getGroup(groupName);
			group.join(instanceName);
			context.println(instanceName + " joined");
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("kraken firewall api: cannot join group " + groupName + ", instance " + instanceName, e);
		}
	}

	@ScriptUsage(description = "join the group", arguments = {
			@ScriptArgument(name = "group name", type = "string", description = "group name"),
			@ScriptArgument(name = "instance name", type = "string", description = "instance name") })
	public void leave(String[] args) {
		String groupName = args[0];
		String instanceName = args[1];

		try {
			if (groupName.equals("all"))
				throw new IllegalArgumentException("'all' group should not be changed");

			FirewallGroup group = controller.getGroup(groupName);
			group.leave(instanceName);
			context.println(instanceName + " left");
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("kraken firewall api: cannot leave group " + groupName + ", instance " + instanceName, e);
		}
	}

	public void managers(String[] args) {
		context.println("Firewall Instance Managers");
		context.println("----------------------------");
		for (FirewallInstanceManager manager : controller.getInstanceManagers()) {
			context.println(manager.toString());
		}
	}

	public void instances(String[] args) {
		context.println("Firewall Instances");
		context.println("----------------------------");
		for (FirewallInstance instance : controller.getInstances()) {
			context.println(instance.toString());
		}
	}

	@ScriptUsage(description = "create instance", arguments = {
			@ScriptArgument(name = "manager name", type = "string", description = "manager name"),
			@ScriptArgument(name = "instance name", type = "string", description = "instance name") })
	public void createInstance(String[] args) {
		try {
			String managerName = args[0];
			String instanceName = args[1];

			FirewallInstanceManager manager = controller.getInstanceManager(managerName);
			Properties config = new Properties();

			// TODO: add key=value configuration support
			FirewallInstance instance = manager.createInstance(instanceName, config);
			context.println(instance.getName() + " created");
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("kraken firewall api: cannot create instance", e);
		}
	}

	@ScriptUsage(description = "remove instance", arguments = { @ScriptArgument(name = "instance name", type = "string", description = "instance name") })
	public void removeInstance(String[] args) {
		try {
			String instanceName = args[0];

			FirewallInstance instance = controller.getInstance(instanceName);
			if (instance == null) {
				context.println("instance not found");
				return;
			}

			instance.getInstanceManager().removeInstance(instanceName);
			context.println(instanceName + " removed");
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("kraken firewall api: cannot remove instance", e);
		}
	}

	@ScriptUsage(description = "block specific source ip", arguments = {
			@ScriptArgument(name = "group name", type = "string", description = "group name"),
			@ScriptArgument(name = "source ip", type = "string", description = "source ip"),
			@ScriptArgument(name = "block interval", type = "int", description = "block interval in minutes") })
	public void block(String[] args) {
		try {
			String groupName = args[0];
			InetAddress sourceIp = InetAddress.getByName(args[1]);
			int interval = Integer.valueOf(args[2]);

			FirewallGroup group = controller.getGroup(groupName);
			group.blockSourceIp(sourceIp, interval);
			context.println(args[1] + " blocked");
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("kraken firewall api: cannot block ip " + args[1], e);
		}
	}

	@ScriptUsage(description = "unblock specific source ip", arguments = {
			@ScriptArgument(name = "group name", type = "string", description = "group name"),
			@ScriptArgument(name = "source ip", type = "string", description = "source ip") })
	public void unblock(String[] args) {
		try {
			String groupName = args[0];
			InetAddress sourceIp = InetAddress.getByName(args[1]);

			FirewallGroup group = controller.getGroup(groupName);
			group.unblockSourceIp(sourceIp);
			context.println(args[1] + " unblocked");
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("kraken firewall api: cannot unblock ip " + args[1], e);
		}
	}

	@ScriptUsage(description = "print block rules in group", arguments = { @ScriptArgument(name = "group name", type = "string", description = "group name") })
	public void rules(String[] args) {
		String groupName = args[0];

		FirewallGroup group = controller.getGroup(groupName);
		if (group == null) {
			context.println("group not found");
			return;
		}

		context.println("Rules");
		context.println("----------------");
		for (FirewallRule rule : group.getRules()) {
			context.println(rule.toString());
		}
	}
}
