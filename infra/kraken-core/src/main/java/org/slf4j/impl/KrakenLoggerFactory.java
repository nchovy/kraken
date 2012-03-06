/*
 * Copyright 2009 NCHOVY
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
package org.slf4j.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

public class KrakenLoggerFactory implements ILoggerFactory, Runnable {
	final static KrakenLoggerFactory INSTANCE = new KrakenLoggerFactory();

	private Map<String, KrakenLogger> loggerMap;
	private LinkedBlockingQueue<KrakenLog> queue;
	private Thread t;
	private boolean doStop = false;
	private Map<Integer, BlockingQueue<KrakenLog>> monitors;
	private Random rand;

	private KrakenLoggerFactory() {
		loggerMap = new HashMap<String, KrakenLogger>();
		queue = new LinkedBlockingQueue<KrakenLog>();
		monitors = new ConcurrentHashMap<Integer, BlockingQueue<KrakenLog>>();
		rand = new Random(System.currentTimeMillis());
	}

	public void start() {
		t = new Thread(this, "Kraken Log Monitor");
		t.start();
	}

	public void stop() {
		doStop = true;
		t.interrupt();
	}

	@Override
	public void run() {
		while (true) {
			try {
				KrakenLog log = queue.poll(100, TimeUnit.MILLISECONDS);
				if (doStop)
					break;

				if (log == null)
					continue;

				for (BlockingQueue<KrakenLog> monitor : monitors.values()) {
					monitor.add(log);
				}
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	@Override
	public Logger getLogger(String name) {
		KrakenLogger logger = null;

		synchronized (this) {
			logger = loggerMap.get(name);
			if (logger == null) {
				logger = new KrakenLogger(name, queue);
				loggerMap.put(name, logger);
			}
		}

		return logger;
	}

	public boolean hasLogger(String name) {
		synchronized (this) {
			return loggerMap.get(name) != null;
		}
	}

	public List<String> getLoggerList() {
		List<String> loggers = new ArrayList<String>();
		synchronized (this) {
			for (String key : loggerMap.keySet()) {
				loggers.add(key);
			}
		}

		Collections.sort(loggers);
		return loggers;
	}

	public void setLogLevel(String name, String level, boolean isEnabled) {
		KrakenLogger logger = null;
		synchronized (this) {
			logger = loggerMap.get(name);
		}

		if (logger == null)
			return;

		if (level.equalsIgnoreCase("debug")) {
			logger.setDebugEnabled(isEnabled);
		} else if (level.equalsIgnoreCase("trace")) {
			logger.setTraceEnabled(isEnabled);
		} else if (level.equalsIgnoreCase("info")) {
			logger.setInfoEnabled(isEnabled);
		} else if (level.equalsIgnoreCase("warn")) {
			logger.setWarnEnabled(isEnabled);
		} else if (level.equalsIgnoreCase("error")) {
			logger.setErrorEnabled(isEnabled);
		}
	}

	public int createMonitor() {
		int monitorId = rand.nextInt(10000);
		monitors.put(monitorId, new LinkedBlockingQueue<KrakenLog>());
		return monitorId;
	}

	public void destroyMonitor(int monitorId) {
		BlockingQueue<KrakenLog> monitor = monitors.remove(monitorId);
		if (monitor != null)
			monitor.clear();
	}

	public KrakenLog getLog(int monitorId) throws IllegalStateException, InterruptedException {
		BlockingQueue<KrakenLog> q = monitors.get(monitorId);
		if (q == null)
			throw new IllegalStateException("monitor not found.");

		return q.take();
	}

}
