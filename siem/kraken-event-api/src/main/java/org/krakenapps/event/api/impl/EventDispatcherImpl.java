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
package org.krakenapps.event.api.impl;

import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.event.api.Event;
import org.krakenapps.event.api.EventDispatcher;
import org.krakenapps.event.api.EventKey;
import org.krakenapps.event.api.EventPipe;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "event-dispatcher")
@Provides
public class EventDispatcherImpl implements EventDispatcher {
	private final Logger logger = LoggerFactory.getLogger(EventDispatcherImpl.class.getName());
	private CopyOnWriteArraySet<EventPipe> pipes;

	@Requires
	private PreferencesService prefsvc;

	private AtomicInteger next;
	private volatile int reserved;

	@Validate
	public void start() {
		pipes = new CopyOnWriteArraySet<EventPipe>();
		next = new AtomicInteger(prefsvc.getSystemPreferences().getInt("next", 1));
		try {
			reserve(10);
		} catch (IllegalStateException e) {
			logger.error("kraken event api: cannot save event id", e);
		}
	}

	@Invalidate
	public void stop() {
		try {
			reserve(10);
		} catch (IllegalStateException e) {
			logger.error("kraken event api: cannot save event id", e);
		}
	}

	private void reserve(int count) throws IllegalStateException {
		try {
			reserved = next.get() + count;

			if (prefsvc != null) {
				Preferences p = prefsvc.getSystemPreferences();
				p.putInt("next", reserved);
				p.flush();
				p.sync();
			}
		} catch (BackingStoreException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void dispatch(Event event) {
		if (logger.isTraceEnabled())
			logger.trace("kraken event api: dispatching event [{}]", event);

		if (event.getKey() == null) {
			int id = next.getAndIncrement();
			event.setKey(new EventKey(0, id));

			// save reserved id range periodically
			if (id > reserved)
				reserve(100);
		}

		for (EventPipe pipe : pipes) {
			try {
				pipe.onEvent(event);
			} catch (Exception e) {
				logger.warn("kraken event api: event pipe should not throw any exception", e);
			}
		}
	}

	@Override
	public void ack(Event event) {
		if (logger.isTraceEnabled())
			logger.trace("kraken event api: dispatching event ack [{}]", event);

		for (EventPipe pipe : pipes) {
			try {
				pipe.onEventAcked(event);
			} catch (Exception e) {
				logger.warn("kraken event api: event pipe should not throw any exception", e);
			}
		}
	}

	@Override
	public void addEventPipe(EventPipe pipe) {
		logger.trace("kraken event api: adding event pipe [{}]", pipe);

		pipes.add(pipe);
	}

	@Override
	public void removeEventPipe(EventPipe pipe) {
		logger.trace("kraken event api: removing event pipe [{}]", pipe);

		pipes.remove(pipe);
	}
}
