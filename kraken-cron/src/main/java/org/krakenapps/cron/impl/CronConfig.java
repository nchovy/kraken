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

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.krakenapps.cron.Schedule;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles jdbc operations associated with cron schedules. uses hsql
 * db.
 * 
 * @author periphery
 * @since 1.0.0
 */
public class CronConfig {
	final Logger logger = LoggerFactory.getLogger(CronConfig.class.getName());
	private Preferences prefs;
	private AtomicInteger maxId = new AtomicInteger();

	public CronConfig(Preferences prefs) {
		this.prefs = prefs;
		loadMaxId();
	}

	private void loadMaxId() {
		try {
			Preferences schedules = getScheduleRoot();
			for (String name : schedules.childrenNames()) {
				int id = Integer.parseInt(name);
				if (maxId.get() < id) {
					maxId.set(id);
				}
			}
		} catch (BackingStoreException e) {
			logger.warn("kraken cron: fetch max id failed", e);
		}

	}

	/**
	 * insert the schedule into db.
	 * 
	 * @param schedule
	 * @return id
	 */
	public int addEntry(Schedule schedule) {
		try {
			Preferences schedules = getScheduleRoot();
			int nextId = maxId.incrementAndGet();
			Preferences entry = schedules.node(Integer.toString(nextId));

			entry.put("minute", schedule.get(CronField.Type.MINUTE).toString());
			entry.put("hour", schedule.get(CronField.Type.HOUR).toString());
			entry.put("day_of_month", schedule.get(CronField.Type.DAY_OF_MONTH).toString());
			entry.put("month", schedule.get(CronField.Type.MONTH).toString());
			entry.put("day_of_week", schedule.get(CronField.Type.DAY_OF_WEEK).toString());
			entry.put("task", schedule.getTaskName());

			schedules.flush();
			schedules.sync();

			return nextId;
		} catch (BackingStoreException e) {
			logger.warn("kraken cron: add entry failed", e);
			return -1;
		}
	}

	/**
	 * remove the schedule represented by the given id from db.
	 * 
	 * @param id
	 */
	public void removeEntry(int id) {
		try {
			Preferences schedules = getScheduleRoot();
			String name = Integer.toString(id);
			if (!schedules.nodeExists(name))
				return;

			schedules.node(name).removeNode();
			schedules.flush();
			schedules.sync();
		} catch (BackingStoreException e) {
			logger.warn("kraken cron: remove entry failed", e);
		}
	}

	/**
	 * select and return all the registered schedules from db.
	 * 
	 * @return id-schedule table
	 * @throws ParseException
	 *             when data in db is corrupted and unable to parse as schedule.
	 */
	public Map<Integer, Schedule> getEntries() throws ParseException {
		Preferences schedules = getScheduleRoot();
		Map<Integer, Schedule> map = new HashMap<Integer, Schedule>();
		try {
			for (String id : schedules.childrenNames()) {
				Preferences p = schedules.node(id);

				Schedule.Builder builder = new Schedule.Builder(p.get("task", null));
				builder.set(CronField.Type.MINUTE, p.get("minute", null));
				builder.set(CronField.Type.HOUR, p.get("hour", null));
				builder.set(CronField.Type.DAY_OF_MONTH, p.get("day_of_month", null));
				builder.set(CronField.Type.MONTH, p.get("month", null));
				builder.set(CronField.Type.DAY_OF_WEEK, p.get("day_of_week", null));
				map.put(Integer.parseInt(id), builder.build());
			}
			return map;
		} catch (BackingStoreException e) {
			logger.error("kraken cron: load schedule instances error", e);
		}
		return null;
	}

	private Preferences getScheduleRoot() {
		return prefs.node("/kraken_cron/schedules");
	}
}
