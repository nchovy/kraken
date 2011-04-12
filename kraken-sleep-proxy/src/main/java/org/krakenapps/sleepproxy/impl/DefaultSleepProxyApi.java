package org.krakenapps.sleepproxy.impl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.jpa.handler.Transactional;
import org.krakenapps.pcap.decoder.ethernet.MacAddress;
import org.krakenapps.pcap.util.WakeOnLan;
import org.krakenapps.sleepproxy.PowerStat;
import org.krakenapps.sleepproxy.SleepProxyApi;
import org.krakenapps.sleepproxy.exception.AgentGroupNotFoundException;
import org.krakenapps.sleepproxy.exception.AgentNotFoundException;
import org.krakenapps.sleepproxy.exception.SleepPolicyNotFoundException;
import org.krakenapps.sleepproxy.model.Agent;
import org.krakenapps.sleepproxy.model.AgentGroup;
import org.krakenapps.sleepproxy.model.NetworkAdapter;
import org.krakenapps.sleepproxy.model.PowerLog;
import org.krakenapps.sleepproxy.model.SleepLog;
import org.krakenapps.sleepproxy.model.SleepPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "sleep-proxy-api")
@Provides
@JpaConfig(factory = "sleep-proxy")
public class DefaultSleepProxyApi implements SleepProxyApi {
	private final Logger logger = LoggerFactory.getLogger(DefaultSleepProxyApi.class.getName());

	@Requires
	private ThreadLocalEntityManagerService entityManagerService;

	@Override
	public Collection<SleepLog> getLogs(Date from, Date to) {
		return getLogs(getRootAgentGroup().getId(), from, to);
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Collection<SleepLog> getLogs(Integer groupId, Date from, Date to) {
		EntityManager em = entityManagerService.getEntityManager();
		if (groupId == null)
			groupId = getRootAgentGroup().getId();

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		logger.trace("sleep proxy: fetch logs where from=%s, to=%s, group=%d", new Object[] { dateFormat.format(from),
				dateFormat.format(to), groupId });

		ArrayList<Integer> groupFilter = new ArrayList<Integer>();
		for (AgentGroup group : getAgentGroups(groupId, true))
			groupFilter.add(group.getId());

		List<SleepLog> logs = em.createQuery(
				"FROM SleepLog l "
						+ "WHERE l.created >= :from AND l.created <= :to AND l.agent.agentGroup.id IN (:filter)")
				.setParameter("from", from).setParameter("to", to).setParameter("filter", groupFilter).getResultList();

		return logs;
	}

	@Transactional
	@Override
	public AgentGroup getRootAgentGroup() {
		EntityManager em = entityManagerService.getEntityManager();

		try {
			return (AgentGroup) em.createQuery("FROM AgentGroup g WHERE g.parent IS NULL").getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Transactional
	@Override
	public AgentGroup getAgentGroup(int id) {
		EntityManager em = entityManagerService.getEntityManager();
		AgentGroup group = em.find(AgentGroup.class, id);
		if (group == null)
			throw new AgentGroupNotFoundException(id);

		return group;
	}

	@Transactional
	@Override
	public Agent getAgent(String guid) {
		EntityManager em = entityManagerService.getEntityManager();
		try {
			return (Agent) em.createQuery("FROM Agent a WHERE a.guid = ?").setParameter(1, guid).getSingleResult();
		} catch (NoResultException e) {
			throw new AgentNotFoundException(guid);
		}
	}

	@Transactional
	@Override
	public void removeAgent(int id) {
		EntityManager em = entityManagerService.getEntityManager();
		Agent agent = em.find(Agent.class, id);
		if (agent != null)
			em.remove(agent);
	}

	@Transactional
	@Override
	public Agent getAgent(int id) {
		EntityManager em = entityManagerService.getEntityManager();
		Agent agent = em.find(Agent.class, id);
		if (agent == null)
			return null;

		// force loading
		agent.getAdapters().size();
		return agent;
	}

	@Transactional
	@Override
	public Collection<AgentGroup> getAgentGroups(Integer id, boolean descendants) {
		EntityManager em = entityManagerService.getEntityManager();
		AgentGroup root = findGroup(id, em);

		List<AgentGroup> groups = new ArrayList<AgentGroup>();
		traverse(root, groups);
		return groups;
	}

	private void traverse(AgentGroup group, List<AgentGroup> groups) {
		groups.add(group);

		for (AgentGroup child : group.getChildren())
			traverse(child, groups);
	}

	private AgentGroup findGroup(Integer id, EntityManager em) {
		AgentGroup root = null;

		if (id != null) {
			root = em.find(AgentGroup.class, id);
		}

		if (root == null) {
			try {
				root = (AgentGroup) em.createQuery("FROM AgentGroup g WHERE g.parent IS NULL").getSingleResult();
			} catch (NoResultException e) {
				throw new AgentGroupNotFoundException(0);
			}
		}

		return root;
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Collection<Agent> getAgents(Integer groupId, boolean descendants) {
		EntityManager em = entityManagerService.getEntityManager();

		ArrayList<Integer> groupFilter = new ArrayList<Integer>();

		for (AgentGroup group : getAgentGroups(groupId, descendants))
			groupFilter.add(group.getId());

		List<Agent> agents = em.createQuery("FROM Agent a WHERE a.agentGroup.id IN (:filter)").setParameter("filter",
				groupFilter).getResultList();

		return agents;
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Collection<SleepLog> getLogs(String guid, Date from, Date to) {
		EntityManager em = entityManagerService.getEntityManager();
		Agent agent = getAgent(guid);
		if (agent == null)
			throw new AgentNotFoundException(guid);

		return em.createQuery("FROM SleepLog l WHERE l.agent.id = ? AND l.created >= ? AND l.created <= ?")
				.setParameter(1, agent.getId()).setParameter(2, from).setParameter(2, to).getResultList();
	}

	@Transactional
	@Override
	public AgentGroup createAgentGroup(Integer parentId, String name, String description, int policyId) {
		EntityManager em = entityManagerService.getEntityManager();

		AgentGroup parent = getAgentGroup(parentId);
		if (parent == null)
			throw new IllegalStateException("parent group not found: " + parentId);

		SleepPolicy policy = getSleepPolicy(policyId);
		if (policy == null)
			throw new IllegalStateException("policy not found: " + policyId);

		AgentGroup group = new AgentGroup();
		group.setParent(parent);
		group.setName(name);
		group.setDescription(description);
		group.setPolicy(policy);
		group.setCreated(new Date());
		group.setUpdated(new Date());

		em.persist(group);

		return group;
	}

	@Transactional
	@Override
	public void updateAgentGroup(int id, String name, String description, int policyId) {
		EntityManager em = entityManagerService.getEntityManager();

		SleepPolicy policy = getSleepPolicy(policyId);
		if (policy == null)
			throw new IllegalStateException("policy not found: " + policyId);

		AgentGroup group = em.find(AgentGroup.class, id);
		if (group == null)
			throw new IllegalStateException("group not found: " + id);

		group.setName(name);
		group.setDescription(description);
		group.setPolicy(policy);
		group.setUpdated(new Date());

		em.merge(group);
	}

	@Transactional
	@Override
	public void removeAgentGroup(int id) {
		EntityManager em = entityManagerService.getEntityManager();
		AgentGroup group = getAgentGroup(id);
		em.remove(group);
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Collection<SleepPolicy> getSleepPolicies() {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("FROM SleepPolicy").getResultList();
	}

	@Transactional
	@Override
	public SleepPolicy getSleepPolicy(int id) {
		EntityManager em = entityManagerService.getEntityManager();
		return em.find(SleepPolicy.class, id);
	}

	@Transactional
	@Override
	public SleepPolicy createSleepPolicy(String name, String description, int awayCriteria, boolean forceHibernate) {
		EntityManager em = entityManagerService.getEntityManager();
		SleepPolicy policy = new SleepPolicy();

		policy.setName(name);
		policy.setDescription(description);
		policy.setAwayCriteria(awayCriteria);
		policy.setForceHibernate(forceHibernate);
		policy.setCreated(new Date());
		policy.setUpdated(new Date());

		em.persist(policy);

		return policy;
	}

	@Transactional
	@Override
	public void updateSleepPolicy(int id, String name, String description, int awayCriteria, boolean forceHibernate) {
		EntityManager em = entityManagerService.getEntityManager();
		SleepPolicy policy = getSleepPolicy(id);
		if (policy == null)
			throw new SleepPolicyNotFoundException();

		policy.setName(name);
		policy.setDescription(description);
		policy.setAwayCriteria(awayCriteria);
		policy.setForceHibernate(forceHibernate);
		policy.setUpdated(new Date());

		em.merge(policy);
	}

	@Transactional
	@Override
	public void removeSleepPolicy(int id) {
		EntityManager em = entityManagerService.getEntityManager();
		SleepPolicy policy = getSleepPolicy(id);

		// TODO: check group policy mappings

		em.remove(policy);
	}

	@Override
	public void wakeAgent(int id) {
		Agent agent = getAgent(id);
		if (agent == null)
			throw new IllegalStateException("agent not found for " + id);

		for (NetworkAdapter adapter : agent.getAdapters()) {
			try {
				logger.info("sleep proxy: sending wol packet to {}", adapter.getMac());
				WakeOnLan.wake(new MacAddress(adapter.getMac()));
			} catch (IOException e) {
				logger.error("sleep proxy: cannot send WOL packet", e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Collection<PowerStat> getAgentPowerGraph(int id, Date from, Date to) {
		EntityManager em = entityManagerService.getEntityManager();

		// normalize to 5min boundary
		from = normalize(from);
		to = normalize(to);

		// prepare data map
		Collection<PowerLog> logs = em.createQuery(
				"FROM PowerLog l WHERE l.agent.id = ? AND l.date >= ? AND l.date <= ?").setParameter(1, id)
				.setParameter(2, from).setParameter(3, to).getResultList();

		return buildGraph(from, to, logs);
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Collection<PowerStat> getGroupPowerGraph(Integer groupId, Date from, Date to, boolean descendants) {
		EntityManager em = entityManagerService.getEntityManager();

		// normalize to 5min boundary
		from = normalize(from);
		to = normalize(to);

		List<Integer> filter = getAgentFilter(groupId, descendants);
		Collection<PowerLog> logs = new ArrayList<PowerLog>();
		if (filter.size() > 0) {
			logs = em.createQuery(
					"SELECT new PowerLog(l.date, l.used, l.canSaved, l.saved) "
							+ "FROM PowerLog l WHERE l.date >= :from AND l.date <= :to AND l.agent.id IN (:agents) "
							+ "GROUP BY date").setParameter("from", from).setParameter("to", to).setParameter("agents",
					filter).getResultList();
		}

		return buildGraph(from, to, logs);
	}

	private List<Integer> getAgentFilter(Integer groupId, boolean descendants) {
		Collection<Agent> agents = getAgents(groupId, descendants);
		List<Integer> filter = new ArrayList<Integer>();
		for (Agent agent : agents)
			filter.add(agent.getId());

		return filter;
	}

	private Collection<PowerStat> buildGraph(Date from, Date to, Collection<PowerLog> logs) {
		Map<Date, PowerLog> m = new HashMap<Date, PowerLog>();
		for (PowerLog log : logs)
			m.put(log.getDate(), log);

		List<PowerStat> graph = new ArrayList<PowerStat>();

		// add data or zero, build continuous graph
		Date d = from;
		while (!d.after(to)) {
			if (m.containsKey(d)) {
				PowerLog log = m.get(d);
				graph.add(new PowerStat(log));
			} else {
				graph.add(new PowerStat(d));
			}

			d = add5min(d);
		}

		return graph;
	}

	private Date add5min(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.MINUTE, 5);
		d = c.getTime();
		return d;
	}

	/**
	 * adjust to 5min boundary
	 */
	private Date normalize(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) / 5 * 5);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

}
