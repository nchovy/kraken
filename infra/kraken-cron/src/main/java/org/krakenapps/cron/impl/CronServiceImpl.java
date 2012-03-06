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
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.cron.CronService;
import org.krakenapps.cron.DuplicatedScheduleException;
import org.krakenapps.cron.Schedule;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * This class provides implementation for the {@link CronService} interface.
 * 
 * @author periphery
 * @since 1.0.0
 */
@Component(name = "cron-service")
@Provides
public class CronServiceImpl implements CronService {
	private static BundleContext bundleContext;
	/**
	 * schedule id to Schedule mapping.
	 */
	private ConcurrentMap<Integer, Schedule> map;

	@Requires
	private ConfigService conf;
	private final CronConfig config;
	private final Scheduler scheduler = new Scheduler();
	private JobServiceTracker tracker;

	public CronServiceImpl(BundleContext context) throws ParseException {
		tracker = new JobServiceTracker(context, this);
		bundleContext = context;
		this.config = new CronConfig(conf);
		refreshMap();
	}

	public CronServiceImpl(BundleContext context, ConfigService conf) throws ParseException {
		tracker = new JobServiceTracker(context, this);
		bundleContext = context;
		this.config = new CronConfig(conf);
		refreshMap();
		validate();
	}

	@Validate
	public void validate() {
		scheduler.start(getMap());
		tracker.open();
	}

	@Invalidate
	public void invalidate() {
		tracker.close();
		scheduler.stop();
	}

	/**
	 * register schedule. schedule is saved in db, and added to scheduler.
	 */
	@Override
	public int registerSchedule(Schedule schedule) {
		// check duplicated schedule
		for (Schedule e : map.values())
			if (e.equals(schedule))
				throw new DuplicatedScheduleException(schedule);

		int id = config.addEntry(schedule);
		this.map.put(id, schedule);
		scheduler.put(id, schedule);
		return id;
	}

	/**
	 * unregister schedule. schedule is removed from db, and from scheduler.
	 */
	@Override
	public void unregisterSchedule(int i) {
		if (null == this.map.remove(i))
			throw new NoSuchElementException();
		config.removeEntry(i);
		scheduler.remove(i);
	}

	/**
	 * load schedules from db.
	 * 
	 * @throws ParseException
	 *             when data in db is corrupted and unable to parse as schedule.
	 */
	private void refreshMap() throws ParseException {
		this.map = new ConcurrentHashMap<Integer, Schedule>(config.getEntries());
	}

	private Map<Integer, Schedule> getMap() {
		return new HashMap<Integer, Schedule>(this.map);
	}

	@Override
	public Map<Integer, Schedule> getSchedules() {
		// it is safe to return because schedule is immutable object
		Map<Integer, Schedule> schedules = new TreeMap<Integer, Schedule>();
		for (Integer key : map.keySet()) {
			Schedule schedule = map.get(key);
			schedules.put(key, schedule);
		}

		return schedules;
	}

	@Override
	public List<String> getJobList() {
		return scheduler.getJobList();
	}

	public static Runnable getRef(String Name) throws InvalidSyntaxException {
		return getRef(bundleContext, Name);
	}

	/**
	 * get reference of current active Runnable given its instance name.
	 * 
	 * @param context
	 * @param instanceName
	 *            instance name of the Runnable
	 * @return
	 * @throws InvalidSyntaxException
	 *             when given instance name is not a valid string.
	 */
	private static Runnable getRef(BundleContext context, String instanceName) throws InvalidSyntaxException {
		ServiceReference[] refs = context.getServiceReferences(Runnable.class.getName(), "(instance.name=" + instanceName + ")");
		if (refs == null || refs.length == 0) {
			throw new NullPointerException();
		}

		Runnable task = ((Runnable) context.getService(refs[0]));
		return task;
	}
}
