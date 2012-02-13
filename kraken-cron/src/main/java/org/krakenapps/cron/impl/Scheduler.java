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
package org.krakenapps.cron.impl;

import java.util.*;
import java.util.Map.Entry;

import org.krakenapps.cron.Schedule;
import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cron scheduler that checks and runs registered cron jobs. singleton class.
 * 
 * @author periphery
 * @since 1.0.0
 */
public class Scheduler {
	private final Logger logger = LoggerFactory.getLogger(CronConfig.class.getName());
	private PriorityQueue<Job> queue;
	private final Thread loop = new Thread(new Loop(), "Cron Scheduler");
	private boolean running;

	public void start(Map<Integer, Schedule> map) {
		queue = reset(map);
		running = true;
		loop.start();
	}

	public void stop() {
		running = false;
	}

	private PriorityQueue<Job> reset(Map<Integer, Schedule> map) {
		PriorityQueue<Job> tempQueue = new PriorityQueue<Job>();
		for (Entry<Integer, Schedule> en : map.entrySet()) {
			Job job = new Job(en.getKey().intValue(), en.getValue());
			tempQueue.add(job);
		}
		return tempQueue;
	}

	/**
	 * insert the schedule to the scheduling queue.
	 * 
	 * @param id
	 *            id of the new schedule
	 * @param sche
	 *            new schedule to add
	 */
	public void put(int id, Schedule sche) {
		Job job = new Job(id, sche);
		synchronized (queue) {
			queue.add(job);
		}
	}

	/**
	 * delete the schedule according to the given id from the scheduling queue.
	 * 
	 * @param id
	 *            id of the deleting schedule
	 */
	public void remove(int id) {
		synchronized (queue) {
			for (Object job : queue.toArray()) {
				if (((Job) job).getScheduleId() == id)
					queue.remove(job);
			}
		}
	}

	/**
	 * list jobs in the scheduling queue.
	 * 
	 * @return list of scheduling queue entries.
	 */
	public List<String> getJobList() {
		Object[] sorted = queue.toArray();
		Arrays.sort(sorted);
		List<String> result = new ArrayList<String>();
		for (Object job : sorted) {
			result.add(((Job) job).toString());
		}
		return result;
	}

	/**
	 * this class is used as a thread that periodically checks and runs
	 * scheduled tasks. the thread busy waits, sleeping for 10 seconds and
	 * checks whether the first task in the scheduling queue is time for
	 * execution. if so, it generates child thread to run the task. If the
	 * 'Schedule.running' is set to false when this thread wakes up, the thread
	 * terminates itself, meaning that there can be time gap between setting the
	 * 'Scheduler.running' to false and the actual stopping of the loop thread.
	 * 
	 * @author periphery
	 * 
	 */
	private class Loop implements Runnable {
		private int SLEEP_TIME = 10; // sec.

		@Override
		public void run() {
			logger.info("Cron: scheduler started");
			loop();
		}

		private void loop() {
			while (running) {
				checkAndRun();
				try {
					Thread.sleep(this.SLEEP_TIME * 1000); // sleep for 10 sec
				} catch (InterruptedException e) {
					logger.warn("Cron: scheduler Thread.sleep error.", e);
				}
			}
			logger.info("Cron: scheduler stopped");
		}

		private void checkAndRun() {
			synchronized (queue) {
				if (itIsTime()) {
					Job first = queue.poll();
					Thread runner = new Thread(new Runner(first.clone()), "Cron Runner");
					runner.start();

					// set base time as 1 min after current time
					first.setNextOccurence(new Date(new Date().getTime() + 60 * 1000));
					queue.add(first);
					// recursive call to check next Job
					checkAndRun();
				}
			}
		}

		public boolean itIsTime() {
			try {
				return queue.peek().isTimeToDo();
			} catch (Exception e) { // empty queue
				return false;
			}
		}
	}

	private class Runner implements Runnable {
		private final Job job;

		public Runner(Job job) {
			this.job = job;
		}

		@Override
		public void run() {
			try {
				logger.debug("Cron: run registered task " + job);
				job.run();
			} catch (NullPointerException e) {
				logger.debug("Cron: unable to run " + job + ". runnable \'" + job.schedule.getTaskName() + "\' is not active.");
			} catch (InvalidSyntaxException e) {
				logger.warn("Cron: scheduler instance.name syntax error.", e);
			}
		}
	}
}
