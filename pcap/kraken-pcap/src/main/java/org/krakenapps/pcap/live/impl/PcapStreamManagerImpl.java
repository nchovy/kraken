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
package org.krakenapps.pcap.live.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.krakenapps.pcap.live.PcapDevice;
import org.krakenapps.pcap.live.PcapDeviceMetadata;
import org.krakenapps.pcap.live.PcapDeviceManager;
import org.krakenapps.pcap.live.PcapStreamEventListener;
import org.krakenapps.pcap.live.PcapStreamManager;
import org.krakenapps.pcap.live.PcapStat;
import org.krakenapps.pcap.live.Promiscuous;
import org.krakenapps.pcap.util.PcapLiveRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author delmitz
 */
public class PcapStreamManagerImpl implements PcapStreamManager {
	private final Logger logger = LoggerFactory.getLogger(PcapStreamManagerImpl.class.getName());

	private CopyOnWriteArraySet<PcapStreamEventListener> callbacks;

	/**
	 * alias to device mappings
	 */
	private Map<String, LiveStream> streamMap;

	public void validate() {
		if (streamMap == null)
			streamMap = new ConcurrentHashMap<String, LiveStream>();

		callbacks = new CopyOnWriteArraySet<PcapStreamEventListener>();
	}

	public void invalidate() {
		for (String alias : streamMap.keySet()) {
			stop(alias);
		}

		streamMap.clear();
		callbacks.clear();
	}

	@Override
	public Collection<String> getStreamKeys() {
		return Collections.unmodifiableCollection(streamMap.keySet());
	}

	@Override
	public PcapLiveRunner get(String alias) {
		LiveStream stream = streamMap.get(alias);
		if (stream == null)
			return null;

		return stream.runner;
	}

	@Override
	public PcapStat getStat(String alias) {
		PcapLiveRunner runner = get(alias);
		if (runner == null)
			return null;

		try {
			return runner.getDevice().getStat();
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public void start(String key, String deviceName, int milliseconds) throws IOException {
		start(key, deviceName, milliseconds, Promiscuous.Off, null);
	}

	@Override
	public void start(String key, String deviceName, int milliseconds, Promiscuous promisc) throws IOException {
		start(key, deviceName, milliseconds, promisc, null);
	}

	@Override
	public void start(String key, String deviceName, int milliseconds, Promiscuous promisc, String filter)
			throws IOException {
		PcapDeviceMetadata info = null;
		for (PcapDeviceMetadata d : PcapDeviceManager.getDeviceMetadataList()) {
			if (d.getName().equals(deviceName)) {
				info = d;
				break;
			}
		}

		PcapDevice device = PcapDeviceManager.open(info.getName(), promisc, milliseconds);
		if (filter != null)
			device.setFilter(filter, true);

		if (streamMap.containsKey(key))
			throw new IllegalArgumentException("duplicated alias of pcap device: " + key);

		logger.info("kraken-pcap: starting live runner [{}]", key);

		LiveStream stream = new LiveStream();

		stream.runner = new PcapLiveRunner(device);
		stream.thread = new Thread(stream.runner);

		streamMap.put(key, stream);
		stream.thread.start();

		// fire callbacks
		for (PcapStreamEventListener callback : callbacks) {
			try {
				callback.onOpen(key, stream.runner);
			} catch (Exception e) {
				logger.warn("kraken pcap: callback should not throw any exception", e);
			}
		}
	}

	@Override
	public void stop(String alias) {
		logger.info("kraken-pcap: stopping live runner [{}]", alias);
		LiveStream stream = streamMap.remove(alias);
		if (stream != null) {
			// fire callbacks
			for (PcapStreamEventListener callback : callbacks) {
				try {
					callback.onClose(alias, stream.runner);
				} catch (Exception e) {
					logger.warn("kraken pcap: callback should not throw any exception", e);
				}
			}

			stream.thread.interrupt();
			stream.runner.stop();
		}
	}

	@Override
	public void addEventListener(PcapStreamEventListener callback) {
		callbacks.add(callback);
	}

	@Override
	public void removeEventListener(PcapStreamEventListener callback) {
		callbacks.remove(callback);
	}

	private static class LiveStream {
		private Thread thread;
		private PcapLiveRunner runner;
	}
}
