package org.krakenapps.sleepproxy.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.jpa.handler.Transactional;
import org.krakenapps.sleepproxy.ConfigKey;
import org.krakenapps.sleepproxy.ConfigStore;
import org.krakenapps.sleepproxy.model.Agent;
import org.krakenapps.sleepproxy.model.AgentGroup;
import org.krakenapps.sleepproxy.model.SleepPolicy;
import org.krakenapps.webconsole.ResourceServlet;
import org.krakenapps.webconsole.ServletRegistry;
import org.krakenapps.webconsole.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "sleep-proxy-policy-provider")
@JpaConfig(factory = "sleep-proxy")
public class PolicyProvider extends ResourceServlet {
	private static final long serialVersionUID = 1L;

	private final Logger logger = LoggerFactory.getLogger(PolicyProvider.class.getName());

	@Requires
	private ThreadLocalEntityManagerService entityManagerService;

	@Requires
	WebSocketServer server;

	@Requires
	private ServletRegistry servletRegistry;

	@Requires
	private ConfigStore cs;

	@Validate
	public void start() {
		servletRegistry.register("/ksp", this);
	}

	@Invalidate
	public void stop() {
		if (servletRegistry != null)
			servletRegistry.unregister("/ksp");
	}

	private String getDefaultIp() {
		try {
			Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
			while (n.hasMoreElements()) {
				NetworkInterface ni = n.nextElement();
				Enumeration<InetAddress> addrs = ni.getInetAddresses();
				while (addrs.hasMoreElements()) {
					InetAddress addr = addrs.nextElement();

					if (addr.isLoopbackAddress())
						continue;

					if (addr instanceof Inet4Address)
						return addr.getHostAddress();
				}
			}
		} catch (SocketException e) {
		}

		return "127.0.0.1";
	}

	@Override
	protected InputStream getInputStream(HttpServletRequest req) {
		String guid = req.getParameter("GUID");
		logger.info("sleep proxy: fetch policy for {}", guid);

		SleepPolicy policy = getPolicy(guid);
		if (policy == null) {
			logger.warn("sleep proxy: default policy not found for guid [{}]", guid);
			return null;
		}

		String interval = cs.get(ConfigKey.PolicyInterval, "70");
		String syslogIp = cs.get(ConfigKey.SyslogIP, getDefaultIp());
		String syslogPort = cs.get(ConfigKey.SyslogPort, "514");
		String logAddr = syslogIp + ":" + syslogPort;
		String heartbeatInterval = cs.get(ConfigKey.HeartbeatInterval, "60");

		int port = server.getBindings().iterator().next().getPort();
		String url = cs.get(ConfigKey.PolicyUrl, "http://" + syslogIp + ":" + port + "/ksp/policy");

		String content = "[policy]\n" + "url = " + url + "\n" + "interval = " + interval + "\n" + "[power]\n"
				+ "addr = " + logAddr + "\n" + "heartbeat_interval = " + heartbeatInterval + "\n" + "away = "
				+ policy.getAwayCriteria() + "\n" + "force_hibernate = " + policy.getForceHibernate() + "\n";

		try {
			return new ByteArrayInputStream(content.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	@Transactional
	private SleepPolicy getPolicy(String guid) {
		// very naive approach (it needs policy cache)
		try {
			AgentGroup group = getAgentGroup(guid);
			do {
				if (group.getPolicy() != null)
					return group.getPolicy();

				group = group.getParent();
			} while (group.getParent() != null);

		} catch (NoResultException e) {
			return null;
		}

		return null;
	}

	private AgentGroup getAgentGroup(String guid) {
		EntityManager em = entityManagerService.getEntityManager();
		try {
			Agent agent = (Agent) em.createQuery("FROM Agent a WHERE a.guid = ?").setParameter(1, guid)
					.getSingleResult();

			return agent.getAgentGroup();
		} catch (NoResultException e) {
			// default reference is root group
			return (AgentGroup) em.createQuery("FROM AgentGroup g WHERE g.parent IS NULL").getSingleResult();
		}
	}

	@Override
	public String toString() {
		return "sleep proxy policy provider";
	}
}
