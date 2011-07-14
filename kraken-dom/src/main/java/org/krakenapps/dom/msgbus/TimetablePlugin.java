/*
 * Copyright 2011 Future Systems, Inc.
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
package org.krakenapps.dom.msgbus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.dom.api.TimetableApi;
import org.krakenapps.dom.model.Schedule;
import org.krakenapps.dom.model.Timetable;
import org.krakenapps.msgbus.Marshaler;
import org.krakenapps.msgbus.MsgbusException;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "dom-timetable-plugin")
@MsgbusPlugin
public class TimetablePlugin {
	@Requires
	private TimetableApi timetableApi;

	@MsgbusMethod
	public void getTimetables(Request req, Response resp) {
		int organizationId = req.getSession().getOrgId();
		Collection<Timetable> timetables = timetableApi.getTimetables(organizationId);
		resp.put("timetables", Marshaler.marshal(timetables));
	}

	@MsgbusMethod
	public void getTimetable(Request req, Response resp) {
		int organizationId = req.getSession().getOrgId();
		int id = req.getInteger("id");

		Timetable timetable = timetableApi.getTimetable(organizationId, id);
		Map<String, Object> m = timetable.marshal();
		m.put("schedules", Marshaler.marshal(timetable.getSchedules()));
		resp.put("schedules", m);
	}

	@SuppressWarnings("unchecked")
	@MsgbusMethod
	public void createTimetable(Request req, Response resp) {
		try {
			int organizationId = req.getSession().getOrgId();
			String name = req.getString("name");
			List<Schedule> schedules = parseSchedules((Collection<Object>) req.get("schedules"));
			Timetable newTime = timetableApi.createTimetable(organizationId, name, schedules);
			resp.put("id", newTime.getId());
		} catch (IllegalStateException e) {
			if (e.getMessage().startsWith("duplicated"))
				throw new MsgbusException("dom", "duplicated-timetable-name");
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	@MsgbusMethod
	public void updateTimetable(Request req, Response resp) {
		try {
			int organizationId = req.getSession().getOrgId();
			int id = req.getInteger("id");
			String name = req.getString("name");
			List<Schedule> schedules = parseSchedules((Collection<Object>) req.get("schedules"));
			timetableApi.updateTimetable(organizationId, id, name, schedules);
		} catch (IllegalStateException e) {
			if (e.getMessage().startsWith("duplicated"))
				throw new MsgbusException("dom", "duplicated-timetable-name");
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	private List<Schedule> parseSchedules(Collection<Object> schedules) {
		List<Schedule> l = new ArrayList<Schedule>();

		for (Object o : schedules) {
			Map<String, Object> schedule = (Map<String, Object>) o;
			Schedule s = new Schedule();
			s.setDayOfWeek((Integer) schedule.get("day"));
			s.setBeginSecond((Integer) schedule.get("begin"));
			s.setEndSecond((Integer) schedule.get("end"));

			if (s.getBeginSecond() > s.getEndSecond())
				throw new IllegalArgumentException();
			
			l.add(s);
		}

		return l;
	}

	@MsgbusMethod
	public void removeTimetable(Request req, Response resp) {
		int organizationId = req.getSession().getOrgId();
		int id = req.getInteger("id");

		timetableApi.removeTimetable(organizationId, id);
	}
}
