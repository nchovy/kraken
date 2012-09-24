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

import org.krakenapps.confdb.ConfigTransaction;

public interface EntityEventListener<T> {
	void entityAdded(String domain, T obj, Object state);

	void entityUpdated(String domain, T obj, Object state);

	void entityRemoving(String domain, T obj, ConfigTransaction xact, Object state);

	void entityRemoved(String domain, T obj, Object state);

	void entitiesAdded(String domain, Collection<EntityState> objs);

	void entitiesUpdated(String domain, Collection<EntityState> objs);

	void entitiesRemoving(String domain, Collection<EntityState> objs, ConfigTransaction xact);

	void entitiesRemoved(String domain, Collection<EntityState> objs);
}
