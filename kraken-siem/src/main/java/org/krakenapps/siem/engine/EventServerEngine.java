/*
 * Copyright 2011 NCHOVY
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
package org.krakenapps.siem.engine;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.event.api.Event;
import org.krakenapps.event.api.EventDispatcher;
import org.krakenapps.event.api.EventPipe;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.jpa.handler.Transactional;
import org.krakenapps.siem.EventServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "siem-event-server")
@JpaConfig(factory = "siem")
@Provides
public class EventServerEngine implements EventServer, EventPipe {
	private final Logger logger = LoggerFactory.getLogger(EventServerEngine.class.getName());

	@Requires
	private ThreadLocalEntityManagerService entityManagerService;

	@Requires
	private EventDispatcher eventDispatcher;

	@Validate
	public void start() {
		eventDispatcher.addEventPipe(this);
	}

	@Invalidate
	public void stop() {
		if (eventDispatcher != null)
			eventDispatcher.removeEventPipe(this);
	}

	//
	// EventPipe implementation
	//

	@Transactional
	@Override
	public void onEvent(Event event) {
		EntityManager em = entityManagerService.getEntityManager();
		em.persist(event);

		logger.trace("kraken siem: new event [{}]", event.toString());
	}

	@Override
	public void onEventAcked(Event event) {
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public List<Event> getEvents(int offset, int limit) {
		EntityManager em = entityManagerService.getEntityManager();
		List<Event> events = em.createQuery("FROM Event e ORDER BY e.lastSeen DESC").setFirstResult(offset).setMaxResults(limit).getResultList();
		return events;
	}

	@Override
	@Transactional
	public Long getEventsCount() {
		EntityManager em = entityManagerService.getEntityManager();
		return (Long) em.createQuery("SELECT COUNT(*) FROM Event").getSingleResult();
	}

}
