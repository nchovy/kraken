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
package org.krakenapps.ipmanager.impl;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.ipmanager.IpDetection;
import org.krakenapps.ipmanager.IpEventListener;
import org.krakenapps.ipmanager.IpManager;
import org.krakenapps.ipmanager.IpQueryCondition;
import org.krakenapps.ipmanager.LogQueryCondition;
import org.krakenapps.ipmanager.model.Agent;
import org.krakenapps.ipmanager.model.AllowedMac;
import org.krakenapps.ipmanager.model.DeniedMac;
import org.krakenapps.ipmanager.model.DetectedMac;
import org.krakenapps.ipmanager.model.HostEntry;
import org.krakenapps.ipmanager.model.HostNic;
import org.krakenapps.ipmanager.model.IpEntry;
import org.krakenapps.ipmanager.model.IpEventLog;
import org.krakenapps.ipmanager.model.IpEventLog.Type;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.jpa.handler.Transactional;
import org.krakenapps.pcap.decoder.ethernet.MacAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "ipm-manager")
@Provides
@JpaConfig(factory = "ipm")
public class IpManagerService implements IpManager, Runnable {
	private final Logger logger = LoggerFactory.getLogger(IpManagerService.class.getName());

	@Requires
	private ThreadLocalEntityManagerService entityManagerService;

	private BlockingQueue<IpDetection> queue;

	private CopyOnWriteArraySet<IpEventListener> callbacks;

	private Thread t;
	private boolean doStop;

	@Validate
	public void start() {
		callbacks = new CopyOnWriteArraySet<IpEventListener>();

		doStop = false;
		queue = new LinkedBlockingQueue<IpDetection>();
		t = new Thread(this, "IP Sync");
		t.start();
	}

	@Invalidate
	public void stop() {
		callbacks.clear();

		doStop = true;
		t.interrupt();
	}

	@Transactional
	@SuppressWarnings("unchecked")
	@Override
	public List<Agent> getAgents(int orgId) {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("FROM Agent a WHERE a.orgId = ?").setParameter(1, orgId).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public List<HostEntry> getHosts(int orgId) {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("SELECT h FROM HostEntry h INNER JOIN h.agent a WHERE a.orgId = ?")
				.setParameter(1, orgId).getResultList();
	}

	@Transactional
	@Override
	public List<IpEntry> getIpEntries(IpQueryCondition condition) {
		EntityManager em = entityManagerService.getEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<IpEntry> cq = cb.createQuery(IpEntry.class);
		Root<IpEntry> root = cq.from(IpEntry.class);
		cq.where(condition.getPredicate(cb, root));

		return em.createQuery(cq).getResultList();
	}

	@Transactional
	@Override
	public List<AllowedMac> getAllowMacAddresses(int orgId, int ipId) {
		EntityManager em = entityManagerService.getEntityManager();
		IpEntry entry = (IpEntry) em.createQuery("FROM IpEntry i WHERE i.id = ?").setParameter(1, ipId)
				.getSingleResult();

		if (entry.getAgent().getId() != orgId)
			return null;
		return entry.getAllowedMacs();
	}

	@Transactional
	@Override
	public int allowMacAddress(int orgId, int ipId, String mac, Date from, Date to) {
		mac = mac.toUpperCase();

		EntityManager em = entityManagerService.getEntityManager();
		try {
			IpEntry ipEntry = (IpEntry) em
					.createQuery("SELECT e FROM IpEntry e INNER JOIN e.agent a WHERE e.id = ? AND a.orgId = ?")
					.setParameter(1, ipId).setParameter(2, orgId).getSingleResult();

			try {
				DeniedMac d = (DeniedMac) em.createQuery("FROM DeniedMac d WHERE d.mac = ?").setParameter(1, mac)
						.getSingleResult();
				throw new IllegalStateException("conflict with denied mac: " + d.getId());
			} catch (NoResultException e) {
			}

			boolean isNew = false;

			// check if already registered
			AllowedMac allowedMac = null;

			for (AllowedMac a : ipEntry.getAllowedMacs()) {
				if (a.getMac().equals(mac))
					allowedMac = a;
			}

			if (allowedMac == null) {
				allowedMac = new AllowedMac();
				allowedMac.setIp(ipEntry);
				allowedMac.setMac(mac);
				allowedMac.setCreateDateTime(new Date());
				isNew = true;
			}

			allowedMac.setBeginDateTime(from);
			allowedMac.setEndDateTime(to);

			if (isNew)
				em.persist(allowedMac);
			else
				em.merge(allowedMac);

			return allowedMac.getId();
		} catch (NoResultException e) {
			throw new IllegalStateException("ip entry not found: " + ipId);
		}
	}

	@Transactional
	@Override
	public void disallowMacAddress(int orgId, int macId) {
		EntityManager em = entityManagerService.getEntityManager();

		AllowedMac allowedMac = null;
		try {
			allowedMac = (AllowedMac) em.createQuery("FROM AllowedMac a WHERE a.id = ?").setParameter(1, macId)
					.getSingleResult();

			if (allowedMac.getIp().getAgent().getOrgId() != orgId)
				throw new SecurityException("no permission to edit mac: " + macId);

			em.remove(allowedMac);
		} catch (NoResultException e) {
			throw new IllegalStateException("allowed mac not found: " + macId);
		}
	}

	@Transactional
	@Override
	public List<DeniedMac> getDenyMacAddresses(int orgId, int agentId) {
		EntityManager em = entityManagerService.getEntityManager();
		Agent agent = (Agent) em.createQuery("FROM Agent a WHERE a.id = ?").setParameter(1, agentId).getSingleResult();
		if (agent.getOrgId() != orgId)
			return null;
		return agent.getDeniedMac();
	}

	@Transactional
	@Override
	public int denyMacAddress(int orgId, int agentId, String mac, Date from, Date to) {
		mac = mac.toUpperCase();

		EntityManager em = entityManagerService.getEntityManager();
		try {
			Agent agent = (Agent) em.createQuery("FROM Agent a WHERE a.id = ? AND a.orgId = ?")
					.setParameter(1, agentId).setParameter(2, orgId).getSingleResult();

			boolean isNew = false;

			DeniedMac deniedMac = null;
			for (DeniedMac m : agent.getDeniedMac()) {
				if (m.getMac().equals(mac))
					deniedMac = m;
			}

			if (deniedMac == null) {
				deniedMac = new DeniedMac();
				deniedMac.setAgent(agent);
				deniedMac.setMac(mac);
				deniedMac.setCreateDateTime(new Date());
				isNew = true;
			}

			deniedMac.setBeginDateTime(from);
			deniedMac.setEndDateTime(to);

			if (isNew)
				em.persist(deniedMac);
			else
				em.merge(deniedMac);

			return deniedMac.getId();
		} catch (NoResultException e) {
			throw new IllegalStateException("agent not found: " + agentId);
		}
	}

	@Transactional
	@Override
	public void removeDenyMacAddress(int orgId, int macId) {
		EntityManager em = entityManagerService.getEntityManager();

		DeniedMac deniedMac = null;
		try {
			deniedMac = (DeniedMac) em.createQuery("FROM DeniedMac d WHERE d.id = ?").setParameter(1, macId)
					.getSingleResult();

			if (deniedMac.getAgent().getOrgId() != orgId)
				throw new SecurityException("no permission to edit mac: " + macId);

			em.remove(deniedMac);
		} catch (NoResultException e) {
			throw new IllegalStateException("denied mac not found: " + macId);
		}
	}

	@Override
	public void updateIpEntry(IpDetection detection) {
		queue.add(detection);
	}

	@Transactional
	@Override
	public List<IpEventLog> getLogs(LogQueryCondition condition) {
		int page = condition.getPage();
		if (page < 1)
			throw new IllegalArgumentException("page number should be natural number");

		EntityManager em = entityManagerService.getEntityManager();
		int pageSize = condition.getPageSize();
		int offset = (page - 1) * pageSize;

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<IpEventLog> cq = cb.createQuery(IpEventLog.class);
		Root<IpEventLog> root = cq.from(IpEventLog.class);
		cq.where(condition.getPredicate(cb, root));
		cq.orderBy(cb.desc(root.get("id")));
		List<IpEventLog> logs = em.createQuery(cq).setFirstResult(offset).setMaxResults(pageSize).getResultList();

		return logs;
	}

	@Override
	public void run() {
		logger.info("kraken ipmanager: starting ip sync thread");

		while (!doStop) {
			try {
				syncIpEntries();
				Thread.sleep(3000);
			} catch (InterruptedException e) {
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Transactional
	private void syncIpEntries() {
		EntityManager em = entityManagerService.getEntityManager();
		List<IpDetection> l = new LinkedList<IpDetection>();
		queue.drainTo(l);
		if (l.size() == 0)
			return;

		Map<String, Agent> agents = mapAgents((List<Agent>) em.createQuery("FROM Agent a").getResultList());

		List<String> ipFilter = ipFilter(l);
		Map<IpEntryKey, IpEntry> entries = mapIpEntries(em.createQuery("FROM IpEntry e WHERE e.ip IN (:filter)")
				.setParameter("filter", ipFilter).getResultList());

		List<String> macFilter = macFilter(l);
		Map<DetectedMacKey, DetectedMac> macs = mapDetectedMacs(em
				.createQuery("FROM DetectedMac d WHERE d.mac IN (:filter)").setParameter("filter", macFilter)
				.getResultList());

		for (IpDetection d : l) {
			sync(em, agents, entries, macs, d);
		}
	}

	/*
	 * Agent GUID to Agent map
	 */
	private Map<String, Agent> mapAgents(List<Agent> agents) {
		Map<String, Agent> m = new HashMap<String, Agent>();
		for (Agent a : agents)
			m.put(a.getGuid(), a);
		return m;
	}

	private Map<IpEntryKey, IpEntry> mapIpEntries(List<IpEntry> entries) {
		Map<IpEntryKey, IpEntry> m = new HashMap<IpEntryKey, IpEntry>();
		for (IpEntry e : entries)
			m.put(new IpEntryKey(e.getAgent().getId(), e.getIp()), e);
		return m;
	}

	private Map<DetectedMacKey, DetectedMac> mapDetectedMacs(List<DetectedMac> macs) {
		Map<DetectedMacKey, DetectedMac> m = new HashMap<DetectedMacKey, DetectedMac>();
		for (DetectedMac d : macs)
			m.put(new DetectedMacKey(d.getAgent().getId(), d.getMac()), d);
		return m;
	}

	private List<String> ipFilter(List<IpDetection> l) {
		List<String> s = new ArrayList<String>(l.size());
		for (IpDetection d : l)
			s.add(d.getIp().getHostAddress());
		return s;
	}

	private List<String> macFilter(List<IpDetection> l) {
		List<String> s = new ArrayList<String>(l.size());
		for (IpDetection d : l)
			s.add(d.getMac().toString());
		return s;
	}

	private void sync(EntityManager em, Map<String, Agent> agents, Map<IpEntryKey, IpEntry> entries,
			Map<DetectedMacKey, DetectedMac> macs, IpDetection d) {
		Agent agent = agents.get(d.getAgentGuid());
		if (agent == null) {
			logger.warn("kraken ipmanager: agent [{}] not found, discarded", d.getAgentGuid());
			return;
		}

		// netbios/dhcp host check
		HostEntry host = syncHostEntry(em, d, agent);

		if (d.getIp() != null && !d.getIp().getHostAddress().equals("0.0.0.0"))
			syncIpEntry(em, entries, macs, d, agent, host);
	}

	private void syncIpEntry(EntityManager em, Map<IpEntryKey, IpEntry> entries, Map<DetectedMacKey, DetectedMac> macs,
			IpDetection d, Agent agent, HostEntry host) {
		IpEntry entry = null;
		logger.debug("kraken ipmanager: trace ip detection [{}]", d);

		// new ip?
		IpEntryKey key = new IpEntryKey(agent.getId(), d.getIp().getHostAddress());
		if (!entries.containsKey(key)) {
			entry = new IpEntry();
			entry.setIp(d.getIp().getHostAddress());
			entry.setAgent(agent);
			entry.setFirstSeen(d.getDate());
			entry.setLastSeen(d.getDate());
			entry.setCurrentMac(d.getMac().toString());
			if (host != null)
				entry.getHostEntries().add(host);
			em.persist(entry);

			entries.put(key, entry);

			generateLog(em, agent, Type.NewIpDetected, d);
			notifyNewIpDetected(d, agent);

			logger.trace("kraken ipmanager: new ip [{}] added", d);
		} else {
			entry = entries.get(key);
			if (host != null)
				entry.getHostEntries().add(host);
			// ip conflict?
			if (!entry.getCurrentMac().equals(d.getMac().toString())) {
				MacAddress originalMac = new MacAddress(entry.getCurrentMac());
				entry.setCurrentMac(d.getMac().toString());

				generateLog(em, agent, Type.IpConflict, d, originalMac.toString(), null);
				notifyIpConflict(d, agent, originalMac);
			}

			entry.setLastSeen(d.getDate());
			em.merge(entry);
		}

		// update detected mac
		DetectedMacKey macKey = new DetectedMacKey(agent.getId(), d.getMac().toString());
		DetectedMac detectedMac = macs.get(macKey);
		if (detectedMac == null) {
			detectedMac = newDetectedMac(d, agent, entry);
			em.persist(detectedMac);
			macs.put(macKey, detectedMac);

			generateLog(em, agent, Type.NewMacDetected, d);
			notifyNewMacDetected(d, agent);
		} else {
			// is ip changed?
			if (!detectedMac.getIp().getIp().equals(d.getIp().getHostAddress())) {
				DetectedMac changed = newDetectedMac(d, agent, entry);
				em.persist(changed);
				macs.put(macKey, changed);

				generateLog(em, agent, Type.IpChanged, d, null, detectedMac.getIp().getIp());
				notifyIpChanged(d, agent, detectedMac.getIp().getIp());
			} else {
				// not changed, just update last seen
				detectedMac.setLastSeen(d.getDate());
				em.merge(detectedMac);
			}
		}
	}

	private DetectedMac newDetectedMac(IpDetection d, Agent agent, IpEntry entry) {
		DetectedMac detectedMac;
		detectedMac = new DetectedMac();
		detectedMac.setAgent(agent);
		detectedMac.setFirstSeen(d.getDate());
		detectedMac.setLastSeen(d.getDate());
		detectedMac.setIp(entry);
		detectedMac.setMac(d.getMac().toString());
		return detectedMac;
	}

	private HostEntry syncHostEntry(EntityManager em, IpDetection d, Agent agent) {
		if (d.getHostName() == null && d.getWorkGroup() == null)
			return null;

		HostEntry hostEntry = null;
		try {
			hostEntry = (HostEntry) em
					.createQuery("SELECT h FROM HostEntry h INNER JOIN h.hostMacs m WHERE h.agent = ? AND m.mac = ?")
					.setParameter(1, agent).setParameter(2, d.getMac().toString()).getSingleResult();

			setHostEntry(d, hostEntry);

			em.merge(hostEntry);

		} catch (NoResultException e) {
			hostEntry = new HostEntry();
			hostEntry.setAgent(agent);
			hostEntry.setFirstSeen(d.getDate());

			setHostEntry(d, hostEntry);

			em.persist(hostEntry);

			HostNic nic = new HostNic();
			nic.setHost(hostEntry);
			nic.setFirstSeen(d.getDate());
			nic.setLastSeen(d.getDate());
			nic.setMac(d.getMac().toString());

			em.persist(nic);
		}

		return hostEntry;
	}

	private void setHostEntry(IpDetection d, HostEntry hostEntry) {
		hostEntry.setLastSeen(d.getDate());

		if (d.getHostName() != null)
			hostEntry.setName(d.getHostName());
		if (d.getWorkGroup() != null)
			hostEntry.setWorkGroup(d.getWorkGroup());
		if (d.getCategory() != null)
			hostEntry.setCategory(d.getCategory());
		if (d.getVendor() != null)
			hostEntry.setVendor(d.getVendor());
	}

	private static class DetectedMacKey {
		private int agentId;
		private String mac;

		public DetectedMacKey(int agentId, String mac) {
			this.agentId = agentId;
			this.mac = mac;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + agentId;
			result = prime * result + ((mac == null) ? 0 : mac.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DetectedMacKey other = (DetectedMacKey) obj;
			if (agentId != other.agentId)
				return false;
			if (mac == null) {
				if (other.mac != null)
					return false;
			} else if (!mac.equals(other.mac))
				return false;
			return true;
		}
	}

	private static class IpEntryKey {
		private int agentId;
		private String ip;

		public IpEntryKey(int agentId, String ip) {
			this.agentId = agentId;
			this.ip = ip;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + agentId;
			result = prime * result + ((ip == null) ? 0 : ip.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			IpEntryKey other = (IpEntryKey) obj;
			if (agentId != other.agentId)
				return false;
			if (ip == null) {
				if (other.ip != null)
					return false;
			} else if (!ip.equals(other.ip))
				return false;
			return true;
		}
	}

	private void generateLog(EntityManager em, Agent agent, Type type, IpDetection d) {
		generateLog(em, agent, type, d, null, null);
	}

	private void generateLog(EntityManager em, Agent agent, Type type, IpDetection d, String oldMac, String oldIp) {
		IpEventLog log = new IpEventLog();
		log.setType(type.getCode());
		log.setAgent(agent);
		log.setDate(d.getDate());

		if (oldIp != null) {
			log.setIp1(oldIp);
			log.setIp2(d.getIp().getHostAddress());
		} else {
			log.setIp1(d.getIp().getHostAddress());
		}

		if (oldMac != null) {
			log.setMac1(oldMac);
			log.setMac2(d.getMac().toString());
		} else {
			log.setMac1(d.getMac().toString());
		}

		log.setOrgId(agent.getOrgId());
		em.persist(log);
	}

	private void notifyIpConflict(IpDetection d, Agent agent, MacAddress originalMac) {
		// fire callbacks
		for (IpEventListener callback : callbacks) {
			try {
				callback.onIpConflict(agent, d.getIp(), originalMac, d.getMac());
			} catch (Exception e) {
				logger.warn("kraken ipmanager: event callback should not throw any exception", e);
			}
		}
	}

	private void notifyNewIpDetected(IpDetection d, Agent agent) {
		// fire callbacks
		for (IpEventListener callback : callbacks) {
			try {
				callback.onNewIpDetected(agent, d.getIp(), d.getMac());
			} catch (Exception e) {
				logger.warn("kraken ipmanager: event callback should not throw any exception", e);
			}
		}
	}

	private void notifyNewMacDetected(IpDetection d, Agent agent) {
		// fire callbacks
		for (IpEventListener callback : callbacks) {
			try {
				callback.onNewMacDetected(agent, d.getIp(), d.getMac());
			} catch (Exception e) {
				logger.warn("kraken ipmanager: event callback should not throw any exception", e);
			}
		}
	}

	private void notifyIpChanged(IpDetection d, Agent agent, String oldIp) {
		// fire ip changed callbacks
		for (IpEventListener callback : callbacks) {
			try {
				callback.onIpChanged(agent, InetAddress.getByName(oldIp), d.getIp(), d.getMac());
			} catch (Exception e) {
				logger.warn("kraken ipmanager: event callback should not throw any exception", e);
			}
		}
	}

	@Override
	public void addListener(IpEventListener callback) {
		callbacks.add(callback);
	}

	@Override
	public void removeListener(IpEventListener callback) {
		callbacks.remove(callback);
	}
}
