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
package org.krakenapps.siem.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.krakenapps.event.api.Event;
import org.krakenapps.firewall.api.FirewallController;
import org.krakenapps.firewall.api.FirewallGroup;
import org.krakenapps.siem.response.ResponseAction;
import org.krakenapps.siem.response.ResponseActionManager;
import org.krakenapps.siem.response.ResponseType;

public class FirewallResponseAction implements ResponseAction {
	private FirewallController controller;
	private ResponseActionManager manager;
	private String namespace;
	private String name;
	private String description;
	private Properties config;
	private String groupName;
	private int minutes;

	public FirewallResponseAction(ResponseActionManager manager, FirewallController controller, String namespace,
			String name, String description, Properties config) {
		this.controller = controller;
		this.manager = manager;
		this.namespace = namespace;
		this.name = name;
		this.description = description;
		this.config = config;
		this.groupName = config.getProperty("group_name");
		this.minutes = Integer.valueOf(config.getProperty("minutes"));
	}

	@Override
	public ResponseActionManager getManager() {
		return manager;
	}

	@Override
	public String getNamespace() {
		return namespace;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public Properties getConfig() {
		return config;
	}

	@Override
	public ResponseType getType() {
		return ResponseType.Block;
	}

	public String getTargetGroup() {
		return groupName;
	}

	@Override
	public void handle(Event event) {
		FirewallGroup group = controller.getGroup(groupName);
		if (group == null)
			return;

		group.blockSourceIp(event.getSourceIp(), minutes);
	}

	@Override
	public String toString() {
		return "firewall response, group [" + groupName + "], block interval [" + minutes + "] min";
	}

	@Override
	public Map<String, Object> marshal() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("manager", manager.getName());
		m.put("namespace", namespace);
		m.put("name", name);
		m.put("description", description);
		m.put("group_name", groupName);
		m.put("minutes", minutes);
		return m;
	}

}
