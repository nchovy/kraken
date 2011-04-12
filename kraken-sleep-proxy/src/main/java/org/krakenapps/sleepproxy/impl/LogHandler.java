package org.krakenapps.sleepproxy.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.jpa.handler.Transactional;
import org.krakenapps.sleepproxy.LogListener;
import org.krakenapps.sleepproxy.LogProvider;
import org.krakenapps.sleepproxy.SleepProxyApi;
import org.krakenapps.sleepproxy.model.Agent;
import org.krakenapps.sleepproxy.model.AgentGroup;
import org.krakenapps.sleepproxy.model.NetworkAdapter;
import org.krakenapps.sleepproxy.model.SleepLog;
import org.krakenapps.sleepproxy.model.SleepLog.Status;
import org.krakenapps.syslog.Syslog;
import org.krakenapps.syslog.SyslogListener;
import org.krakenapps.syslog.SyslogServer;
import org.krakenapps.syslog.SyslogServerRegistry;
import org.krakenapps.syslog.SyslogServerRegistryEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "sleep-proxy-log-handler")
@Provides(specifications = { LogProvider.class })
@JpaConfig(factory = "sleep-proxy")
public class LogHandler implements SyslogListener, SyslogServerRegistryEventListener, LogProvider {
	private static final int DEFAULT_POWER_CONSUMPTION = 350;

	private final Logger logger = LoggerFactory.getLogger(LogHandler.class.getName());

	private Set<LogListener> callbacks = Collections.newSetFromMap(new ConcurrentHashMap<LogListener, Boolean>());

	@Requires
	private SyslogServerRegistry registry;

	@Requires
	private ThreadLocalEntityManagerService entityManagerService;

	@Requires
	private SleepProxyApi sleepProxyApi;

	@Validate
	public void start() {
		registry.addEventListener(this);

		if (registry.contains("sleep-syslog")) {
			SyslogServer server = registry.getServer("sleep-syslog");
			server.addListener(this);

			logger.info("sleep proxy: added syslog callback");
		}
	}

	@Invalidate
	public void stop() {
		if (registry == null)
			return;

		registry.removeEventListener(this);

		SyslogServer server = registry.getServer("sleep-syslog");
		if (server == null)
			return;

		server.removeListener(this);
	}

	@Override
	public void syslogServerAdded(String name, SyslogServer server) {
		logger.debug("sleep proxy: {} syslog server added", name);

		if (name.equals("sleep-syslog")) {
			logger.info("sleep proxy: syslog server started");
			server.addListener(this);
		}
	}

	@Override
	public void syslogServerRemoved(String name, SyslogServer server) {
		logger.debug("sleep proxy: {} syslog server removed", name);

		if (name.equals("sleep-syslog")) {
			server.removeListener(this);
			logger.info("sleep proxy: syslog server stopped");
		}
	}

	@Override
	public void onReceive(Syslog syslog) {
		logger.info("sleep proxy: agent [{}] log received [{}]", syslog.getRemoteAddress(), syslog.getMessage());

		try {
			LogMessage msg = LogParser.parse(syslog.getMessage());
			handle(msg);
		} catch (Exception e) {
			logger.error("sleep proxy: cannot receive log", e);
		}
	}

	@Transactional
	private void handle(LogMessage msg) {
		EntityManager em = entityManagerService.getEntityManager();
		int type = msg.getMsgType();

		switch (type) {
		case 1:
			handleIdle(em, msg);
			break;
		case 2:
			handleBusy(em, msg);
			break;
		case 3:
			handleHibernate(em, msg);
			break;
		case 4:
			handleWakeup(em, msg);
			break;
		case 5:
			handleHeartbeat(em, msg);
			break;
		}
	}

	private void handleHeartbeat(EntityManager em, LogMessage msg) {
		AgentGroup root = sleepProxyApi.getRootAgentGroup();
		if (root == null)
			throw new IllegalStateException("root agent group not found");

		Agent agent = getAgent(em, msg);
		if (agent != null) {
			dispatch(agent, Status.Heartbeat);
			updateAgent(em, msg, agent);
		} else {
			createAgent(em, msg, root);
		}
	}

	private void updateAgent(EntityManager em, LogMessage msg, Agent agent) {
		// update host status
		agent.setUserName(msg.getUserName());
		agent.setHostName(msg.getHostName());
		agent.setDomainName(msg.getDomain());
		agent.setUpdated(new Date());

		// check adapter changes
		Set<String> newMacs = toNicMacSet(msg.getNetworkAdapters());
		Set<String> oldMacs = toAdapterMacSet(agent.getAdapters());

		Set<String> removed = new HashSet<String>(oldMacs);
		removed.removeAll(newMacs);

		Set<String> updated = new HashSet<String>(oldMacs);
		updated.retainAll(newMacs);

		Set<String> added = new HashSet<String>(newMacs);
		added.removeAll(oldMacs);

		for (String mac : removed) {
			NetworkAdapter adapter = findAdapter(agent, mac);
			agent.getAdapters().remove(adapter);
		}

		for (String mac : updated) {
			NetworkAdapter adapter = findAdapter(agent, mac);
			NicInfo nic = findNic(msg, mac);
			adapter.setIp(nic.getIp().getHostAddress());
			adapter.setUpdated(new Date());
		}

		for (String mac : added) {
			NetworkAdapter adapter = new NetworkAdapter();
			NicInfo nic = findNic(msg, mac);
			adapter.setAgent(agent);
			adapter.setMac(nic.getMac());
			adapter.setIp(nic.getIp().getHostAddress());
			adapter.setDescription(nic.getDescription());
			adapter.setCreated(new Date());
			adapter.setUpdated(new Date());
		}

		em.merge(agent);
	}

	private NicInfo findNic(LogMessage msg, String mac) {
		for (NicInfo nic : msg.getNetworkAdapters())
			if (nic.getMac().equals(mac))
				return nic;

		return null;
	}

	private NetworkAdapter findAdapter(Agent agent, String mac) {
		for (NetworkAdapter adapter : agent.getAdapters())
			if (adapter.getMac().equals(mac))
				return adapter;

		return null;
	}

	private Set<String> toNicMacSet(Collection<NicInfo> nics) {
		Set<String> s = new HashSet<String>();
		for (NicInfo n : nics)
			s.add(n.getMac());
		return s;
	}

	private Set<String> toAdapterMacSet(Collection<NetworkAdapter> nics) {
		Set<String> s = new HashSet<String>();
		for (NetworkAdapter n : nics)
			s.add(n.getMac());
		return s;
	}

	private void createAgent(EntityManager em, LogMessage msg, AgentGroup root) {
		Agent agent;
		agent = new Agent();
		agent.setAgentGroup(root);
		agent.setGuid(msg.getGuid());
		agent.setUserName(msg.getUserName());
		agent.setHostName(msg.getHostName());
		agent.setDomainName(msg.getDomain());
		agent.setPowerConsumtion(DEFAULT_POWER_CONSUMPTION);
		agent.setCreated(new Date());
		agent.setUpdated(new Date());

		for (NicInfo nic : msg.getNetworkAdapters()) {
			NetworkAdapter adapter = new NetworkAdapter();
			adapter.setAgent(agent);
			adapter.setMac(nic.getMac());
			adapter.setIp(nic.getIp().getHostAddress());
			adapter.setDescription(nic.getDescription());
			adapter.setCreated(new Date());
			adapter.setUpdated(new Date());
			agent.getAdapters().add(adapter);
		}

		em.persist(agent);
	}

	private void handleIdle(EntityManager em, LogMessage msg) {
		Agent agent = getAgent(em, msg);
		dispatch(agent, Status.Idle);
	}

	private void handleBusy(EntityManager em, LogMessage msg) {
		Agent agent = getAgent(em, msg);
		dispatch(agent, Status.Busy);
	}

	private void handleHibernate(EntityManager em, LogMessage msg) {
		Agent agent = getAgent(em, msg);
		if (agent == null) {
			logger.warn("sleep proxy: agent [{}, guid={}] not found", msg.getHostName(), msg.getGuid());
			return;
		}

		dispatch(agent, Status.Suspend);

		SleepLog log = new SleepLog();
		log.setAgent(agent);
		log.setHostName(agent.getHostName());
		log.setStatus(SleepLog.Status.Suspend);
		log.setCreated(new Date());

		em.persist(log);
	}

	private void handleWakeup(EntityManager em, LogMessage msg) {
		Agent agent = getAgent(em, msg);
		if (agent == null) {
			logger.warn("sleep proxy: agent [{}, guid={}] not found", msg.getHostName(), msg.getGuid());
			return;
		}

		dispatch(agent, Status.Resume);

		SleepLog log = new SleepLog();
		log.setAgent(agent);
		log.setHostName(agent.getHostName());
		log.setStatus(SleepLog.Status.Resume);
		log.setCreated(new Date());

		em.persist(log);
	}

	private Agent getAgent(EntityManager em, LogMessage msg) {
		try {
			return (Agent) em.createQuery("FROM Agent a WHERE a.guid = ?").setParameter(1, msg.getGuid())
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	//
	// Log Provider
	//

	private void dispatch(Agent agent, Status status) {
		for (LogListener callback : callbacks) {
			try {
				callback.onReceive(agent, status);
			} catch (Exception e) {
				logger.error("sleep proxy: log listener should not throw any exception", e);
			}
		}
	}

	@Override
	public void register(LogListener callback) {
		callbacks.add(callback);
	}

	@Override
	public void unregister(LogListener callback) {
		callbacks.remove(callback);
	}

}
