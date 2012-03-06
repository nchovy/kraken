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
package org.krakenapps.radius.server.impl;

import org.krakenapps.radius.server.RadiusFactoryEventListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class RadiusFactoryServiceTracker extends ServiceTracker {

	private RadiusFactoryEventListener listener;

	public RadiusFactoryServiceTracker(RadiusFactoryEventListener listener, BundleContext bc, String classNameFilter) {
		super(bc, classNameFilter, null);
		this.listener = listener;
	}

	@Override
	public Object addingService(ServiceReference reference) {
		Object service = super.addingService(reference);
		listener.addingService(service);
		return service;
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {
		listener.removedService(service);
		super.removedService(reference, service);
	}
}
