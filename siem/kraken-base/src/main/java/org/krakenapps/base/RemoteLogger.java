/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.base;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.krakenapps.log.api.AbstractLogger;
import org.krakenapps.log.api.Log;

public class RemoteLogger extends AbstractLogger {
	private final org.slf4j.Logger slog = org.slf4j.LoggerFactory.getLogger(RemoteLogger.class.getName());
	private SentryProxy proxy;
	private boolean isRunning;
	private int interval;

	public RemoteLogger(SentryProxy proxy, String name, String factoryName, String description, Properties config) {
		super(proxy.getGuid(), name, proxy.getGuid(), factoryName, description, config);
		this.proxy = proxy;
	}

	public void onLog(Log log) {
		try {
			write(log);
		} catch (Exception e) {
			slog.error("kraken base: cannot save log", e);
		}
	}

	@Override
	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	@Override
	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	@Override
	public void run() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void runOnce() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void start(int interval) {
		try {
			proxy.startRemoteLogger(getName(), interval);
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Override
	public void stop() {
		stop(5000);
	}

	@Override
	public void stop(int maxWaitTime) {
		try {
			setRunning(false);

			if (proxy.isOpen())
				proxy.stopRemoteLogger(getName(), maxWaitTime);
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Override
	public String toString() {
		String a = toString(getLastLogDate());
		return String.format("name=%s, factory=%s, log count=%d, last log=%s, running=%s, interval=%d", getFullName(),
				getFactoryFullName(), getLogCount(), a, isRunning, interval);
	}

	private String toString(Date d) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if (d == null)
			return null;

		return dateFormat.format(d);
	}
}
