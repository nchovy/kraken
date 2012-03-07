/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.log.api.impl;

import org.krakenapps.log.api.LoggerFactory;
import org.krakenapps.log.api.LoggerFactoryRegistryEventListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class LoggerFactoryTracker extends ServiceTracker {
	private LoggerFactoryRegistryEventListener listener;

	public LoggerFactoryTracker(BundleContext context, LoggerFactoryRegistryEventListener listener) {
		super(context, LoggerFactory.class.getName(), null);
		this.listener = listener;
	}

	@Override
	public Object addingService(ServiceReference reference) {
		LoggerFactory loggerFactory = (LoggerFactory) super.addingService(reference);
		listener.factoryAdded(loggerFactory);
		return loggerFactory;
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {
		LoggerFactory loggerFactory = (LoggerFactory) service;
		listener.factoryRemoved(loggerFactory);
		super.removedService(reference, service);
	}

}
