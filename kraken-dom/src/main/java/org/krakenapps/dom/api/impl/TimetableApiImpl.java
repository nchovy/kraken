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
package org.krakenapps.dom.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.dom.api.AbstractApi;
import org.krakenapps.dom.api.OrganizationApi;
import org.krakenapps.dom.api.TimetableApi;
import org.krakenapps.dom.model.Organization;
import org.krakenapps.dom.model.Schedule;
import org.krakenapps.dom.model.Timetable;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.jpa.handler.Transactional;

@Component(name = "dom-timetable-api")
@Provides
@JpaConfig(factory = "dom")
public class TimetableApiImpl extends AbstractApi<Timetable> implements TimetableApi {
	@Requires
	private OrganizationApi orgApi;

	@Requires
	private ThreadLocalEntityManagerService entityManagerService;

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Collection<Timetable> getTimetables(int organizationId) {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("FROM Timetable t WHERE t.organization.id = ?").setParameter(1, organizationId)
				.getResultList();
	}

	@Transactional
	@Override
	public Timetable getTimetable(int organizationId, int id) {
		EntityManager em = entityManagerService.getEntityManager();
		return get(em, organizationId, id);
	}

	@Override
	public Timetable createTimetable(int organizationId, String name, List<Schedule> schedules) {
		Timetable t = createTimetableInternal(organizationId, name, schedules);
		fireEntityAdded(t);
		return t;
	}

	@Transactional
	private Timetable createTimetableInternal(int organizationId, String name, List<Schedule> schedules) {
		EntityManager em = entityManagerService.getEntityManager();
		Long count = (Long) em
				.createQuery("SELECT COUNT(*) FROM Timetable t WHERE t.organization.id = ? AND t.name = ?")
				.setParameter(1, organizationId).setParameter(2, name).getSingleResult();

		if (count > 0)
			throw new IllegalStateException("duplicated timetable name: " + organizationId + ", " + name);

		Organization org = orgApi.getOrganization(organizationId);
		if (org == null)
			throw new IllegalStateException("organization not found: " + organizationId);

		Timetable t = new Timetable();
		t.setName(name);
		t.setOrganization(org);
		t.setCreateDateTime(new Date());
		t.setUpdateDateTime(new Date());
		em.persist(t);

		for (Schedule s : schedules) {
			s.setTimetable(t);
			t.getSchedules().add(s);
		}

		em.merge(t);

		return t;
	}

	@Override
	public Timetable updateTimetable(int organizationId, int id, String name, List<Schedule> schedules) {
		Timetable t = updateTimetableInternal(organizationId, id, name, schedules);
		fireEntityUpdated(t);
		return t;
	}

	@Transactional
	private Timetable updateTimetableInternal(int organizationId, int id, String name, List<Schedule> schedules) {
		EntityManager em = entityManagerService.getEntityManager();
		Long count = (Long) em
				.createQuery(
						"SELECT COUNT(*) FROM Timetable t WHERE t.organization.id = ? AND t.name = ? AND t.id <> ?")
				.setParameter(1, organizationId).setParameter(2, name).setParameter(3, id).getSingleResult();

		if (count > 0)
			throw new IllegalStateException("duplicated timetable name: " + organizationId + ", " + name);

		Timetable t = get(em, organizationId, id);
		t.setName(name);

		for (Schedule s : t.getSchedules()) {
			em.remove(s);
		}

		t.setSchedules(new ArrayList<Schedule>());
		t.setUpdateDateTime(new Date());
		em.merge(t);

		for (Schedule s : schedules) {
			t.getSchedules().add(s);
			s.setTimetable(t);
			em.persist(s);
		}

		em.merge(t);
		return t;
	}

	@Override
	public Timetable removeTimetable(int organizationId, int id) {
		Timetable t = removeTimetableInternal(organizationId, id);
		fireEntityRemoved(t);
		return t;
	}

	@Transactional
	private Timetable removeTimetableInternal(int organizationId, int id) {
		EntityManager em = entityManagerService.getEntityManager();
		Timetable t = get(em, organizationId, id);
		em.remove(t);
		return t;
	}

	private Timetable get(EntityManager em, int organizationId, int id) {
		Timetable t = null;
		try {
			t = (Timetable) em.createQuery("FROM Timetable t WHERE t.organization.id = ? AND t.id = ?")
					.setParameter(1, organizationId).setParameter(2, id).getSingleResult();
		} catch (NoResultException e) {
			throw new IllegalStateException("timetable not found: " + organizationId + ", " + id);
		}
		return t;
	}
}
