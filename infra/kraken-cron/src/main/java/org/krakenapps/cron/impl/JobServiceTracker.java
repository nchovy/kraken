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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.krakenapps.cron.CronService;
import org.krakenapps.cron.DailyJob;
import org.krakenapps.cron.DuplicatedScheduleException;
import org.krakenapps.cron.HourlyJob;
import org.krakenapps.cron.MinutelyJob;
import org.krakenapps.cron.MonthlyJob;
import org.krakenapps.cron.PeriodicJob;
import org.krakenapps.cron.Schedule;
import org.krakenapps.cron.WeeklyJob;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inspects new or removed Runnable services and manages schedules
 * automatically.
 * 
 * @author xeraph
 * 
 */
public class JobServiceTracker extends ServiceTracker {
	private final Logger logger = LoggerFactory.getLogger(JobServiceTracker.class.getName());

	private CronService cronService;
	private ConcurrentMap<String, List<Integer>> scheduleMap;

	public JobServiceTracker(BundleContext bundleContext, CronService cronService) {
		super(bundleContext, Runnable.class.getName(), null);

		this.cronService = cronService;
		this.scheduleMap = new ConcurrentHashMap<String, List<Integer>>();
	}

	/**
	 * inspects cron annotations of runnable instance and registers schedule
	 * automatically.
	 */
	@Override
	public Object addingService(ServiceReference reference) {
		Object service = super.addingService(reference);
		String instanceName = (String) reference.getProperty("instance.name");
		List<Integer> schedules = new ArrayList<Integer>();

		Annotation[] annotations = service.getClass().getAnnotations();
		for (Annotation annotation : annotations) {
			Class<?> clazz = annotation.annotationType();
			if (clazz.equals(MinutelyJob.class))
				addSchedule(schedules, new Schedule.Builder(instanceName).build());

			if (clazz.equals(HourlyJob.class))
				addSchedule(schedules, new Schedule.Builder(instanceName).buildHourly());

			if (clazz.equals(DailyJob.class))
				addSchedule(schedules, new Schedule.Builder(instanceName).buildDaily());

			if (clazz.equals(WeeklyJob.class))
				addSchedule(schedules, new Schedule.Builder(instanceName).buildWeekly());

			if (clazz.equals(MonthlyJob.class))
				addSchedule(schedules, new Schedule.Builder(instanceName).buildMonthly());

			if (clazz.equals(PeriodicJob.class)) {
				try {
					PeriodicJob periodicJob = (PeriodicJob) annotation;
					Schedule schedule = new Schedule.Builder(instanceName).build(periodicJob.value());
					addSchedule(schedules, schedule);
				} catch (Exception e) {
					logger.error("cron: periodic schedule syntax error", e);
				}
			}

			if (schedules.size() != 0)
				scheduleMap.put(instanceName, schedules);
		}

		return service;
	}

	private void addSchedule(List<Integer> schedules, Schedule schedule) {
		try {
			int id = cronService.registerSchedule(schedule);
			schedules.add(id);
			logger.trace("cron: adding schedule {}", id);
		} catch (DuplicatedScheduleException e) {
			// annotated class will cause duplicated schedule exception. since
			// schedule persist in db and loaded when bundle is restarted,
			// dynamically loaded OSGi service annotation will conflict with
			// pre-loaded oneself. you may ignore this here.
		} catch (Exception e) {
			logger.error("cron: add schedule error", e);
		}
	}

	/**
	 * removes schedules that registered automatically when service is removed.
	 */
	@Override
	public void removedService(ServiceReference reference, Object service) {
		String instanceName = (String) reference.getProperty("instance.name");
		if (instanceName != null) {
			List<Integer> schedules = scheduleMap.get(instanceName);
			if (schedules != null) {
				for (Integer id : schedules) {
					logger.trace("cron: removing schedule {}", id);
					cronService.unregisterSchedule(id);
				}
			}
		}

		super.removedService(reference, service);
	}
}
