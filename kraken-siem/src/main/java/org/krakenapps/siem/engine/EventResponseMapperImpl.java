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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.event.api.Event;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.jpa.handler.Transactional;
import org.krakenapps.siem.model.EventResponseMapping;
import org.krakenapps.siem.model.ResponseActionInstance;
import org.krakenapps.siem.response.ResponseAction;
import org.krakenapps.siem.response.ResponseActionManager;
import org.krakenapps.siem.response.ResponseActionManagerEventListener;
import org.krakenapps.siem.response.ResponseServer;
import org.krakenapps.siem.response.ResponseServerEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "siem-response-mapper")
@JpaConfig(factory = "siem")
@Provides
public class EventResponseMapperImpl implements EventResponseMapper, ResponseServerEventListener,
		ResponseActionManagerEventListener {
	private final Logger logger = LoggerFactory.getLogger(EventResponseMapperImpl.class.getName());
	private ConcurrentMap<ResponseKey, CopyOnWriteArraySet<ResponseAction>> actionMap;

	@Requires
	private ThreadLocalEntityManagerService entityManagerService;

	@Requires
	private ResponseServer responseServer;

	@Validate
	public void start() {
		responseServer.addEventListener(this);
		for (ResponseActionManager manager : responseServer.getResponseActionManagers()) {
			managerAdded(manager);
		}

		actionMap = new ConcurrentHashMap<ResponseKey, CopyOnWriteArraySet<ResponseAction>>();
		loadActions();

		responseServer.setResponseMapper(this);
	}

	@Invalidate
	public void stop() {
		if (responseServer != null) {
			responseServer.removeEventListener(this);
			responseServer.setResponseMapper(null);
		}
	}

	@SuppressWarnings("unchecked")
	@Transactional
	private void loadActions() {
		EntityManager em = entityManagerService.getEntityManager();
		List<EventResponseMapping> mappings = em.createQuery("FROM EventResponseMapping m").getResultList();
		for (EventResponseMapping mapping : mappings) {
			for (ResponseActionInstance instance : mapping.getResponses()) {
				ResponseActionManager manager = responseServer.getResponseActionManager(instance.getManager());
				if (manager == null) {
					logger.trace("kraken siem: manager [{}] not found, preloading deferred", instance.getManager());
					continue;
				}

				ResponseAction action = manager.getAction(instance.getNamespace(), instance.getName());
				if (action == null) {
					logger.trace("kraken siem: action [{}] not found, preloading deferred", instance.getNamespace()
							+ "\\" + instance.getName());
					continue;
				}

				addResponseTemp(new ResponseKey(mapping.getCategory(), mapping.getEventSource()), action);
				logger.trace("kraken siem: event [category={}, source={}] to response [{}] mapping loaded",
						new Object[] { mapping.getCategory(), mapping.getEventSource(), action.getNamespace() + "\\"
						+ action.getName() });
			}
		}
	}

	@Override
	public Collection<ResponseAction> getActions(ResponseKey key) {
		CopyOnWriteArraySet<ResponseAction> c = actionMap.get(key);
		if (c == null)
			return null;

		return Collections.unmodifiableCollection(c);
	}

	@Override
	public Collection<ResponseKey> getKeys() {
		return Collections.unmodifiableCollection(actionMap.keySet());
	}

	@Override
	public Collection<ResponseAction> getActions(Event e) {
		return actionMap.get(new ResponseKey(e.getCategory(), e.getKey().getSource()));
	}

	@Override
	public void addResponse(ResponseKey key, ResponseAction action) {
		addResponseTemp(key, action);
		addResponseConfig(key, action);
		logger.trace("kraken siem: added response mapping, key [{}], action [{}]", key, action.toString());
	}

	@Override
	public void removeResponse(ResponseKey key, ResponseAction action) {
		removeResponseTemp(key, action);
		removeResponseConfig(key, action);

		logger.trace("kraken siem: removed response mapping, key [{}], action [{}]", key, action.toString());
	}

	private void addResponseTemp(ResponseKey key, ResponseAction action) {
		CopyOnWriteArraySet<ResponseAction> actions = new CopyOnWriteArraySet<ResponseAction>();
		CopyOnWriteArraySet<ResponseAction> old = actionMap.putIfAbsent(key, actions);
		if (old != null)
			actions = old;

		actions.add(action);
	}

	@Transactional
	private void addResponseConfig(ResponseKey key, ResponseAction action) {
		EntityManager em = entityManagerService.getEntityManager();

		ResponseActionInstance instance = getResponseActionInstance(action, em);
		EventResponseMapping mapping = getEventResponseMapping(key, em);

		if (mapping == null) {
			mapping = new EventResponseMapping();
			mapping.setCategory(key.getCategory());
			mapping.setEventSource(key.getEventSource());

			em.persist(mapping);
		}

		instance.getEventMappings().add(mapping);
		mapping.getResponses().add(instance);

		em.merge(instance);
	}

	private void removeResponseTemp(ResponseKey key, ResponseAction action) {
		CopyOnWriteArraySet<ResponseAction> actions = actionMap.get(key);
		if (actions == null)
			return;

		actions.remove(action);
	}

	@Transactional
	private void removeResponseConfig(ResponseKey key, ResponseAction action) {
		EntityManager em = entityManagerService.getEntityManager();

		ResponseActionInstance instance = getResponseActionInstance(action, em);
		EventResponseMapping mapping = getEventResponseMapping(key, em);

		instance.getEventMappings().remove(mapping);
		mapping.getResponses().remove(instance);

		em.merge(instance);
		em.merge(mapping);

		if (mapping.getResponses().size() == 0)
			em.remove(mapping);
	}

	private ResponseActionInstance getResponseActionInstance(ResponseAction action, EntityManager em) {
		ResponseActionInstance instance = null;
		try {
			instance = (ResponseActionInstance) em.createQuery(
					"FROM ResponseActionInstance r WHERE r.manager = ? AND r.namespace = ? AND r.name = ?").setParameter(
					1, action.getManager().getName()).setParameter(2, action.getNamespace()).setParameter(3,
					action.getName()).getSingleResult();
		} catch (NoResultException e) {
			throw new IllegalStateException("response instance configuration not found: " + action.getNamespace()
					+ "\\" + action.getName());
		}
		return instance;
	}

	private EventResponseMapping getEventResponseMapping(ResponseKey key, EntityManager em) {
		EventResponseMapping mapping = null;
		try {
			mapping = (EventResponseMapping) em.createQuery(
					"FROM EventResponseMapping m WHERE m.category = ? AND m.eventSource = ?").setParameter(
					1, key.getCategory()).setParameter(2, key.getEventSource()).getSingleResult();
		} catch (NoResultException e) {
		}
		return mapping;
	}

	@Override
	public void managerAdded(ResponseActionManager manager) {
		if (manager == null)
			return;

		manager.addEventListener(this);
		for (ResponseAction action : manager.getActions()) {
			actionCreated(manager, action);
		}
	}

	@Override
	public void managerRemoved(ResponseActionManager manager) {
		if (manager == null)
			return;

		manager.removeEventListener(this);
	}

	@Transactional
	@Override
	public void actionCreated(ResponseActionManager manager, ResponseAction action) {
		try {
			EntityManager em = entityManagerService.getEntityManager();
			ResponseActionInstance instance = getResponseActionInstance(action, em);

			for (EventResponseMapping mapping : instance.getEventMappings()) {
				addResponseTemp(new ResponseKey(mapping.getCategory(), mapping.getEventSource()), action);
			}
		} catch (Exception e) {
			// ignore if not exists
		}
	}

	@Override
	public void actionRemoved(ResponseActionManager manager, ResponseAction action) {
		try {
			EntityManager em = entityManagerService.getEntityManager();
			ResponseActionInstance instance = getResponseActionInstance(action, em);

			for (EventResponseMapping mapping : instance.getEventMappings()) {
				removeResponseTemp(new ResponseKey(mapping.getCategory(), mapping.getEventSource()), action);
			}
		} catch (Exception e) {
			// ignore if not exists
		}
	}
}
