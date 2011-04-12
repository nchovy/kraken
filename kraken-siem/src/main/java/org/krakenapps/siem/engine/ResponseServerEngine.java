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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.event.api.Event;
import org.krakenapps.event.api.EventDispatcher;
import org.krakenapps.event.api.EventPipe;
import org.krakenapps.siem.response.ResponseAction;
import org.krakenapps.siem.response.ResponseActionManager;
import org.krakenapps.siem.response.ResponseMapper;
import org.krakenapps.siem.response.ResponseServer;
import org.krakenapps.siem.response.ResponseServerEventListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "siem-response-server")
@Provides
public class ResponseServerEngine extends ServiceTracker implements ResponseServer, EventPipe {
	private final Logger logger = LoggerFactory.getLogger(ResponseServerEngine.class.getName());

	@Requires
	private EventDispatcher eventDispatcher;

	private BundleContext bc;
	private ResponseMapper mapper;

	private ConcurrentMap<String, ResponseActionManager> managerMap;
	private CopyOnWriteArraySet<ResponseServerEventListener> callbacks;

	public ResponseServerEngine(BundleContext bc) {
		super(bc, ResponseActionManager.class.getName(), null);
		this.bc = bc;
		this.callbacks = new CopyOnWriteArraySet<ResponseServerEventListener>();
	}

	@Validate
	public void start() {
		managerMap = new ConcurrentHashMap<String, ResponseActionManager>();
		eventDispatcher.addEventPipe(this);
		super.open();

		loadManagers();
	}

	private void loadManagers() {
		try {
			ServiceReference[] refs = bc.getServiceReferences(ResponseActionManager.class.getName(), null);
			if (refs == null)
				return;

			for (ServiceReference ref : refs) {
				ResponseActionManager manager = (ResponseActionManager) bc.getService(ref);
				managerMap.put(manager.getName(), manager);

				for (ResponseServerEventListener callback : callbacks) {
					try {
						callback.managerAdded(manager);
					} catch (Exception e) {
						logger.error("kraken siem: response server event callback should not throw any exception", e);
					}
				}
			}
		} catch (InvalidSyntaxException e) {
			logger.error("kraken siem: cannot load response action managers", e);
		}
	}

	@Invalidate
	public void stop() {
		super.close();

		if (eventDispatcher != null)
			eventDispatcher.removeEventPipe(this);
	}

	@Override
	public void setResponseMapper(ResponseMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public Collection<ResponseActionManager> getResponseActionManagers() {
		return Collections.unmodifiableCollection(managerMap.values());
	}

	@Override
	public ResponseActionManager getResponseActionManager(String name) {
		return managerMap.get(name);
	}

	@Override
	public Object addingService(ServiceReference reference) {
		ResponseActionManager manager = (ResponseActionManager) super.addingService(reference);
		managerMap.put(manager.getName(), manager);

		logger.trace("kraken siem: new response action manager [{}]", manager.getName());

		for (ResponseServerEventListener callback : callbacks) {
			try {
				callback.managerAdded(manager);
			} catch (Exception e) {
				logger.error("kraken siem: response server event callback should not throw any exception", e);
			}
		}

		return manager;
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {
		ResponseActionManager manager = (ResponseActionManager) service;
		managerMap.remove(manager.getName());

		logger.trace("kraken siem: response action manager [{}] removed", manager.getName());

		for (ResponseServerEventListener callback : callbacks) {
			try {
				callback.managerRemoved(manager);
			} catch (Exception e) {
				logger.error("kraken siem: response server event callback should not throw any exception", e);
			}
		}

		super.removedService(reference, service);
	}

	@Override
	public void onEvent(Event event) {
		if (mapper == null) {
			logger.trace("kraken siem: response mapper not found");
			return;
		}

		Collection<ResponseAction> actions = mapper.getActions(event);
		if (actions == null)
			return;

		for (ResponseAction action : actions) {
			try {
				action.handle(event);
			} catch (Exception e) {
				logger.warn("kraken siem: response action should not throw any exception", e);
			}
		}
	}

	@Override
	public void onEventAcked(Event event) {
	}

	@Override
	public void addEventListener(ResponseServerEventListener callback) {
		callbacks.add(callback);
	}

	@Override
	public void removeEventListener(ResponseServerEventListener callback) {
		callbacks.remove(callback);
	}
}
