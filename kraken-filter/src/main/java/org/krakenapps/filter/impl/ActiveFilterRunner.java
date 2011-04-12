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
package org.krakenapps.filter.impl;

import org.krakenapps.filter.ActiveFilter;
import org.krakenapps.filter.exception.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides thread management for the active filter.
 * 
 * @author xeraph
 * @since 1.0.0
 */
public class ActiveFilterRunner implements Runnable {
	final Logger logger = LoggerFactory.getLogger(ActiveFilterRunner.class);
	private volatile boolean doStop;
	private long sleepMilliseconds;
	private ActiveFilter activeFilter;
	private Thread thread;

	/**
	 * Creates an active filter with 1 second of sleep interval.
	 * 
	 * @param activeFilter
	 */
	public ActiveFilterRunner(ActiveFilter activeFilter) {
		this(activeFilter, 1000);
	}

	/**
	 * Creates an active filter that operates periodically Period is expressed
	 * in millisecond.
	 */
	public ActiveFilterRunner(ActiveFilter activeFilter, long period) {
		doStop = false;
		sleepMilliseconds = period;
		this.activeFilter = activeFilter;
		thread = new Thread(this);
	}

	/**
	 * Runs the infinite loop of the active filter.
	 */
	@Override
	public void run() {
		logger.info("starting active filter: class [{}], filter id [{}]", activeFilter.getClass()
				.getName(), activeFilter.getProperty("instance.name"));
		try {
			activeFilter.validateConfiguration();
			activeFilter.open();
			activeFilter.setRunning(true);
		} catch (ConfigurationException e) {
			logger.error("configuration error: {} - {}", e.getConfigurationName(), e
					.getErrorMessage());
			return;
		}

		while (true) {
			if (doStop)
				break;

			try {
				if (sleepMilliseconds == 0)
					activeFilter.run();
				else {
					long time = -System.currentTimeMillis();
					activeFilter.run();
					time += System.currentTimeMillis();
					long netPeriod = sleepMilliseconds - time;
					if (netPeriod > 0)
						Thread.sleep(netPeriod);
				}
			} catch (InterruptedException e) {
				logger.info("active filter interrupted: "
						+ activeFilter.getProperty("instance.name"));
				break;
			} catch (Exception e) {
				logger.error("active filter run() exception", e);
				break;
			}
		}

		activeFilter.close();
		activeFilter.setRunning(false);

		logger.info("active filter thread stopped.");

		// create new thread instance for next run
		thread = new Thread(this);
	}

	/**
	 * Returns the sleep interval of the active filter in milliseconds unit.
	 */
	public long getSleepInterval() {
		return sleepMilliseconds;
	}

	/**
	 * Starts the thread for the active filter.
	 */
	public void start() {
		logger.info("starting active filter runner thread.");
		thread.start();
	}

	/**
	 * Requests to stop thread of the active filter.
	 */
	public void stop() {
		doStop = true;
		thread.interrupt();
	}

	/**
	 * Waits termination of the active filter.
	 */
	@SuppressWarnings("deprecation")
	public void waitToFinish() {
		int i;
		int MAX_WAIT_TURNS = 50;
		for (i = 0; i < MAX_WAIT_TURNS; i++) {
			if (activeFilter.isRunning() == false)
				return;

			if (i % 10 == 0)
				logger.info("Waiting thread stop.");

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				logger.info("Active filter interrupted.");
				break;
			}
		}

		if (i == MAX_WAIT_TURNS)
			thread.stop();
	}
}
