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
package org.krakenapps.pcap.util;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.pcap.decoder.arp.ArpDecoder;
import org.krakenapps.pcap.decoder.arp.ArpPacket;
import org.krakenapps.pcap.decoder.arp.ArpProcessor;
import org.krakenapps.pcap.decoder.ethernet.EthernetDecoder;
import org.krakenapps.pcap.decoder.ethernet.EthernetFrame;
import org.krakenapps.pcap.decoder.ethernet.EthernetHeader;
import org.krakenapps.pcap.decoder.ethernet.EthernetType;
import org.krakenapps.pcap.decoder.ethernet.MacAddress;
import org.krakenapps.pcap.live.AddressBinding;
import org.krakenapps.pcap.live.PcapDevice;
import org.krakenapps.pcap.live.PcapDeviceManager;
import org.krakenapps.pcap.live.PcapDeviceMetadata;
import org.krakenapps.pcap.packet.PcapPacket;
import org.krakenapps.pcap.routing.RoutingEntry;
import org.krakenapps.pcap.routing.RoutingTable;

/**
 * @author xeraph
 */
public class Arping {
	public static Map<InetAddress, MacAddress> scan(String deviceName, int timeout) throws InterruptedException,
			IOException {
		PcapDeviceMetadata metadata = PcapDeviceManager.getDeviceMetadata(deviceName);
		return scan(metadata.getName(), metadata.getSubnet(), metadata.getNetmask(), timeout);
	}

	public static Map<InetAddress, MacAddress> scan(String deviceName, InetAddress network, InetAddress mask,
			int timeout) throws InterruptedException, IOException {
		return scan(deviceName, network, mask, timeout, 0);
	}

	public static Map<InetAddress, MacAddress> scan(String deviceName, InetAddress network, InetAddress mask,
			int timeout, int ipg) throws InterruptedException, IOException {
		if (!(network instanceof Inet4Address) || !(mask instanceof Inet4Address))
			throw new IllegalArgumentException("network address should be IPv4 address");

		PcapDevice device = null;
		try {
			device = PcapDeviceManager.open(deviceName, 1000);
			device.setFilter("arp", false);

			ArpListener listener = new ArpListener(device);
			Thread t = new Thread(listener);
			t.start();

			// send request
			int net = IpConverter.toInt((Inet4Address) network);
			int m = IpConverter.toInt((Inet4Address) mask);
			int from = (net & m);
			int to = from | ~m;

			for (int ip = from; ip <= to; ip++) {
				InetAddress targetIp = IpConverter.toInetAddress(ip);
				Buffer b = preparePacket(device.getMetadata(), targetIp);
				device.write(b);

				if (ipg > 0)
					Thread.sleep(ipg);
			}

			Thread.sleep(timeout);
			listener.stop = true;
			t.interrupt();

			return listener.table;
		} finally {
			// double check (listener should already closed device)
			if (device != null && device.isOpen())
				device.close();
		}
	}

	private static class ArpListener implements Runnable {
		private volatile boolean stop = false;
		private PcapDevice device;
		private EthernetDecoder eth;
		private Map<InetAddress, MacAddress> table;

		public ArpListener(PcapDevice device) {
			this.device = device;
			eth = new EthernetDecoder();
			table = new HashMap<InetAddress, MacAddress>();
		}

		@Override
		public void run() {
			ArpCallback callback = buildArpStack(eth);

			try {
				while (!stop) {
					PcapPacket p = device.getPacket();
					eth.decode(p);
					addToCache(table, callback);
				}
			} catch (IOException e) {
			} finally {
				try {
					if (device != null && device.isOpen())
						device.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public static Map<InetAddress, MacAddress> scan(String deviceName, Collection<InetAddress> targets, int timeout)
			throws IOException {
		Map<InetAddress, MacAddress> table = new HashMap<InetAddress, MacAddress>();

		EthernetDecoder eth = new EthernetDecoder();
		ArpCallback callback = buildArpStack(eth);

		PcapDevice device = PcapDeviceManager.open(deviceName, timeout);
		device.setFilter("arp", false);

		device.getPacket();

		PcapDeviceMetadata metadata = PcapDeviceManager.getDeviceMetadata(deviceName);
		for (InetAddress target : targets) {
			Buffer b = preparePacket(metadata, target);
			if (b != null)
				device.write(b);
		}

		long begin = new Date().getTime();
		while (true) {
			try {
				PcapPacket packet = device.getPacket();
				eth.decode(packet);
				addToCache(table, callback);
			} catch (IOException e) {
				if (!e.getMessage().equals("Timeout"))
					throw e;
			}
			long end = new Date().getTime();
			if (end - begin > timeout)
				break;
		}
		device.close();

		return table;
	}

	private static void addToCache(Map<InetAddress, MacAddress> table, ArpCallback callback) {
		ArpPacket reply = callback.getLast();
		if (reply != null && reply.getOpcode() == 0x2) {
			table.put(reply.getSenderIp(), reply.getSenderMac());
		}
	}

	public static MacAddress query(InetAddress targetIp, int timeout) throws IOException {
		RoutingEntry entry = RoutingTable.findRoute(targetIp);
		if (entry == null)
			throw new IllegalStateException("route not found for " + targetIp.getHostAddress());

		PcapDeviceMetadata metadata = PcapDeviceManager.getDeviceMetadata(entry.getInterfaceName());
		if (metadata == null)
			throw new IllegalStateException("interface not found for " + targetIp.getHostAddress());

		if (metadata.isIntranet(targetIp))
			return Arping.query(metadata.getName(), targetIp, timeout);

		return Arping.query(metadata.getName(), entry.getGateway(), timeout);
	}

	public static MacAddress query(String deviceName, InetAddress targetIp, int timeout) throws IOException {
		EthernetDecoder eth = new EthernetDecoder();
		ArpCallback callback = buildArpStack(eth);

		PcapDeviceMetadata metadata = PcapDeviceManager.getDeviceMetadata(deviceName);
		if (metadata == null)
			throw new IllegalArgumentException("pcap device not found: " + deviceName);

		Buffer b = preparePacket(metadata, targetIp);
		PcapDevice device = PcapDeviceManager.open(metadata.getName(), timeout);

		device.setFilter("arp", false);
		if (b != null) {
			device.write(b);
		}

		ArpPacket last = null;
		long begin = new Date().getTime();
		while (true) {
			try {
				PcapPacket packet = device.getPacket();
				eth.decode(packet);
				ArpPacket reply = callback.getLast();
				if (reply != null && reply.getOpcode() == 0x2 && reply.getSenderIp().equals(targetIp)) {
					last = reply;
					break;
				}
			} catch (IOException e) {
				if (!e.getMessage().equals("Timeout"))
					throw e;
			}
			long end = new Date().getTime();
			if (end - begin > timeout)
				break;
		}
		device.close();

		return last == null ? null : last.getSenderMac();
	}

	private static ArpCallback buildArpStack(EthernetDecoder eth) {
		ArpDecoder arp = new ArpDecoder();
		ArpCallback callback = new ArpCallback();
		eth.register(EthernetType.ARP, arp);
		arp.register(callback);
		return callback;
	}

	private static Buffer preparePacket(PcapDeviceMetadata metadata, InetAddress targetIp) {
		MacAddress senderMac = metadata.getMacAddress();

		InetAddress senderIp = null;
		for (AddressBinding binding : metadata.getBindings()) {
			if (binding.getAddress() instanceof Inet4Address) {
				senderIp = binding.getAddress();
				break;
			}
		}

		if (senderIp == null)
			return null;

		ArpPacket p = ArpPacket.createRequest(senderMac, senderIp, targetIp);

		MacAddress broadcastMac = new MacAddress("ff:ff:ff:ff:ff:ff");
		EthernetHeader ethernetHeader = new EthernetHeader(senderMac, broadcastMac, EthernetType.ARP);
		EthernetFrame frame = new EthernetFrame(ethernetHeader, p.getBuffer());
		return frame.getBuffer();
	}

	private static class ArpCallback implements ArpProcessor {
		private ArpPacket last;

		@Override
		public void process(ArpPacket p) {
			last = p;
		}

		public ArpPacket getLast() {
			return last;
		}
	}
}
