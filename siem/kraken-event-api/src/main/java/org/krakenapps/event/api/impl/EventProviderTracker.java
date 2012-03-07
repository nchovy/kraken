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

import org.krakenapps.event.api.EventProvider;
import org.krakenapps.event.api.EventProviderRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class EventProviderTracker extends ServiceTracker {
	private EventProviderRegistry registry;

	public EventProviderTracker(BundleContext bc, EventProviderRegistry registry) {
		super(bc, EventProvider.class.getName(), null);
		this.registry = registry;
	}

	@Override
	public Object addingService(ServiceReference reference) {
		EventProvider provider = (EventProvider) super.addingService(reference);
		registry.register(provider);
		return provider;
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {
		EventProvider provider = (EventProvider) service;
		registry.unregister(provider);
		super.removedService(reference, service);
	}
}
