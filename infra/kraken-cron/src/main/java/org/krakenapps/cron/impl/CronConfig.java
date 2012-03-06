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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.krakenapps.confdb.CollectionName;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.cron.Schedule;

/**
 * This class handles jdbc operations associated with cron schedules. uses hsql
 * db.
 * 
 * @author periphery
 * @since 1.0.0
 */
public class CronConfig {
	private ConfigDatabase db;
	private AtomicInteger maxId = new AtomicInteger();

	public CronConfig(ConfigService conf) {
		this.db = conf.ensureDatabase("kraken-cron");
		loadMaxId();
	}

	private void loadMaxId() {
		Collection<ScheduleInfo> schedules = db.findAll(ScheduleInfo.class).getDocuments(ScheduleInfo.class);
		for (ScheduleInfo schedule : schedules) {
			if (maxId.get() < schedule.id) {
				maxId.set(schedule.id);
			}
		}
	}

	/**
	 * insert the schedule into db.
	 * 
	 * @param schedule
	 * @return id
	 */
	public int addEntry(Schedule schedule) {
		ScheduleInfo info = new ScheduleInfo();
		info.id = maxId.incrementAndGet();
		info.task = schedule.getTaskName();
		info.minute = schedule.get(CronField.Type.MINUTE).toString();
		info.hour = schedule.get(CronField.Type.HOUR).toString();
		info.dayOfMonth = schedule.get(CronField.Type.DAY_OF_MONTH).toString();
		info.month = schedule.get(CronField.Type.MONTH).toString();
		info.dayOfWeek = schedule.get(CronField.Type.DAY_OF_WEEK).toString();
		db.add(info);

		return info.id;
	}

	/**
	 * remove the schedule represented by the given id from db.
	 * 
	 * @param id
	 */
	public void removeEntry(int id) {
		Config c = db.findOne(ScheduleInfo.class, Predicates.field("id", id));
		if (c != null)
			db.remove(c);
	}

	/**
	 * select and return all the registered schedules from db.
	 * 
	 * @return id-schedule table
	 * @throws ParseException
	 *             when data in db is corrupted and unable to parse as schedule.
	 */
	public Map<Integer, Schedule> getEntries() throws ParseException {
		Map<Integer, Schedule> map = new HashMap<Integer, Schedule>();
		for (ScheduleInfo schedule : db.findAll(ScheduleInfo.class).getDocuments(ScheduleInfo.class)) {
			Schedule.Builder builder = new Schedule.Builder(schedule.task);
			builder.set(CronField.Type.MINUTE, schedule.minute);
			builder.set(CronField.Type.HOUR, schedule.hour);
			builder.set(CronField.Type.DAY_OF_MONTH, schedule.dayOfMonth);
			builder.set(CronField.Type.MONTH, schedule.month);
			builder.set(CronField.Type.DAY_OF_WEEK, schedule.dayOfWeek);
			map.put(schedule.id, builder.build());
		}
		return map;
	}

	@CollectionName("schedule")
	private static class ScheduleInfo {
		private int id;
		private String task;
		private String minute;
		private String hour;
		private String dayOfMonth;
		private String month;
		private String dayOfWeek;
	}
}
