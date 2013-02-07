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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.rule.RuleDatabase;
import org.krakenapps.rule.RuleGroup;
import org.krakenapps.rule.RuleStorage;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

@Component(name = "rule-database")
@Provides
public class RuleDatabaseImpl extends ServiceTracker implements RuleDatabase {
	private Set<RuleStorage> storages;

	public RuleDatabaseImpl(BundleContext bc) {
		super(bc, RuleStorage.class.getName(), null);
		this.storages = Collections.synchronizedSet(new HashSet<RuleStorage>());
	}

	@Validate
	public void start() {
		super.open();
	}

	@Invalidate
	public void stop() {
		super.close();
	}

	@Override
	public Object addingService(ServiceReference reference) {
		RuleStorage storage = (RuleStorage) super.addingService(reference);
		storages.add(storage);
		return storage;
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {
		RuleStorage storage = (RuleStorage) service;
		storages.remove(storage);
		super.removedService(reference, service);
	}

	@Override
	public Collection<RuleStorage> getStorages() {
		return storages;
	}

	@Override
	public Collection<RuleGroup> getRuleGroups(String name) {
		List<RuleGroup> l = new ArrayList<RuleGroup>();
		for (RuleStorage r : storages) {
			RuleGroup g = r.getRuleGroup(name);
			if (g != null)
				l.add(g);
		}
		return l;
	}

	@Override
	public void addStorage(RuleStorage storage) {
		storages.add(storage);
	}

	@Override
	public void removeStorage(RuleStorage storage) {
		storages.remove(storage);
	}

}
