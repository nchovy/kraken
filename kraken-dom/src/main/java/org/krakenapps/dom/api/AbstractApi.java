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

public class AbstractApi<T> implements EntityEventProvider<T> {
	private final Logger logger = LoggerFactory.getLogger(AbstractApi.class);
	private WeakHashMap<EntityEventListener<T>, Integer> listeners;

	public AbstractApi() {
		listeners = new WeakHashMap<EntityEventListener<T>, Integer>();
	}

	public void fireEntityAdded(T t) {
		for (EntityEventListener<T> listener : listeners.keySet()) {
			if (listener != null) {
				try {
					listener.entityAdded(t);
				} catch (Exception e) {
					logger.warn("abstract entity api: entity event callback should not throw any exception", e);
				}
			}
		}
	}

	public void fireEntityUpdated(T t) {
		for (EntityEventListener<T> listener : listeners.keySet()) {
			if (listener != null) {
				try {
					listener.entityUpdated(t);
				} catch (Exception e) {
					logger.warn("abstract entity api: entity event callback should not throw any exception", e);
				}
			}
		}
	}

	public void fireEntityRemoving(T t) {
		for (EntityEventListener<T> listener : listeners.keySet()) {
			if (listener != null) {
				try {
					listener.entityRemoving(t);
				} catch (Exception e) {
					logger.warn("abstract entity api: entity event callback should not throw any exception", e);
				}
			}
		}
	}

	public void fireEntityRemoved(T t) {
		for (EntityEventListener<T> listener : listeners.keySet()) {
			if (listener != null) {
				try {
					listener.entityRemoved(t);
				} catch (Exception e) {
					logger.warn("abstract entity api: entity event callback should not throw any exception", e);
				}
			}
		}
	}

	@Override
	public void addEntityEventListener(EntityEventListener<T> listener) {
		if (listener == null) {
			logger.warn("abstract entity api: listener is null at subscribe()");
			return;
		}

		listeners.put(listener, null);
	}

	@Override
	public void removeEntityEventListener(EntityEventListener<T> listener) {
		if (listener == null) {
			logger.warn("abstract entity api: listener is null at unsubscribe()");
			return;
		}

		listeners.remove(listener);
	}
}
