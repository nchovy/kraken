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
package org.krakenapps.portmon.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.cron.PeriodicJob;
import org.krakenapps.portmon.PortEventListener;
import org.krakenapps.portmon.PortMonitor;
import org.krakenapps.portmon.PortStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PeriodicJob("* * * * *")
@Component(name = "port-monitor")
@Provides
public class PortMonitorImpl implements PortMonitor, Runnable {
	private ConcurrentMap<InetSocketAddress, LastStatus> tcpTargets;
	private Set<PortEventListener> callbacks;
	private int timeout;
	private ExecutorService threadPool;

	public PortMonitorImpl() {
		tcpTargets = new ConcurrentHashMap<InetSocketAddress, LastStatus>();
		callbacks = Collections.newSetFromMap(new ConcurrentHashMap<PortEventListener, Boolean>());
		timeout = 10000; // 10seconds
		threadPool = Executors.newCachedThreadPool();
	}

	@Override
	public void setTimeout(int milliseconds) {
		this.timeout = milliseconds;
	}

	@Override
	public Collection<InetSocketAddress> getTcpTargets() {
		return new ArrayList<InetSocketAddress>(tcpTargets.keySet());
	}

	@Override
	public PortStatus getTcpPortStatus(InetSocketAddress target) {
		return tcpTargets.get(target);
	}

	@Override
	public void run() {
		List<Callable<Void>> tasks = new ArrayList<Callable<Void>>(tcpTargets.size());
		for (InetSocketAddress target : tcpTargets.keySet()) {
			TcpChecker checker = new TcpChecker(target, timeout);
			tasks.add(checker);
		}

		try {
			threadPool.invokeAll(tasks);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addTcpTarget(InetSocketAddress target) {
		if (target == null)
			throw new IllegalArgumentException("target must be not null");

		tcpTargets.put(target, new LastStatus());
	}

	@Override
	public void removeTcpTarget(InetSocketAddress target) {
		if (target == null)
			throw new IllegalArgumentException("target must be not null");

		tcpTargets.remove(target);
	}

	@Override
	public void addListener(PortEventListener callback) {
		if (callback == null)
			throw new IllegalArgumentException("callback must be not null");

		callbacks.add(callback);
	}

	@Override
	public void removeListener(PortEventListener callback) {
		if (callback == null)
			throw new IllegalArgumentException("callback must be not null");

		callbacks.remove(callback);
	}

	private static class LastStatus implements PortStatus {
		public Date check;
		public boolean status;
		public Date success;
		public Date fail;

		@Override
		public Date getLastCheckTime() {
			return check;
		}

		@Override
		public boolean getLastStatus() {
			return status;
		}

		@Override
		public String toString() {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return String.format("status=%s, check=%s", status ? "up" : "down", check == null ? "none" : dateFormat
					.format(check));
		}
	}

	private void fireTcpConnectCallback(InetSocketAddress target, int connectTime) {
		for (PortEventListener callback : callbacks) {
			callback.onConnect(target, connectTime);
		}
	}

	private void fireTcpConnectRefusedCallback(InetSocketAddress target, int timeout, IOException e) {
		for (PortEventListener callback : callbacks) {
			callback.onConnectRefused(target, timeout, e);
		}
	}

	private class TcpChecker implements Callable<Void> {
		private final Logger logger = LoggerFactory.getLogger(TcpChecker.class.getName());
		private InetSocketAddress target;
		private int timeout;

		public TcpChecker(InetSocketAddress target, int timeout) {
			this.target = target;
			this.timeout = timeout;
		}

		@Override
		public Void call() {
			LastStatus status = tcpTargets.get(target);
			if (status == null)
				return null;

			Date beginDate = new Date();
			status.check = beginDate;
			Socket socket = new Socket();
			try {
				logger.trace("kraken-portmon: try to connect {}", target);

				long begin = new Date().getTime();
				socket.connect(target, timeout);
				long end = new Date().getTime();

				logger.trace("kraken-portmon: {} connected", target);

				socket.close();

				status.success = beginDate;
				status.status = true;

				fireTcpConnectCallback(target, (int) (end - begin));
			} catch (IOException e) {
				status.fail = beginDate;
				status.status = false;

				fireTcpConnectRefusedCallback(target, timeout, e);
			}

			return null;
		}
	}
}
