package org.krakenapps.siem.engine;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.isc.api.IscClient;
import org.krakenapps.isc.api.IscClientConfig;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.jpa.handler.Transactional;
import org.krakenapps.rule.Rule;
import org.krakenapps.rule.RuleGroup;
import org.krakenapps.rule.RuleStorage;
import org.krakenapps.siem.model.HttpRule;

@Component(name = "siem-isc-rule-storage")
@Provides
@JpaConfig(factory = "siem")
public class IscRuleStorage implements IscHttpRuleManager, RuleStorage {
	@Requires
	private ThreadLocalEntityManagerService entityManagerService;

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

	@Transactional
	@Override
	public Date getLastUpdateDate() {
		EntityManager em = entityManagerService.getEntityManager();
		try {
			HttpRule rule = (HttpRule) em.createQuery("FROM HttpRule h ORDER BY h.updateDateTime").setMaxResults(1)
					.getSingleResult();
			return rule.getUpdateDateTime();
		} catch (NoResultException e) {
			return new Date(0);
		}
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public void update() throws Exception {
		EntityManager em = entityManagerService.getEntityManager();
		if (config.getApiKey() == null)
			throw new Exception("set api key first");

		Object[] objs = (Object[]) client.call("rule.recent", getLastUpdateDate());
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		for (Object obj : objs) {
			Map<String, Object> m = (Map<String, Object>) obj;
			HttpRule rule = new HttpRule();
			rule.setSid((Integer) m.get("id"));
			rule.setName((String) m.get("name"));
			rule.setRule((String) m.get("rule"));
			rule.setCreateDateTime(format.parse((String) m.get("created_at")));
			rule.setUpdateDateTime(format.parse((String) m.get("updated_at")));

			if (em.find(HttpRule.class, rule.getSid()) == null)
				em.persist(rule);
			else
				em.merge(rule);
		}

		httpRuleGroup.rules = em.createQuery("FROM HttpRule h").getResultList();
	}

}
