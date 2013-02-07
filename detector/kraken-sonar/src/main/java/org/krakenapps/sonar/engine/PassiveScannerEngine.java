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
 package org.krakenapps.sonar.engine;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.pcap.Protocol;
import org.krakenapps.pcap.decoder.arp.ArpPacket;
import org.krakenapps.pcap.decoder.arp.ArpProcessor;
import org.krakenapps.pcap.decoder.dhcp.DhcpMessage;
import org.krakenapps.pcap.decoder.dhcp.DhcpProcessor;
import org.krakenapps.pcap.decoder.http.HttpDecoder;
import org.krakenapps.pcap.decoder.http.HttpProcessor;
import org.krakenapps.pcap.decoder.http.HttpRequest;
import org.krakenapps.pcap.decoder.http.HttpResponse;
import org.krakenapps.pcap.live.PcapDevice;
import org.krakenapps.pcap.live.PcapDeviceManager;
import org.krakenapps.pcap.live.PcapDeviceMetadata;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.PcapLiveRunner;
import org.krakenapps.sonar.PassiveScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "sonar-passive-scanner")
@Provides
public class PassiveScannerEngine implements PassiveScanner {
	private ConcurrentMap<String, Sniffer> sniffers;
	private Set<ArpProcessor> arpSniffers;
	private Set<HttpProcessor> httpSniffers;
	private Set<DhcpProcessor> dhcpSniffers;

	@Validate
	@Override
	public void start() {
		sniffers = new ConcurrentHashMap<String, Sniffer>();
		arpSniffers = Collections.newSetFromMap(new ConcurrentHashMap<ArpProcessor, Boolean>());
		httpSniffers = Collections.newSetFromMap(new ConcurrentHashMap<HttpProcessor, Boolean>());
		dhcpSniffers = Collections.newSetFromMap(new ConcurrentHashMap<DhcpProcessor, Boolean>());

		for (PcapDeviceMetadata metadata : PcapDeviceManager.getDeviceMetadataList()) {
			PcapLiveRunner runner = openDevice(metadata);
			if (runner != null) {
				Sniffer sniffer = new Sniffer(runner);
				System.out.println("Kraken Sonar Sniffer (" + metadata.getName() + ")");
				new Thread(sniffer, "Kraken Sonar Sniffer (" + metadata.getName() + ")").start();
				sniffers.put(metadata.getName(), sniffer);
			}
		}
	}

	@Invalidate
	@Override
	public void stop() {
		for (Sniffer sniffer : sniffers.values()) {
			sniffer.stop();
		}

		httpSniffers.clear();
		sniffers.clear();
	}

	@Override
	public PcapLiveRunner getDevice(String name) {
		Sniffer sniffer = sniffers.get(name);
		if (sniffer == null)
			return null;

		return sniffer.runner;
	}

	@Override
	public Collection<String> getDeviceNames() {
		return sniffers.keySet();
	}

	private PcapLiveRunner openDevice(PcapDeviceMetadata metadata) {
		try {
			PcapDevice device = PcapDeviceManager.open(metadata.getName(), 10000);
			PcapLiveRunner runner = new PcapLiveRunner(device);

			return runner;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public void addArpSniffer(ArpProcessor callback) {
		arpSniffers.add(callback);
	}

	@Override
	public void removeArpSniffer(ArpProcessor callback) {
		arpSniffers.remove(callback);
	}

	@Override
	public void addTcpSniffer(Protocol protocol, Object callback) {
		if (protocol == Protocol.HTTP && callback instanceof HttpProcessor)
			httpSniffers.add((HttpProcessor) callback);
	}

	@Override
	public void removeTcpSniffer(Protocol protocol, Object callback) {
		if (protocol == Protocol.HTTP && callback instanceof HttpProcessor)
			httpSniffers.remove(callback);
	}

	@Override
	public void addUdpSniffer(Protocol protocol, Object callback) {
		if (protocol == Protocol.DHCP && callback instanceof DhcpProcessor)
			dhcpSniffers.add((DhcpProcessor) callback);
	}

	@Override
	public void removeUdpSniffer(Protocol protocol, Object callback) {
		if (protocol == Protocol.DHCP && callback instanceof DhcpProcessor)
			dhcpSniffers.remove(callback);
	}

	private class Sniffer implements Runnable, ArpProcessor, HttpProcessor, DhcpProcessor {
		private final Logger logger = LoggerFactory.getLogger(Sniffer.class.getName());
		private Thread executingThread;
		private PcapLiveRunner runner;

		public Sniffer(PcapLiveRunner runner) {
			this.runner = runner;
		}

		public void stop() {
			executingThread.interrupt();
			runner.stop();
		}

		@Override
		public void run() {
			this.executingThread = Thread.currentThread();

			try {
				HttpDecoder http = new HttpDecoder();
				runner.setTcpProcessor(Protocol.HTTP, http);
				runner.run();
			} catch (Exception e) {
				logger.error("kraken-sonar: sniffer terminated: ", e);
			}
		}

		//
		// Http Sniffers
		//

		@Override
		public void onRequest(HttpRequest req) {
			for (HttpProcessor sniffer : httpSniffers)
				sniffer.onRequest(req);
		}

		@Override
		public void onResponse(HttpRequest req, HttpResponse resp) {
			for (HttpProcessor sniffer : httpSniffers)
				sniffer.onResponse(req, resp);
		}

		@Override
		public void onMultipartData(Buffer buffer) {
			for (HttpProcessor sniffer : httpSniffers)
				sniffer.onMultipartData(buffer);
		}

		//
		// ARP
		//

		@Override
		public void process(ArpPacket p) {
			for (ArpProcessor arp : arpSniffers)
				arp.process(p);
		}

		//
		// DHCP
		//

		@Override
		public void process(DhcpMessage msg) {
			for (DhcpProcessor dhcp : dhcpSniffers)
				dhcp.process(msg);
		}
	}
}
