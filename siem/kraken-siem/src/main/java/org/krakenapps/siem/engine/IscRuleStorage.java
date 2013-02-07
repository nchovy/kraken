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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigIterator;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.isc.api.IscClient;
import org.krakenapps.isc.api.IscClientConfig;
import org.krakenapps.rule.Rule;
import org.krakenapps.rule.RuleGroup;
import org.krakenapps.rule.RuleStorage;
import org.krakenapps.siem.ConfigManager;
import org.krakenapps.siem.model.HttpRule;

@Component(name = "siem-isc-rule-storage")
@Provides
public class IscRuleStorage implements IscHttpRuleManager, RuleStorage {

	@Requires
	private ConfigManager configManager;

	@Requires
	private IscClient client;

	@Requires
	private IscClientConfig config;

	private HttpRuleGroup httpRuleGroup = new HttpRuleGroup(this);

	private static class HttpRuleGroup implements RuleGroup {
		private RuleStorage storage;
		private Collection<Rule> rules = new ArrayList<Rule>();

		private HttpRuleGroup(RuleStorage storage) {
			this.storage = storage;
		}

		@Override
		public RuleStorage getStorage() {
			return storage;
		}

		@Override
		public String getName() {
			return "http";
		}

		@Override
		public Collection<Rule> getRules() {
			return rules;
		}
	}

	@Override
	public String getName() {
		return "isc-rule-storage";
	}

	@Override
	public Collection<RuleGroup> getRuleGroups() {
		Collection<RuleGroup> groups = new ArrayList<RuleGroup>();
		groups.add(httpRuleGroup);
		return groups;
	}

	@Override
	public RuleGroup getRuleGroup(String name) {
		if (name.equals("http"))
			return httpRuleGroup;

		return null;
	}

	@Override
	public Date getLastUpdateDate() {
		Date latest = new Date(0);
		for (Rule r : httpRuleGroup.rules) {
			HttpRule hr = (HttpRule) r;
			if (hr.getUpdateDateTime().after(latest))
				latest = hr.getUpdateDateTime();
		}
		return latest;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void update() throws Exception {
		if (config.getApiKey() == null)
			throw new Exception("set api key first");

		ConfigDatabase db = configManager.getDatabase();
		ConfigCollection col = db.ensureCollection("http-rule");

		Object[] objs = (Object[]) client.call("rule.recent", getLastUpdateDate());
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		for (Object obj : objs) {
			Map<String, Object> m = (Map<String, Object>) obj;
			HttpRule rule = new HttpRule();
			rule.setName((String) m.get("name"));
			rule.setRule((String) m.get("rule"));
			rule.setCreateDateTime(format.parse((String) m.get("created_at")));
			rule.setUpdateDateTime(format.parse((String) m.get("updated_at")));

			Config c = col.findOne(Predicates.field("name", rule.getName()));
			if (c != null) {
				c.setDocument(PrimitiveConverter.serialize(rule));
				col.update(c);
			} else {
				col.add(PrimitiveConverter.serialize(rule));
			}
		}

		// load all rules
		List<Rule> rules = new LinkedList<Rule>();
		ConfigIterator it = col.findAll();
		while (it.hasNext()) {
			Config c = it.next();
			HttpRule r = PrimitiveConverter.parse(HttpRule.class, c.getDocument());
			rules.add(r);
		}

		httpRuleGroup.rules = rules;
	}
}
