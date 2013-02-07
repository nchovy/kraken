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
package org.krakenapps.rule.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;
import org.krakenapps.rule.Rule;
import org.krakenapps.rule.RuleEngine;
import org.krakenapps.rule.RuleEngineRegistry;

@MsgbusPlugin
@Component(name = "rule-plugin")
public class RulePlugin {
	@Requires
	private RuleEngineRegistry registry;

	@MsgbusMethod
	public void engines(Request req, Response resp) {
		Collection<RuleEngine> engines = registry.getEngines();
		resp.put("engines", marshal(engines));
	}

	@MsgbusMethod
	public void rules(Request req, Response resp) {
		if (req.has("engine")) {
			RuleEngine engine = registry.getEngine(req.getString("engine"));
			Collection<Rule> rules = engine.getRules();
			resp.put("rules", marshal(rules));
		} else {
			for (RuleEngine engine : registry.getEngines()) {
				Collection<Rule> rules = engine.getRules();
				resp.put(engine.getName(), marshal(rules));
			}
		}
	}

	private List<Object> marshal(Collection<?> objs) {
		List<Object> l = new ArrayList<Object>();

		for (Object obj : objs) {
			if (obj instanceof RuleEngine)
				l.add(marshal((RuleEngine) obj));
			else if (obj instanceof Rule)
				l.add(marshal((Rule) obj));
		}

		return l;
	}

	private Map<String, Object> marshal(RuleEngine engine) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("name", engine.getName());
		m.put("description", engine.getDescription());
		m.put("rule_counts", engine.getRules().size());
		return m;
	}

	private Map<String, Object> marshal(Rule rule) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("type", rule.getType());
		m.put("id", rule.getId());
		m.put("message", rule.getMessage());
		m.put("cve_names", rule.getCveNames());
		m.put("references", rule.getReferences());
		return m;
	}
}
