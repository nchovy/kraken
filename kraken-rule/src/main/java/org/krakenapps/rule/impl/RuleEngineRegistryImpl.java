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
package org.krakenapps.rule.impl;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.rule.RuleEngine;
import org.krakenapps.rule.RuleEngineRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "rule-engine-registry")
@Provides
public class RuleEngineRegistryImpl extends ServiceTracker implements RuleEngineRegistry {
	private final Logger logger = LoggerFactory.getLogger(RuleEngineRegistryImpl.class.getName());

	private BundleContext bc;
	private ConcurrentMap<String, RuleEngine> engines;

	public RuleEngineRegistryImpl(BundleContext bc) {
		super(bc, RuleEngine.class.getName(), null);
		this.bc = bc;
		engines = new ConcurrentHashMap<String, RuleEngine>();
	}

	@Validate
	public void start() {
		super.open();

		// add all existing rule engines
		try {
			ServiceReference[] refs = bc.getServiceReferences(RuleEngine.class.getName(), null);
			if (refs == null)
				return;

			for (ServiceReference ref : refs) {
				RuleEngine engine = (RuleEngine) bc.getService(ref);
				engines.put(engine.getName(), engine);
			}
		} catch (InvalidSyntaxException e) {
		}
	}

	@Invalidate
	public void stop() {
		super.close();
	}

	@Override
	public Collection<RuleEngine> getEngines() {
		return engines.values();
	}

	@Override
	public RuleEngine getEngine(String name) {
		return engines.get(name);
	}

	@Override
	public Object addingService(ServiceReference reference) {
		RuleEngine engine = (RuleEngine) super.addingService(reference);
		engines.put(engine.getName(), engine);

		logger.info("kraken rule: engine [{}] added", engine.getName());
		return engine;
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {
		RuleEngine engine = (RuleEngine) service;
		engines.remove(engine.getName());
		super.removedService(reference, service);

		logger.info("kraken rule: engine [{}] removed", engine.getName());
	}

}
