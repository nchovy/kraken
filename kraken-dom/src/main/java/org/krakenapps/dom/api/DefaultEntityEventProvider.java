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
package org.krakenapps.dom.api;

import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultEntityEventProvider<T> implements EntityEventProvider<T> {
	private final Logger logger = LoggerFactory.getLogger(DefaultEntityEventProvider.class);
	private WeakHashMap<EntityEventListener<T>, Integer> listeners = new WeakHashMap<EntityEventListener<T>, Integer>();

	@Override
	public void addEntityEventListener(EntityEventListener<T> listener) {
		if (listener == null) {
			logger.warn("kraken dom: listener is null at subscribe()");
			return;
		}
		listeners.put(listener, null);
	}

	@Override
	public void removeEntityEventListener(EntityEventListener<T> listener) {
		if (listener == null) {
			logger.warn("kraken dom: listener is null at unsubscribe()");
			return;
		}
		listeners.remove(listener);
	}

	public void fireEntityAdded(String domain, T t) {
		for (EntityEventListener<T> listener : listeners.keySet()) {
			if (listener != null) {
				try {
					listener.entityAdded(domain, t);
				} catch (Exception e) {
					logger.warn("kraken dom: entity event callback should not throw any exception", e);
				}
			}
		}
	}

	public void fireEntityUpdated(String domain, T t) {
		for (EntityEventListener<T> listener : listeners.keySet()) {
			if (listener != null) {
				try {
					listener.entityUpdated(domain, t);
				} catch (Exception e) {
					logger.warn("kraken dom: entity event callback should not throw any exception", e);
				}
			}
		}
	}

	public void fireEntityRemoving(String domain, T t) {
		// do NOT add try-catch. it's intentional exception throwing
		for (EntityEventListener<T> listener : listeners.keySet()) {
			if (listener != null) {
				listener.entityRemoving(domain, t);
			}
		}
	}

	public void fireEntityRemoved(String domain, T t) {
		for (EntityEventListener<T> listener : listeners.keySet()) {
			if (listener != null) {
				try {
					listener.entityRemoved(domain, t);
				} catch (Exception e) {
					logger.warn("kraken dom: entity event callback should not throw any exception", e);
				}
			}
		}
	}
}
