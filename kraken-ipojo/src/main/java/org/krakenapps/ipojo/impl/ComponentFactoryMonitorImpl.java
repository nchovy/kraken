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
package org.krakenapps.ipojo.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.ipojo.ComponentFactoryTracker;
import org.krakenapps.ipojo.ComponentFactoryMonitor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "ipojo-component-monitor")
@Provides
public class ComponentFactoryMonitorImpl extends ServiceTracker implements ComponentFactoryMonitor {
	private final Logger logger = LoggerFactory.getLogger(ComponentFactoryMonitor.class.getName());

	private BundleContext bc;
	private Set<String> validFactories;
	private Set<ComponentFactoryTracker> trackers;

	public ComponentFactoryMonitorImpl(BundleContext bc) {
		super(bc, Factory.class.getName(), null);
		this.bc = bc;
		this.validFactories = Collections.synchronizedSet(new HashSet<String>());
		this.trackers = Collections.synchronizedSet(new HashSet<ComponentFactoryTracker>());
	}

	@Validate
	public void start() {
		super.open();

		try {
			ServiceReference[] refs = bc.getAllServiceReferences(Factory.class.getName(), null);
			for (ServiceReference ref : refs) {
				String componentClass = ref.getProperty("component.class").toString();
				validFactories.add(componentClass);
			}
		} catch (InvalidSyntaxException e) {
			logger.error("kraken ipojo: invalid factory filter", e);
		}
	}

	@Invalidate
	public void stop() {
		validFactories.clear();
		trackers.clear();
	}

	@Override
	public Object addingService(ServiceReference reference) {
		Factory factory = (Factory) super.addingService(reference);
		String componentClass = getComponentClass(reference);
		validFactories.add(componentClass);

		checkRequirements();

		return factory;
	}

	private String getComponentClass(ServiceReference reference) {
		return reference.getProperty("component.class").toString();
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {
		String componentClass = getComponentClass(reference);
		validFactories.remove(componentClass);
		super.removedService(reference, service);
	}

	@Override
	public Set<String> getComponentFactories() {
		return new HashSet<String>(validFactories);
	}

	private void checkRequirements() {
		for (ComponentFactoryTracker ready : trackers) {
			try {
				boolean satisfied = true;

				for (String componentName : ready.getRequiredFactoryNames()) {
					if (!validFactories.contains(componentName)) {
						satisfied = false;
						break;
					}
				}

				if (satisfied)
					ready.onReady();

			} catch (Exception e) {
				logger.warn("kraken ipojo: callback should not throw any exception", e);
			}
		}
	}

	@Override
	public Set<ComponentFactoryTracker> getTrackers() {
		return new HashSet<ComponentFactoryTracker>(trackers);
	}

	@Override
	public void addTracker(ComponentFactoryTracker ready) {
		trackers.add(ready);
	}

	@Override
	public void removeTracker(ComponentFactoryTracker ready) {
		trackers.remove(ready);
	}
}
