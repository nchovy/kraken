package org.krakenapps.sleepproxy.impl;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.cron.PeriodicJob;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.jpa.handler.Transactional;
import org.krakenapps.sleepproxy.LogListener;
import org.krakenapps.sleepproxy.LogProvider;
import org.krakenapps.sleepproxy.model.Agent;
import org.krakenapps.sleepproxy.model.PowerLog;
import org.krakenapps.sleepproxy.model.SleepLog.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "sleep-proxy-power-stats")
@Provides(specifications = { Runnable.class })
@JpaConfig(factory = "sleep-proxy")
@PeriodicJob("*/5 * * * *")
public class PowerStatsGenerator implements LogListener, Runnable {
	private final Logger logger = LoggerFactory.getLogger(PowerStatsGenerator.class.getName());

	private static final int POWER = 300;
	private static final int IDLE_POWER = 100;
	private static final int LOW_POWER = 20;

	@Requires
	private LogProvider provider;

	@Requires
	private ThreadLocalEntityManagerService entityManagerService;

	private Map<String, PowerStatus> statusMap;

	@Validate
	public void start() {
		statusMap = new HashMap<String, PowerStatus>();
		provider.register(this);
	}

	@Invalidate
	public void stop() {
		if (provider != null)
			provider.unregister(this);

		statusMap.clear();
	}

	@Override
	public void run() {
		Map<String, PowerLog> logs = new HashMap<String, PowerLog>();
		Map<String, Agent> agents = getAgents();

		// align to 5min boundary
		Date now = normalize(new Date());

		synchronized (statusMap) {
			// dump accumulated power and clear all
			for (String guid : statusMap.keySet()) {
				PowerStatus status = statusMap.get(guid);
				status.flush();
				logger.debug("sleep proxy: dumping log [{}]", status);

				PowerLog log = new PowerLog();
				Agent agent = agents.get(guid);
				if (agent == null) {
					logger.warn("sleep proxy: agent {} not found", guid);
					continue;
				}

				log.setDate(now);
				log.setAgent(agent);
				log.setUsed(POWER * status.used);
				log.setCanSaved(IDLE_POWER * status.canSaved);
				log.setSaved(LOW_POWER * status.saved);

				logs.put(guid, log);

				// clear old values
				status.clear();
			}
		}

		savePowerLogs(logs.values());
	}

	@Transactional
	private void savePowerLogs(Collection<PowerLog> logs) {
		EntityManager em = entityManagerService.getEntityManager();

		for (PowerLog log : logs) {
			em.persist(log);
		}
	}

	private Date normalize(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) * 5 / 5);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	@SuppressWarnings("unchecked")
	@Transactional
	private Map<String, Agent> getAgents() {
		Map<String, Agent> m = new HashMap<String, Agent>();
		EntityManager em = entityManagerService.getEntityManager();

		List<Agent> agents = em.createQuery("FROM Agent a").getResultList();
		for (Agent agent : agents)
			m.put(agent.getGuid(), agent);

		return m;
	}

	@Override
	public void onReceive(Agent agent, Status status) {
		if (agent == null) {
			logger.warn("sleep proxy: agent is null");
			return;
		}

		logger.debug("sleep proxy: received agent [{},{}], status {}", new Object[] { agent.getId(),
				agent.getHostName(), status });

		synchronized (statusMap) {
			if (!statusMap.containsKey(agent.getGuid())) {
				statusMap.put(agent.getGuid(), new PowerStatus(agent.getGuid(), status));
				return;
			}

			PowerStatus old = statusMap.get(agent.getGuid());
			old.update(status);
		}
	}

	private static class PowerStatus {
		private String guid;
		private Date lastHeartbeat;
		private Date lastUpdate;
		private Status lastStatus;

		// seconds
		private int used;
		private int saved;
		private int canSaved;

		public PowerStatus(String guid, Status s) {
			this.guid = guid;
			this.lastHeartbeat = new Date();
			this.lastUpdate = new Date();
			this.lastStatus = Status.Busy;
			this.used = 0;
			this.saved = 0;
			this.canSaved = 0;
		}

		public void update(Status s) {
			Date now = new Date();

			int liveInterval = (int) (now.getTime() - lastHeartbeat.getTime()) / 1000;
			if (liveInterval > 60) {
				lastStatus = s;
				lastHeartbeat = now;
				lastUpdate = now;
				return;
			}
			
			if (s == Status.Heartbeat) {
				lastHeartbeat = now;
				return;
			}

			int seconds = (int) (now.getTime() - lastUpdate.getTime()) / 1000;
			if (lastStatus == Status.Busy) {
				used += seconds;
			} else if (lastStatus == Status.Idle) {
				used += seconds;
				canSaved += seconds;
			} else if (lastStatus == Status.Suspend) {
				saved += seconds;
			}

			// update status
			lastStatus = s;
			lastUpdate = now;
		}

		public void flush() {
			Date now = new Date();
			int seconds = (int) (now.getTime() - lastUpdate.getTime()) / 1000;
			if (lastStatus == Status.Busy) {
				used += seconds;
			} else if (lastStatus == Status.Idle) {
				used += seconds;
				canSaved += seconds;
			} else if (lastStatus == Status.Suspend) {
				saved += seconds;
			}

			lastUpdate = now;
		}

		public void clear() {
			used = 0;
			canSaved = 0;
			saved = 0;
		}

		@Override
		public String toString() {
			return String.format("%s, last update=%s, last status=%s, used=%d, saved=%d, cansaved=%d", guid,
					lastUpdate, lastStatus, used, saved, canSaved);
		}
	}
}
