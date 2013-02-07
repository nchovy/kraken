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
package org.krakenapps.firewall.api.msgbus;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.firewall.api.FirewallController;
import org.krakenapps.firewall.api.FirewallGroup;
import org.krakenapps.firewall.api.FirewallInstance;
import org.krakenapps.firewall.api.FirewallInstanceManager;
import org.krakenapps.firewall.api.FirewallRule;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "firewall-plugin")
@MsgbusPlugin
public class FirewallPlugin {
	@Requires
	private FirewallController controller;

	@MsgbusMethod
	public void getGroups(Request req, Response resp) {
		List<Object> l = new ArrayList<Object>();
		for (FirewallGroup group : controller.getGroups()) {
			l.add(marshal(group));
		}

		resp.put("groups", l);
	}

	@MsgbusMethod
	public void createGroup(Request req, Response resp) {
		String groupName = req.getString("name");
		controller.createGroup(groupName);
	}

	@MsgbusMethod
	public void removeGroup(Request req, Response resp) {
		String groupName = req.getString("name");
		controller.removeGroup(groupName);
	}

	@MsgbusMethod
	public void joinGroup(Request req, Response resp) {
		String groupName = req.getString("group_name");
		String instanceName = req.getString("instance_name");

		FirewallGroup group = controller.getGroup(groupName);
		if (group == null)
			throw new IllegalStateException("group not found");

		group.join(instanceName);
	}

	@MsgbusMethod
	public void leaveGroup(Request req, Response resp) {
		String groupName = req.getString("group_name");
		String instanceName = req.getString("instance_name");

		FirewallGroup group = controller.getGroup(groupName);
		if (group == null)
			throw new IllegalStateException("group not found");

		group.leave(instanceName);
	}

	@MsgbusMethod
	public void getInstanceManagers(Request req, Response resp) {
		List<Object> l = new ArrayList<Object>();

		for (FirewallInstanceManager manager : controller.getInstanceManagers()) {
			l.add(marshal(manager));
		}

		resp.put("managers", l);
	}

	@MsgbusMethod
	public void getInstances(Request req, Response resp) {
		String managerName = req.getString("manager_name");

		List<Object> l = new ArrayList<Object>();

		if (managerName != null) {
			FirewallInstanceManager manager = controller.getInstanceManager(managerName);
			if (manager == null)
				throw new IllegalStateException("firewall instance manager not found");

			for (FirewallInstance instance : manager.getInstances()) {
				l.add(marshal(instance));
			}
		} else {
			for (FirewallInstanceManager manager : controller.getInstanceManagers()) {
				for (FirewallInstance instance : manager.getInstances()) {
					l.add(marshal(instance));
				}
			}
		}

		resp.put("instances", l);
	}

	@MsgbusMethod
	public void getGroupRules(Request req, Response resp) {
		String groupName = req.getString("group_name");

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		List<Object> l = new LinkedList<Object>();
		FirewallGroup group = controller.getGroup(groupName);
		for (FirewallRule rule : group.getRules()) {
			l.add(marshal(dateFormat, rule));
		}

		resp.put("rules", l);
	}

	@MsgbusMethod
	public void getInstanceRules(Request req, Response resp) {
		String name = req.getString("instance_name");

		List<Object> l = new LinkedList<Object>();
		FirewallInstance instance = controller.getInstance(name);
		for (InetAddress source : instance.getBlockedSources()) {
			l.add(source.getHostAddress());
		}

		resp.put("rules", l);
	}

	@MsgbusMethod
	public void createInstance(Request req, Response resp) {
		String managerName = req.getString("manager_name");
		String instanceName = req.getString("instance_name");

		Properties config = new Properties();
		FirewallInstanceManager manager = controller.getInstanceManager(managerName);
		if (manager == null)
			throw new IllegalStateException("firewall instance manager not found");

		manager.createInstance(instanceName, config);
	}

	@MsgbusMethod
	public void removeInstance(Request req, Response resp) {
		String managerName = req.getString("manager_name");
		String instanceName = req.getString("instance_name");

		FirewallInstanceManager manager = controller.getInstanceManager(managerName);
		if (manager == null)
			throw new IllegalStateException("firewall instance manager not found");

		manager.removeInstance(instanceName);
	}

	@MsgbusMethod
	public void block(Request req, Response resp) throws UnknownHostException {
		String groupName = req.getString("group_name");
		String host = req.getString("host");
		int minutes = req.getInteger("minutes");

		FirewallGroup group = controller.getGroup(groupName);
		if (group == null)
			throw new IllegalStateException("group not found");

		group.blockSourceIp(InetAddress.getByName(host), minutes);
	}

	@MsgbusMethod
	public void unblock(Request req, Response resp) throws UnknownHostException {
		String groupName = req.getString("group_name");
		String host = req.getString("host");

		FirewallGroup group = controller.getGroup(groupName);
		if (group == null)
			throw new IllegalStateException("group not found");

		group.unblockSourceIp(InetAddress.getByName(host));
	}

	private Map<String, Object> marshal(FirewallGroup group) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("name", group.getName());
		m.put("members", group.getMembers());
		m.put("rule_count", group.getRules().size());
		return m;
	}

	private Map<String, Object> marshal(FirewallInstanceManager manager) {
		Map<String, Object> m = new HashMap<String, Object>();
		List<String> instanceNames = new ArrayList<String>();

		for (FirewallInstance instance : manager.getInstances()) {
			instanceNames.add(instance.getName());
		}

		m.put("name", manager.getName());
		m.put("instances", instanceNames);
		return m;
	}

	private Map<String, Object> marshal(FirewallInstance instance) {
		Map<String, Object> config = new HashMap<String, Object>();
		for (Object key : instance.getConfig().keySet()) {
			config.put(key.toString(), instance.getConfig().get(key));
		}

		Map<String, Object> m = new HashMap<String, Object>();
		m.put("name", instance.getName());
		m.put("manager", instance.getInstanceManager().getName());
		m.put("config", config);
		return m;
	}

	private Map<String, Object> marshal(SimpleDateFormat dateFormat, FirewallRule rule) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("ip", rule.getSourceIp().getHostAddress());
		m.put("expire", dateFormat.format(rule.getExpire()));
		return m;
	}
}
