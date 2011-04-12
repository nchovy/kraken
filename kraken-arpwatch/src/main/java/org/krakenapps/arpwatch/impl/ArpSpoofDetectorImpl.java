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
package org.krakenapps.arpwatch.impl;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.krakenapps.arpwatch.ArpCache;
import org.krakenapps.arpwatch.ArpCacheListener;
import org.krakenapps.arpwatch.ArpEntry;
import org.krakenapps.arpwatch.ArpSpoofDetector;
import org.krakenapps.arpwatch.ArpSpoofEvent;
import org.krakenapps.arpwatch.ArpSpoofEventListener;
import org.krakenapps.arpwatch.ArpStaticBinding;
import org.krakenapps.arpwatch.ArpStaticBindingConfig;

public class ArpSpoofDetectorImpl implements ArpSpoofDetector, ArpCacheListener {
	private ArpCache cache;
	private ArpStaticBindingConfig config;
	private Set<ArpSpoofEventListener> callbacks;

	public ArpSpoofDetectorImpl() {
		callbacks = Collections.newSetFromMap(new ConcurrentHashMap<ArpSpoofEventListener, Boolean>());
	}

	// validate callback
	public void start() {
		cache.register(this);
	}

	public void stop() {
		if (cache != null)
			cache.unregister(this);
	}

	@Override
	public void entryAdded(ArpEntry entry) {
		checkStaticBinding(entry);
	}

	@Override
	public void entryUpdated(ArpEntry entry) {
		checkStaticBinding(entry);
	}

	@Override
	public void entryChanged(ArpEntry oldEntry, ArpEntry newEntry) {
		checkStaticBinding(newEntry);
	}

	private void checkStaticBinding(ArpEntry newEntry) {
		ArpStaticBinding binding = config.find(newEntry.getIpAddress());
		if (binding != null && !binding.getMacAddress().equals(newEntry.getMacAddress())) {
			ArpSpoofEvent event = new ArpSpoofEventImpl(newEntry.getMacAddress(), newEntry.getIpAddress());
			for (ArpSpoofEventListener callback : callbacks) {
				try {
					callback.underAttack(event);
				} catch (Exception e) {
					// should not reach
				}
			}
		}
	}

	@Override
	public void entryRemoved(ArpEntry entry) {
	}

	@Override
	public void register(ArpSpoofEventListener callback) {
		callbacks.add(callback);
	}

	@Override
	public void unregister(ArpSpoofEventListener callback) {
		callbacks.remove(callback);
	}

}
