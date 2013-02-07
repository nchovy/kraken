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
package org.krakenapps.cron.msgbus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.cron.CronService;
import org.krakenapps.cron.Schedule;
import org.krakenapps.cron.impl.CronField.Type;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPermission;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "cron-plugin")
@MsgbusPlugin
public class CronPlugin {
	@Requires
	private CronService cron;

	public CronPlugin() {
	}

	public CronPlugin(CronService cron) {
		this.cron = cron;
	}

	@MsgbusMethod
	public void getSchedules(Request req, Response resp) {
		List<Object> l = new ArrayList<Object>();

		Map<Integer, Schedule> schedules = cron.getSchedules();
		for (int id : schedules.keySet()) {
			Schedule schedule = schedules.get(id);
			l.add(marshal(id, schedule));
		}

		resp.put("schedules", l);
	}

	@MsgbusMethod
	public void getJobs(Request req, Response resp) {
		resp.put("jobs", cron.getJobList());
	}

	@MsgbusMethod
	@MsgbusPermission(group = "cron", code = "manage")
	public void registerSchedule(Request req, Response resp) {
		String task = req.getString("task");
		String expr = req.getString("expr");

		try {
			Schedule schedule = new Schedule.Builder(task).build(expr);
			int id = cron.registerSchedule(schedule);
			resp.put("id", id);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	@MsgbusMethod
	@MsgbusPermission(group = "cron", code = "manage")
	public void unregisterSchedule(Request req, Response resp) {
		int id = req.getInteger("id");
		cron.unregisterSchedule(id);
	}

	private Map<String, Object> marshal(int id, Schedule schedule) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("id", id);
		m.put("month", schedule.get(Type.MONTH));
		m.put("day_of_week", schedule.get(Type.DAY_OF_WEEK));
		m.put("day_of_month", schedule.get(Type.DAY_OF_MONTH));
		m.put("hour", Type.HOUR);
		m.put("minute", Type.MINUTE);
		m.put("task", schedule.getTaskName());
		return m;
	}
}
