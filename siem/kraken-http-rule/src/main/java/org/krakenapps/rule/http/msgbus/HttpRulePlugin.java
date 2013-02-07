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
package org.krakenapps.rule.http.msgbus;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;
import org.krakenapps.rule.http.HttpRequestContext;
import org.krakenapps.rule.http.HttpRequestRule;
import org.krakenapps.rule.http.HttpResponseRule;
import org.krakenapps.rule.http.HttpRuleEngine;

@MsgbusPlugin
@Component(name = "http-rule-plugin")
public class HttpRulePlugin {
	@Requires
	private HttpRuleEngine engine;

	@MsgbusMethod
	public void getRequestRules(Request req, Response resp) {
		List<Object> rules = new LinkedList<Object>();
		for (HttpRequestRule r : engine.getRequestRules()) {
			rules.add(marshal(r));
		}

		resp.put("rules", rules);
	}

	@MsgbusMethod
	public void getResponseRules(Request req, Response resp) {
		List<Object> rules = new LinkedList<Object>();
		for (HttpResponseRule r : engine.getResponseRules()) {
			rules.add(marshal(r));
		}

		resp.put("rules", rules);
	}

	@SuppressWarnings("unchecked")
	@MsgbusMethod
	public void inspectRequest(Request req, Response resp) {
		String method = req.getString("method");
		String path = req.getString("path");
		Map<String, String> params = (Map<String, String>) req.get("params");

		HttpRequestContext c = new HttpRequestContext(method, path, params);
		HttpRequestRule r = engine.match(c);
		if (r != null)
			resp.put("rule", marshal(r));
		else
			resp.put("rule", null);
	}

	@MsgbusMethod
	public void reload(Request req, Response resp) {
		engine.reload();
	}

	private Map<String, Object> marshal(HttpResponseRule r) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("id", r.getId());
		m.put("msg", r.getMessage());
		m.put("type", r.getType());
		m.put("references", r.getReferences());
		m.put("cve_names", r.getCveNames());
		return m;
	}

	private Map<String, Object> marshal(HttpRequestRule r) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("id", r.getId());
		m.put("msg", r.getMessage());
		m.put("path", r.getPath());
		m.put("type", r.getType());
		m.put("references", r.getReferences());
		m.put("cve_names", r.getCveNames());
		return m;
	}
}
