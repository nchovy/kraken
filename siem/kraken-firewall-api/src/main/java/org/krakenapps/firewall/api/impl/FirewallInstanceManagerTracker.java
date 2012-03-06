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
package org.krakenapps.firewall.api.impl;

import org.krakenapps.firewall.api.FirewallInstanceManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class FirewallInstanceManagerTracker extends ServiceTracker {
	private FirewallControllerEngine engine;

	public FirewallInstanceManagerTracker(BundleContext bc, FirewallControllerEngine engine) {
		super(bc, FirewallInstanceManager.class.getName(), null);
		this.engine = engine;
	}

	@Override
	public Object addingService(ServiceReference reference) {
		FirewallInstanceManager manager = (FirewallInstanceManager) super.addingService(reference);
		engine.register(manager);
		return manager;
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {
		engine.unregister((FirewallInstanceManager) service);
		super.removedService(reference, service);
	}

}
