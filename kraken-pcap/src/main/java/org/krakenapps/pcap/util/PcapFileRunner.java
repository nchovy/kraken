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

import java.io.EOFException;
import java.io.File;
import java.io.IOException;

import org.krakenapps.pcap.Protocol;
import org.krakenapps.pcap.decoder.arp.ArpDecoder;
import org.krakenapps.pcap.decoder.ethernet.EthernetDecoder;
import org.krakenapps.pcap.decoder.ethernet.EthernetType;
import org.krakenapps.pcap.decoder.icmp.IcmpDecoder;
import org.krakenapps.pcap.decoder.icmpv6.Icmpv6Decoder;
import org.krakenapps.pcap.decoder.icmpv6.Icmpv6Processor;
import org.krakenapps.pcap.decoder.ip.InternetProtocol;
import org.krakenapps.pcap.decoder.ip.IpDecoder;
import org.krakenapps.pcap.decoder.ipv6.Ipv6Decoder;
import org.krakenapps.pcap.decoder.tcp.TcpDecoder;
import org.krakenapps.pcap.decoder.tcp.TcpPortProtocolMapper;
import org.krakenapps.pcap.decoder.tcp.TcpProcessor;
import org.krakenapps.pcap.decoder.tcp.TcpSegmentCallback;
import org.krakenapps.pcap.decoder.udp.UdpDecoder;
import org.krakenapps.pcap.decoder.udp.UdpPortProtocolMapper;
import org.krakenapps.pcap.decoder.udp.UdpProcessor;
import org.krakenapps.pcap.file.PcapFileInputStream;
import org.krakenapps.pcap.packet.PcapPacket;

/**
 * @author mindori
 */
public class PcapFileRunner {
	private File dumpFile;

	private EthernetDecoder eth;
	private ArpDecoder arp;
	private IpDecoder ip;
	private Ipv6Decoder ipv6;
	private IcmpDecoder icmp;
	private Icmpv6Decoder icmpv6;
	private TcpDecoder tcp;
	private UdpDecoder udp;

	public PcapFileRunner(File dumpFile) {
		this.dumpFile = dumpFile;

		eth = new EthernetDecoder();
		arp = new ArpDecoder();
		ip = new IpDecoder();
		ipv6 = new Ipv6Decoder();
		icmp = new IcmpDecoder();
		icmpv6 = new Icmpv6Decoder();
		tcp = new TcpDecoder(new TcpPortProtocolMapper());
		udp = new UdpDecoder(new UdpPortProtocolMapper());

		eth.register(EthernetType.IPV4, ip);
		eth.register(EthernetType.IPV6, ipv6);
		eth.register(EthernetType.ARP, arp);

		ip.register(InternetProtocol.ICMP, icmp);
		ip.register(InternetProtocol.TCP, tcp);
		ip.register(InternetProtocol.UDP, udp);

		ipv6.register(InternetProtocol.ICMPV6, icmpv6);
		ipv6.register(InternetProtocol.TCP, tcp);
		ipv6.register(InternetProtocol.UDP, udp);
	}

	public void run() throws IOException {
		PcapFileInputStream is = null;
		try {
			is = new PcapFileInputStream(dumpFile);
			while (true) {
				PcapPacket packet = is.getPacket();
				if (packet == null)
					break;
				eth.decode(packet);
			}
		} catch (EOFException e) {
			// do nothing
		} finally {
			if (is != null)
				is.close();
		}
	}

	public void setTcpProcessor(Protocol protocol, TcpProcessor processor) {
		tcp.getProtocolMapper().register(protocol, processor);
	}

	public void setUdpProcessor(Protocol protocol, UdpProcessor processor) {
		udp.getProtocolMapper().register(protocol, processor);
	}

	public void addTcpCallback(TcpSegmentCallback callback) {
		tcp.registerSegmentCallback(callback);
	}

	public void addIcmpv6Processor(Icmpv6Processor processor) {
		icmpv6.register(processor);
	}

	public EthernetDecoder getEthernetDecoder() {
		return eth;
	}

	public ArpDecoder getArpDecoder() {
		return arp;
	}

	public IpDecoder getIpDecoder() {
		return ip;
	}

	public Ipv6Decoder getIpv6Decoder() {
		return ipv6;
	}

	public IcmpDecoder getIcmpDecoder() { 
		return icmp;
	}
	
	public Icmpv6Decoder getIcmpv6Decoder() {
		return icmpv6;
	}

	public TcpDecoder getTcpDecoder() {
		return tcp;
	}
	
	public UdpDecoder getUdpDecoder() {
		return udp;
	}
}
