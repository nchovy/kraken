/*
 * Copyright 2011 Future Systems, Inc
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
package org.krakenapps.msgbus.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.msgbus.ResourceApi;
import org.krakenapps.msgbus.ResourceHandler;

@Component(name = "msgbus-resource-api")
@Provides
public class ResourceApiImpl implements ResourceApi {
	private ConcurrentMap<String, ResourceHandler> handlers;

	public ResourceApiImpl() {
		handlers = new ConcurrentHashMap<String, ResourceHandler>();
	}

	@Override
	public Collection<String> getResourceHandlerKeys() {
		return new ArrayList<String>(handlers.keySet());
	}

	@Override
	public ResourceHandler getResourceHandler(String groupId) {
		return handlers.get(groupId);
	}

	@Override
	public void register(String groupId, ResourceHandler handler) {
		ResourceHandler old = handlers.putIfAbsent(groupId, handler);
		if (old != null)
			throw new IllegalStateException("duplicated resource handler: " + groupId);
	}

	@Override
	public void unregister(String groupId, ResourceHandler handler) {
		ResourceHandler old = handlers.get(groupId);
		if (old == handler)
			handlers.remove(groupId);
	}
}
