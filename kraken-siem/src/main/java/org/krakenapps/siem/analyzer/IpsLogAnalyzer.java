package org.krakenapps.siem.analyzer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.event.api.Event;
import org.krakenapps.event.api.EventDispatcher;
import org.krakenapps.event.api.EventKey;
import org.krakenapps.event.api.EventSeverity;
import org.krakenapps.siem.LogServer;
import org.krakenapps.siem.NormalizedLog;
import org.krakenapps.siem.NormalizedLogListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "siem-ips-log-analyzer")
public class IpsLogAnalyzer implements NormalizedLogListener {
	private final Logger logger = LoggerFactory.getLogger(IpsLogAnalyzer.class.getName());

	@Requires
	private LogServer logServer;

	@Requires
	private EventDispatcher eventDispatcher;

	private ConcurrentMap<AttackKey, AttackEvent> attacks;

	@Validate
	public void start() {
		attacks = new ConcurrentHashMap<AttackKey, AttackEvent>();
		logServer.addNormalizedLogListener("ips", this);
	}

	@Invalidate
	public void stop() {
		if (logServer != null)
			logServer.removeNormalizedLogListener("ips", this);
	}

	@Override
	public void onLog(NormalizedLog log) {
		logger.info("kraken log api: received ips log [{}]", log);

		try {
			AttackKey key = new AttackKey(log.getString("src"), log.getString("dst"), log.getString("rule"));

			Event event = new Event();
			event.setOrgDomain(log.getOrgDomain());
			event.setFirstSeen(log.getDate("date"));
			event.setLastSeen(log.getDate("date"));
			event.setSourceIp(InetAddress.getByName(log.getString("src_ip")));
			event.setSourcePort(log.getInteger("src_port"));
			event.setDestinationIp(InetAddress.getByName(log.getString("dst_ip")));
			event.setDestinationPort(log.getInteger("dst_port"));
			event.setSeverity(EventSeverity.values()[log.getInteger("severity")]);
			event.setRule(log.getString("rule"));
			event.setDetail(log.getString("detail"));
			event.setCategory("Attack");
			event.setCount(log.getInteger("count"));

			// save until ack
			AttackEvent old = attacks.putIfAbsent(key, new AttackEvent(event));
			if (old != null) {
				event.setKey(old.event.getKey());
				event.setCount(old.count.addAndGet(event.getCount()));
			}

			eventDispatcher.dispatch(event);
		} catch (UnknownHostException e) {
		}
	}

	private static class AttackKey {
		private String src;
		private String dst;
		private String rule;

		public AttackKey(String src, String dst, String rule) {
			this.src = src;
			this.dst = dst;
			this.rule = rule;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((dst == null) ? 0 : dst.hashCode());
			result = prime * result + ((rule == null) ? 0 : rule.hashCode());
			result = prime * result + ((src == null) ? 0 : src.hashCode());
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
			AttackKey other = (AttackKey) obj;
			if (dst == null) {
				if (other.dst != null)
					return false;
			} else if (!dst.equals(other.dst))
				return false;
			if (rule == null) {
				if (other.rule != null)
					return false;
			} else if (!rule.equals(other.rule))
				return false;
			if (src == null) {
				if (other.src != null)
					return false;
			} else if (!src.equals(other.src))
				return false;
			return true;
		}
	}

	private static class AttackEvent {
		private Event event;
		private AtomicInteger count;

		public AttackEvent(Event event) {
			this.event = event;
			this.count = new AtomicInteger(1);
		}
	}
}
