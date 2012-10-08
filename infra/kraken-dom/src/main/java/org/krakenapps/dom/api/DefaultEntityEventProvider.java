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

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

import org.krakenapps.confdb.ConfigTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultEntityEventProvider<T> implements EntityEventProvider<T> {
	private final Logger logger = LoggerFactory.getLogger(DefaultEntityEventProvider.class);
	private CopyOnWriteArraySet<EntityEventListener<T>> listeners = new CopyOnWriteArraySet<EntityEventListener<T>>();

	@Override
	public void addEntityEventListener(EntityEventListener<T> listener) {
		if (listener == null) {
			logger.warn("kraken dom: listener is null at subscribe()");
			return;
		}
		listeners.add(listener);
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
		fireEntityAdded(domain, t, null);
	}

	
	public void fireEntityAdded(String domain, T t, Object state) {
		for (EntityEventListener<T> listener : listeners) {
			if (listener != null) {
				try {
					listener.entityAdded(domain, t, state);
				} catch (Throwable e) {
					logger.warn("kraken dom: entity event callback should not throw any exception", e);
				}
			}
		}
	}

	public void fireEntityAdding(String domain, T t){
		fireEntityAdding(domain, t, null);
	}
	
	public void fireEntityAdding(String domain, T t, Object state){
		for (EntityEventListener<T> listener : listeners) {
			if (listener != null) {
				listener.entityAdding(domain, t, state);
			}
		}
	}
	
	
	public void fireEntitiesAdding(String domain, Collection<EntityState> entities) {
		for (EntityEventListener<T> listener : listeners) {
			if (listener != null) {
				listener.entitiesAdding(domain, entities);
			}
		}
	}	

	public void fireEntitiesAdded(String domain, Collection<EntityState> entities) {
		for (EntityEventListener<T> listener : listeners) {
			if (listener != null) {
				try {
					listener.entitiesAdded(domain, entities);
				} catch (Throwable e) {
					logger.warn("kraken dom: entity event callback should not throw any exception", e);
				}
			}
		}
	}

	public void fireEntityUpdated(String domain, T t) {
		fireEntityUpdated(domain, t, null);
	}

	public void fireEntityUpdated(String domain, T t, Object state) {
		for (EntityEventListener<T> listener : listeners) {
			if (listener != null) {
				try {
					listener.entityUpdated(domain, t, state);
				} catch (Throwable e) {
					logger.warn("kraken dom: entity event callback should not throw any exception", e);
				}
			}
		}
	}

	public void fireEntitiesUpdated(String domain, Collection<EntityState> entities) {
		for (EntityEventListener<T> listener : listeners) {
			if (listener != null) {
				try {
					listener.entitiesUpdated(domain, entities);
				} catch (Throwable e) {
					logger.warn("kraken dom: entity event callback should not throw any exception", e);
				}
			}
		}
	}

	public void fireEntityRemoving(String domain, T t) {
		fireEntityRemoving(domain, t, null, null);
	}

	public void fireEntityRemoving(String domain, T t, ConfigTransaction xact, Object state) {
		// DO NOT add try-catch. it's intentional exception throwing
		for (EntityEventListener<T> listener : listeners) {
			if (listener != null) {
				listener.entityRemoving(domain, t, xact, state);
			}
		}
	}

	public void fireEntitiesRemoving(String domain, Collection<EntityState> entities, ConfigTransaction xact) {
		// DO NOT add try-catch. it's intentional exception throwing
		for (EntityEventListener<T> listener : listeners) {
			if (listener != null) {
				listener.entitiesRemoving(domain, entities, xact);
			}
		}
	}

	public void fireEntityRemoved(String domain, T t) {
		fireEntityRemoved(domain, t, null);
	}

	public void fireEntityRemoved(String domain, T t, Object state) {
		for (EntityEventListener<T> listener : listeners) {
			if (listener != null) {
				try {
					listener.entityRemoved(domain, t, state);
				} catch (Throwable e) {
					logger.warn("kraken dom: entity event callback should not throw any exception", e);
				}
			}
		}
	}

	public void fireEntitiesRemoved(String domain, Collection<EntityState> entities) {
		for (EntityEventListener<T> listener : listeners) {
			if (listener != null) {
				try {
					listener.entitiesRemoved(domain, entities);
				} catch (Throwable e) {
					logger.warn("kraken dom: entity event callback should not throw any exception", e);
				}
			}
		}
	}
}
