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

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.krakenapps.arpwatch.ArpCache;
import org.krakenapps.arpwatch.ArpSpoofDetector;
import org.krakenapps.arpwatch.ArpStaticBindingConfig;
import org.krakenapps.arpwatch.ArpWatcher;
import org.krakenapps.pcap.decoder.arp.ArpPacket;
import org.krakenapps.pcap.decoder.arp.ArpProcessor;
import org.krakenapps.pcap.live.PcapStreamManager;
import org.krakenapps.pcap.util.PcapLiveRunner;

public class ArpWatcherImpl implements ArpWatcher, ArpProcessor {
	private PcapStreamManager streamManager;
	private ArpCache cache;
	private ArpSpoofDetector detector;
	private ArpStaticBindingConfig config;
	private Set<String> streams;

	public ArpWatcherImpl() {
		streams = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
	}

	@Override
	public Collection<String> getStreamKeys() {
		return Collections.unmodifiableCollection(streams);
	}

	@Override
	public ArpCache getArpCache() {
		return cache;
	}

	@Override
	public ArpSpoofDetector getDetector() {
		return detector;
	}

	@Override
	public ArpStaticBindingConfig getStaticBindingConfig() {
		return config;
	}

	@Override
	public void start(String streamKey) throws IllegalArgumentException {
		PcapLiveRunner runner = getArpDecoder(streamKey);
		if (runner != null) {
			streams.add(streamKey);
			runner.getArpDecoder().register(this);
		}
	}

	private PcapLiveRunner getArpDecoder(String streamKey) {
		PcapLiveRunner runner = streamManager.get(streamKey);
		if (runner == null)
			throw new IllegalArgumentException("[" + streamKey + "]stream not found");
		return runner;
	}

	@Override
	public void process(ArpPacket p) {
		if (cache != null)
			cache.add(p);
	}

	@Override
	public void stop() {
		for (String streamKey : streams) {
			PcapLiveRunner runner = getArpDecoder(streamKey);
			if (runner != null)
				runner.getArpDecoder().unregister(this);
		}

		if (cache != null)
			cache.flush();
	}
}
