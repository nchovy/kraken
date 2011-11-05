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

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigIterator;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.event.api.Event;
import org.krakenapps.siem.ConfigManager;
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
@Provides
public class EventResponseMapperImpl implements EventResponseMapper, ResponseServerEventListener,
		ResponseActionManagerEventListener {
	private final Logger logger = LoggerFactory.getLogger(EventResponseMapperImpl.class.getName());
	private ConcurrentMap<ResponseKey, CopyOnWriteArraySet<ResponseAction>> actionMap;

	@Requires
	private ResponseServer responseServer;

	@Requires
	private ConfigManager configManager;

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

	private void loadActions() {
		ConfigCollection col = getCol();

		ConfigIterator it = col.findAll();
		while (it.hasNext()) {
			Config c = it.next();
			ResponseActionInstance instance = PrimitiveConverter.parse(ResponseActionInstance.class, c.getDocument());

			for (EventResponseMapping mapping : instance.getEventMappings()) {
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

				loadResponse(new ResponseKey(mapping.getCategory(), mapping.getEventSource()), action);
				logger.trace("kraken siem: event [category={}, source={}] to response [{}] mapping loaded",
						new Object[] { mapping.getCategory(), mapping.getEventSource(),
								action.getNamespace() + "\\" + action.getName() });
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
		loadResponse(key, action);
		addResponseConfig(key, action);
		logger.trace("kraken siem: added response mapping, key [{}], action [{}]", key, action.toString());
	}

	@Override
	public void removeResponse(ResponseKey key, ResponseAction action) {
		unloadResponse(key, action);
		removeResponseConfig(key, action);

		logger.trace("kraken siem: removed response mapping, key [{}], action [{}]", key, action.toString());
	}

	private void loadResponse(ResponseKey key, ResponseAction action) {
		CopyOnWriteArraySet<ResponseAction> actions = new CopyOnWriteArraySet<ResponseAction>();
		CopyOnWriteArraySet<ResponseAction> old = actionMap.putIfAbsent(key, actions);
		if (old != null)
			actions = old;

		actions.add(action);
	}

	/**
	 * add response mapping to specific event type
	 * 
	 * @param key
	 *            the event type
	 * @param action
	 *            the response action
	 */
	private void addResponseConfig(ResponseKey key, ResponseAction action) {
		ConfigCollection col = getCol();
		Config c = getResponseActionInstance(action);
		ResponseActionInstance instance = PrimitiveConverter.parse(ResponseActionInstance.class, c.getDocument());
		EventResponseMapping mapping = new EventResponseMapping(key.getCategory(), key.getEventSource());

		List<EventResponseMapping> mappings = instance.getEventMappings();
		if (!mappings.contains(mapping))
			mappings.add(mapping);

		c.setDocument(PrimitiveConverter.serialize(instance));
		col.update(c);
	}

	private void unloadResponse(ResponseKey key, ResponseAction action) {
		CopyOnWriteArraySet<ResponseAction> actions = actionMap.get(key);
		if (actions == null)
			return;

		actions.remove(action);
	}

	private void removeResponseConfig(ResponseKey key, ResponseAction action) {
		ConfigCollection col = getCol();
		Config c = getResponseActionInstance(action);
		ResponseActionInstance instance = PrimitiveConverter.parse(ResponseActionInstance.class, c);
		EventResponseMapping mapping = new EventResponseMapping(key.getCategory(), key.getEventSource());

		instance.getEventMappings().remove(mapping);
		if (instance.getEventMappings().size() == 0)
			col.remove(c);
		else {
			c.setDocument(PrimitiveConverter.serialize(instance));
			col.update(c);
		}
	}

	private Config getResponseActionInstance(ResponseAction action) {
		ConfigCollection col = getCol();
		Config c = col.findOne(Predicates.and( //
				Predicates.field("manager", action.getManager().getName()), //
				Predicates.field("namespace", action.getNamespace()), //
				Predicates.field("name", action.getName()))); //

		if (c == null)
			throw new IllegalStateException("response instance configuration not found: " + action.getNamespace()
					+ "\\" + action.getName());

		return c;
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

	@Override
	public void actionCreated(ResponseActionManager manager, ResponseAction action) {
		try {
			Config c = getResponseActionInstance(action);
			ResponseActionInstance instance = PrimitiveConverter.parse(ResponseActionInstance.class, c.getDocument());

			for (EventResponseMapping mapping : instance.getEventMappings()) {
				loadResponse(new ResponseKey(mapping.getCategory(), mapping.getEventSource()), action);
			}
		} catch (Exception e) {
			// ignore if not exists
		}
	}

	@Override
	public void actionRemoved(ResponseActionManager manager, ResponseAction action) {
		try {
			Config c = getResponseActionInstance(action);
			ResponseActionInstance instance = PrimitiveConverter.parse(ResponseActionInstance.class, c.getDocument());

			for (EventResponseMapping mapping : instance.getEventMappings()) {
				unloadResponse(new ResponseKey(mapping.getCategory(), mapping.getEventSource()), action);
			}
		} catch (Exception e) {
			// ignore if not exists
		}
	}

	private ConfigCollection getCol() {
		ConfigDatabase db = configManager.getDatabase();
		return db.ensureCollection("response_action_instance");
	}
}
