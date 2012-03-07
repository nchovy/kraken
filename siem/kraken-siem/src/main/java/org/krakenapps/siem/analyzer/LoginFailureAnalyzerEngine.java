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
package org.krakenapps.siem.analyzer;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.event.api.Event;
import org.krakenapps.event.api.EventDispatcher;
import org.krakenapps.event.api.EventProvider;
import org.krakenapps.event.api.EventSeverity;
import org.krakenapps.siem.LogServer;
import org.krakenapps.siem.NormalizedLog;
import org.krakenapps.siem.NormalizedLogListener;

@Component(name = "siem-login-failure-analyzer")
@Provides
public class LoginFailureAnalyzerEngine implements NormalizedLogListener, LoginFailureAnalyzer, EventProvider {
	private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(LoginFailureAnalyzer.class.getName());

	@Requires
	private LogServer logServer;

	@Requires
	private EventDispatcher eventDispatcher;

	private Set<LoginFailureEventListener> callbacks;

	private static ConcurrentMap<InetAddress, FailureStat> m = new ConcurrentHashMap<InetAddress, FailureStat>();
	private WallClock clock;

	// milliseconds
	private long idleTime;
	private long ignoreTime;
	private long eventPeriod;
	private int limitCount;

	/**
	 * EventProvider.getName()
	 */
	@Override
	public String getName() {
		return "login-bruteforce";
	}

	public LoginFailureAnalyzerEngine() {
		this.limitCount = 3;
		this.idleTime = 10000;
		this.ignoreTime = 5000;
		this.eventPeriod = 3600000;
		this.clock = new SystemWallClock();

		callbacks = Collections.newSetFromMap(new ConcurrentHashMap<LoginFailureEventListener, Boolean>());
	}

	@Validate
	public void start() {
		logServer.addNormalizedLogListener("login", this);
	}

	@Invalidate
	public void stop() {
		if (logServer != null)
			logServer.removeNormalizedLogListener("login", this);
	}

	@Override
	public void onLog(NormalizedLog log) {
		Date date = (Date) log.get("date");
		InetAddress ip = (InetAddress) log.get("src_ip");
		String result = log.getString("result");
		if (date == null || ip == null || result == null || result.equals("success"))
			return;

		if (logger.isDebugEnabled())
			logger.debug("kraken siem: received login log, ip [{}], access [{}]", ip, date);

		FailureStat fs = new FailureStat(ip, new AtomicInteger(1), date);
		FailureStat old = m.putIfAbsent(ip, fs);
		if (old != null)
			fs = old;

		long interval = getIntervalTime(date, fs.lastSeen);
		if (interval >= idleTime) {
			logger.debug("kraken siem: login failure interval {} reset {}", interval, ip);

			fs.firstSeen = date;
			fs.lastSeen = date;
			fs.count.set(0);
			fs.alerted = false;
		} else if (fs.count.get() >= limitCount) {
			fs.lastSeen = date;

			if (!fs.alerted || !isIgnored(fs.firstSeen)) {
				fs.alerted = true;

				if (logger.isTraceEnabled())
					traceAlert(ip, fs);

				if (fs.lastDispatch == null || clock.now().getTime() - fs.lastDispatch.getTime() >= eventPeriod) {
					generateEvent(fs.firstSeen, fs.lastSeen, fs.ip, fs.count.get());
					invokeCallbacks(ip, fs);
					fs.lastDispatch = clock.now();
				}
			}
		}

		fs.count.getAndIncrement();
		m.put(ip, fs);
	}

	private void traceAlert(InetAddress ip, FailureStat fs) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		logger.trace(
				"kraken siem: [{}] tries login bruteforce attack [{}] times, first seen [{}], last seen [{}]",
				new Object[] { ip.getHostAddress(), fs.count.get(), dateFormat.format(fs.firstSeen),
						dateFormat.format(fs.lastSeen) });
	}

	private void generateEvent(Date firstSeen, Date lastSeen, InetAddress source, int count) {
		// TODO: destination ip setup
		Event event = new Event();
		event.setCategory("Attack");
		event.setFirstSeen(firstSeen);
		event.setLastSeen(lastSeen);
		event.setSeverity(EventSeverity.Critical);
		event.setMessageKey("login-bruteforce");
		event.setSourceIp(source);
		event.setCount(count);

		eventDispatcher.dispatch(event);
	}

	private void invokeCallbacks(InetAddress ip, FailureStat fs) {
		for (LoginFailureEventListener callback : callbacks) {
			try {
				callback.onBruteforceAttack(ip, fs.count.get());
			} catch (Exception e) {
				logger.warn("kraken siem: login failure analyzer listener should not throw any exception", e);
			}
		}
	}

	public void setWallClock(WallClock clock) {
		this.clock = clock;
	}

	static class FailureStat {
		InetAddress ip;
		AtomicInteger count;
		Date firstSeen;
		Date lastSeen;
		Date lastDispatch;
		boolean alerted;

		public FailureStat(InetAddress ip, AtomicInteger count, Date firstSeen) {
			this.ip = ip;
			this.count = count;
			this.firstSeen = firstSeen;
			this.lastSeen = firstSeen;
			this.lastDispatch = null;
		}
	}

	public void register(LoginFailureEventListener event) {
		callbacks.add(event);
	}

	public void unregister(LoginFailureEventListener event) {
		callbacks.remove(event);
	}

	private boolean isIgnored(Date firstSeen) {
		Date now = clock.now();
		return now.getTime() - firstSeen.getTime() < ignoreTime;
	}

	private long getIntervalTime(Date recent, Date lastAccess) {
		return recent.getTime() - lastAccess.getTime();
	}

	private static class SystemWallClock implements WallClock {
		@Override
		public Date now() {
			return new Date();
		}
	}
}
