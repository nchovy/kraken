/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.logdb.impl;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.logdb.LookupHandler;
import org.krakenapps.logdb.LookupHandlerRegistry;

@Component(name = "logdb-lookup-registry")
@Provides
public class LookupHandlerRegistryImpl implements LookupHandlerRegistry {
	private ConcurrentMap<String, LookupHandler> lookupHandlers;

	@Override
	public Collection<String> getLookupHandlerNames() {
		return lookupHandlers.keySet();
	}

	public LookupHandlerRegistryImpl() {
		lookupHandlers = new ConcurrentHashMap<String, LookupHandler>();
	}

	@Override
	public void addLookupHandler(String name, LookupHandler handler) {
		LookupHandler old = lookupHandlers.putIfAbsent(name, handler);
		if (old != null)
			throw new IllegalStateException("lookup handler name conflict: " + name);
	}

	@Override
	public LookupHandler getLookupHandler(String name) {
		return lookupHandlers.get(name);
	}

	@Override
	public void removeLookupHandler(String name) {
		lookupHandlers.remove(name);
	}

}
