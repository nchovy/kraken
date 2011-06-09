/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.ipmanager.impl;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.ipmanager.IpDetection;
import org.krakenapps.ipmanager.IpManager;
import org.krakenapps.ipmanager.IpMonitor;
import org.krakenapps.pcap.Protocol;
import org.krakenapps.pcap.decoder.dhcp.DhcpDecoder;
import org.krakenapps.pcap.decoder.dhcp.DhcpMessage;
import org.krakenapps.pcap.decoder.dhcp.DhcpMessage.Type;
import org.krakenapps.pcap.decoder.dhcp.DhcpOptions;
import org.krakenapps.pcap.decoder.dhcp.DhcpProcessor;
import org.krakenapps.pcap.decoder.dhcp.fingerprint.FingerprintDetector;
import org.krakenapps.pcap.decoder.dhcp.fingerprint.FingerprintMetadata;
import org.krakenapps.pcap.decoder.ethernet.EthernetFrame;
import org.krakenapps.pcap.decoder.ethernet.MacAddress;
import org.krakenapps.pcap.decoder.ip.IpPacket;
import org.krakenapps.pcap.decoder.netbios.NetBiosDecoder;
import org.krakenapps.pcap.decoder.netbios.NetBiosNamePacket;
import org.krakenapps.pcap.decoder.netbios.NetBiosNameProcessor;
import org.krakenapps.pcap.decoder.netbios.NetBiosNameHeader.Opcode;
import org.krakenapps.pcap.decoder.netbios.rr.NbResourceRecord;
import org.krakenapps.pcap.decoder.udp.UdpPacket;
import org.krakenapps.pcap.decoder.udp.UdpProcessor;
import org.krakenapps.pcap.live.PcapDevice;
import org.krakenapps.pcap.live.PcapDeviceManager;
import org.krakenapps.pcap.live.PcapDeviceMetadata;
import org.krakenapps.pcap.live.PcapStreamEventListener;
import org.krakenapps.pcap.util.PcapLiveRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Monitor IP entries of local area network
 */
@Component(name = "ipm-ip-monitor")
@Provides(specifications = { IpMonitor.class })
public class IpMonitorService implements IpMonitor, PcapStreamEventListener, UdpProcessor, NetBiosNameProcessor,
		DhcpProcessor {
	private final Logger logger = LoggerFactory.getLogger(IpMonitorService.class.getName());

	@Requires
	private IpManager ipManager;

	private Map<PcapLiveRunner, Thread> stream = new HashMap<PcapLiveRunner, Thread>();

	private NetBiosDecoder netbios;
	private DhcpDecoder dhcp;

	private Map<Integer, TemporaryIpDetection> dhcpRequests = new HashMap<Integer, TemporaryIpDetection>();
	private PriorityQueue<RequestTimestamp> reqTimestamps = new PriorityQueue<RequestTimestamp>(11,
			new RequestTimestampComparator());

	@Validate
	public void start() {
		netbios = new NetBiosDecoder();
		netbios.registerNameProcessor(this);

		dhcp = new DhcpDecoder();
		dhcp.register(this);

		for (PcapDeviceMetadata metadata : PcapDeviceManager.getDeviceMetadataList()) {
			try {
				PcapDevice device = PcapDeviceManager.open(metadata.getName(), 30000);
				device.setFilter("udp");
				PcapLiveRunner runner = new PcapLiveRunner(device);
				runner.getUdpDecoder().registerUdpProcessor(this);
				runner.setUdpProcessor(Protocol.DHCP, dhcp);
				runner.setUdpProcessor(Protocol.NETBIOS, netbios);
				Thread thread = new Thread(runner, "IP Monitor " + device.getMetadata().getName());
				stream.put(runner, thread);
				thread.start();
			} catch (IOException e) {
				logger.error("kraken ipmanager: device open failed (" + metadata.getName() + ")", e);
			}
		}

		for (PcapLiveRunner runner : stream.keySet())
			onOpen("ipm", runner);
	}

	@Invalidate
	public void stop() {
		// remove all callbacks
		if (stream != null) {
			for (PcapLiveRunner runner : stream.keySet()) {
				stream.get(runner).interrupt();
				onClose("ipm", runner);

				try {
					runner.getDevice().close();
				} catch (IOException e) {
					logger.error("kraken ipmanager: device close failed (" + runner.getDevice().getMetadata().getName()
							+ ")", e);
				}
			}
			stream.clear();
		}

		dhcp.unregister(this);
		netbios.unregisterNameProcessor(this);
	}

	@Override
	public void onOpen(String key, PcapLiveRunner runner) {
		runner.setUdpProcessor(Protocol.NETBIOS, netbios);
		runner.setUdpProcessor(Protocol.DHCP, dhcp);
		runner.getUdpDecoder().registerUdpProcessor(this);
		logger.info("kraken ipmanager: connected pcap stream [{}], device [{}]", key, runner.getDevice());
	}

	@Override
	public void onClose(String key, PcapLiveRunner runner) {
		runner.unsetUdpProcessor(Protocol.NETBIOS, netbios);
		runner.unsetUdpProcessor(Protocol.DHCP, dhcp);
		runner.getUdpDecoder().unregisterUdpProcessor(this);
		logger.info("kraken ipmanager: disconnected pcap stream [{}], device [{}]", key, runner.getDevice());
	}

	@Override
	public void process(UdpPacket p) {
		if (p.getSource().getAddress() instanceof Inet6Address) {
			logger.debug("kraken ipmanager: ipv6 address [{}] detected", p.getSource().getAddress());
			return;
		}

		if (p.getSource().getAddress().getHostAddress().equals("0.0.0.0"))
			return;

		// assume ipv4 broadcast packet
		IpPacket ip = p.getIpPacket();
		EthernetFrame eth = (EthernetFrame) (ip.getL2Frame());
		MacAddress sourceMac = eth.getSource();
		InetAddress sourceIp = ip.getSourceAddress();

		IpDetection d = new IpDetection("local", new Date(), sourceMac, sourceIp);
		ipManager.updateIpEntry(d);
	}

	@Override
	public void process(NetBiosNamePacket p) {
		if (p.getUdpPacket().getSource().getAddress().getHostAddress().equals("0.0.0.0"))
			return;

		if (p.getHeader().getOpcode() == Opcode.Registration) {

			NbResourceRecord rr = (NbResourceRecord) p.getData().getAdditionals().get(0);

			String hostName = null;
			String workGroup = null;

			if (rr.getAddressList().get(0).getFlag() == 0)
				hostName = rr.getName();
			else
				workGroup = rr.getName();

			byte domainType = p.getData().getDomainType();
			if (domainType != 0x20 && domainType == 0x00)
				return;

			IpPacket ipPacket = p.getUdpPacket().getIpPacket();
			EthernetFrame frame = (EthernetFrame) ipPacket.getL2Frame();

			ipManager.updateIpEntry(new IpDetection("local", new Date(), frame.getSource(),
					ipPacket.getSourceAddress(), hostName, workGroup, null, null));
		}

		logger.trace("kraken ipmanager: netbios name packet [{}]", p);
	}

	@Override
	public void process(DhcpMessage msg) {
		Type type = DhcpOptions.getDhcpMessageType(msg);

		MacAddress mac = msg.getClientMac();
		InetAddress ip = msg.getClientAddress();
		String workgroup = DhcpOptions.getDomainName(msg);
		String category = null;
		String vendor = null;
		String hostName = DhcpOptions.getHostName(msg);

		String fingerprint = DhcpOptions.getFingerprint(msg);
		FingerprintMetadata metadata = FingerprintDetector.matches(fingerprint);
		if (metadata != null) {
			category = metadata.getCategory();
			vendor = metadata.getVendor();
			logger.trace("kraken ipmanager: dhcp [{}] metadata [{}]", hostName, metadata);
		}

		if (type == Type.Request) {
			TemporaryIpDetection tid = new TemporaryIpDetection();
			tid.mac = mac;
			tid.hostName = hostName;
			tid.workgroup = workgroup;
			tid.category = category;
			tid.vendor = vendor;
			dhcpRequests.put(msg.getTransactionId(), tid);
			reqTimestamps.add(new RequestTimestamp(msg.getTransactionId()));
		} else if (type == Type.Inform) {
			IpDetection d = new IpDetection("local", new Date(), mac, ip, hostName, workgroup, category, vendor);
			ipManager.updateIpEntry(d);
		} else if (type == Type.Ack) {
			TemporaryIpDetection tid = dhcpRequests.get(msg.getTransactionId());
			if (tid == null)
				return;

			mac = tid.mac;
			ip = msg.getYourAddress();
			hostName = tid.hostName;
			workgroup = tid.workgroup;
			category = tid.category;
			vendor = tid.vendor;
			dhcpRequests.remove(msg.getTransactionId());

			IpDetection d = new IpDetection("local", new Date(), mac, ip, hostName, workgroup, category, vendor);
			ipManager.updateIpEntry(d);
		}

		long now = System.currentTimeMillis();
		while (!reqTimestamps.isEmpty() && now - reqTimestamps.peek().date.getTime() > 30000) {
			RequestTimestamp rt = reqTimestamps.poll();
			dhcpRequests.remove(rt.transactionId);
		}
	}

	private class TemporaryIpDetection {
		private MacAddress mac;
		private String hostName;
		private String workgroup;
		private String category;
		private String vendor;
	}

	private class RequestTimestamp {
		private Integer transactionId;
		private Date date = new Date();

		private RequestTimestamp(Integer transactionId) {
			this.transactionId = transactionId;
		}
	}

	private class RequestTimestampComparator implements Comparator<RequestTimestamp> {
		@Override
		public int compare(RequestTimestamp o1, RequestTimestamp o2) {
			return (int) (o1.date.getTime() - o2.date.getTime());
		}
	}
}
