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

import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.krakenapps.arpwatch.ArpCache;
import org.krakenapps.arpwatch.ArpCacheListener;
import org.krakenapps.arpwatch.ArpEntry;
import org.krakenapps.pcap.decoder.arp.ArpPacket;

public class ArpCacheImpl implements ArpCache {
	private Map<InetAddress, ArpEntry> entryMap;
	private Set<ArpCacheListener> listeners;

	public ArpCacheImpl() {
		entryMap = new ConcurrentHashMap<InetAddress, ArpEntry>();
		listeners = Collections.newSetFromMap(new ConcurrentHashMap<ArpCacheListener, Boolean>());
	}

	@Override
	public ArpEntry find(InetAddress ip) {
		return entryMap.get(ip);
	}

	@Override
	public Collection<ArpEntry> getCachedEntries() {
		return Collections.unmodifiableCollection(entryMap.values());
	}

	@Override
	public void add(ArpPacket p) {
		if (p.getOpcode() == 2 || (p.getOpcode() == 1 && p.getSenderIp().equals(p.getTargetIp()))) {
			ArpEntry oldEntry = find(p.getSenderIp());
			if (oldEntry == null) {
				addNewEntry(p);
				return;
			}

			if (oldEntry.getMacAddress().equals(p.getSenderMac())) {
				updateEntry(p, oldEntry);
			} else {
				alertEntry(p, oldEntry);
			}
		}
	}

	private void alertEntry(ArpPacket p, ArpEntry oldEntry) {
		ArpEntry entry = new ArpEntryImpl(p.getSenderMac(), p.getSenderIp(), new Date(), new Date());
		entryMap.put(entry.getIpAddress(), entry);

		// alert!
		for (ArpCacheListener callback : listeners) {
			try {
				callback.entryChanged(oldEntry, entry);
			} catch (Exception e) {
				// should not reach
			}
		}
	}

	private void addNewEntry(ArpPacket p) {
		ArpEntry entry = new ArpEntryImpl(p.getSenderMac(), p.getSenderIp(), new Date(), new Date());
		entryMap.put(entry.getIpAddress(), entry);
		for (ArpCacheListener callback : listeners) {
			try {
				callback.entryAdded(entry);
			} catch (Exception e) {
				// should not reach
			}
		}
	}

	private void updateEntry(ArpPacket p, ArpEntry oldEntry) {
		// extend timeout
		ArpEntry entry = new ArpEntryImpl(p.getSenderMac(), p.getSenderIp(), oldEntry.getFirstSeen(), new Date());
		entryMap.put(entry.getIpAddress(), entry);
		
		for (ArpCacheListener callback : listeners) {
			try {
				callback.entryUpdated(entry);
			} catch (Exception e) {
				// should not reach
			}
		}
	}

	@Override
	public void register(ArpCacheListener listener) {
		listeners.add(listener);
	}

	@Override
	public void unregister(ArpCacheListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void flush() {
		entryMap.clear();
	}

}
