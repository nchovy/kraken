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

import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Date;

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
import org.krakenapps.pcap.live.PcapStreamEventListener;
import org.krakenapps.pcap.live.PcapStreamManager;
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

	@Requires
	private PcapStreamManager pcapStreamManager;

	private NetBiosDecoder netbios;
	private DhcpDecoder dhcp;

	@Validate
	public void start() {
		pcapStreamManager.addEventListener(this);

		netbios = new NetBiosDecoder();
		netbios.registerNameProcessor(this);

		dhcp = new DhcpDecoder();
		dhcp.register(this);

		for (String key : pcapStreamManager.getStreamKeys()) {
			PcapLiveRunner runner = pcapStreamManager.get(key);
			onOpen(key, runner);
		}
	}

	@Invalidate
	public void stop() {
		if (pcapStreamManager != null) {
			// remove all callbacks
			for (String key : pcapStreamManager.getStreamKeys()) {
				PcapLiveRunner runner = pcapStreamManager.get(key);
				onClose(key, runner);
			}

			pcapStreamManager.removeEventListener(this);
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
		String hostName = DhcpOptions.getHostName(msg);
		if (hostName == null)
			return;

		Type type = DhcpOptions.getDhcpMessageType(msg);
		if (type != Type.Inform && type != Type.Request)
			return;

		String fingerprint = DhcpOptions.getFingerprint(msg);
		FingerprintMetadata metadata = FingerprintDetector.matches(fingerprint);
		if (metadata != null)
			logger.trace("kraken ipmanager: dhcp [{}] metadata [{}]", hostName, metadata);

		MacAddress mac = msg.getClientMac();
		InetAddress ip = msg.getClientAddress();
		String workgroup = DhcpOptions.getDomainName(msg);

		String category = null;
		String vendor = null;
		if (metadata != null) {
			category = metadata.getCategory();
			vendor = metadata.getVendor();
		}

		IpDetection d = new IpDetection("local", new Date(), mac, ip, hostName, workgroup, category, vendor);
		ipManager.updateIpEntry(d);
	}
}
