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
import java.net.InetAddress;
import java.util.concurrent.TimeoutException;

import org.krakenapps.pcap.Injectable;
import org.krakenapps.pcap.decoder.ethernet.EthernetFrame;
import org.krakenapps.pcap.decoder.ip.Ipv4Packet;
import org.krakenapps.pcap.decoder.tcp.TcpPacket;
import org.krakenapps.pcap.decoder.udp.UdpPacket;
import org.krakenapps.pcap.live.PcapDevice;
import org.krakenapps.pcap.live.PcapDeviceManager;
import org.krakenapps.pcap.live.PcapDeviceMetadata;

public class PacketManipulator {
	public static void main(String[] args) throws IOException, TimeoutException {
		System.loadLibrary("kpcap");

		InetAddress target = InetAddress.getByName("nchovy.kr");
		send(IP().data(TCP().syn().dst(target, 80)));
	}

	public static void broadcast(EthernetFrame.Builder eth) throws IOException {

		for (PcapDeviceMetadata metadata : PcapDeviceManager.getDeviceMetadataList()) {
			PcapDevice device = PcapDeviceManager.open(metadata.getName(), 1000);
			eth.src(device.getMetadata().getMacAddress());

			try {
				send(device, eth);
			} finally {
				device.close();
			}
		}
	}

	public static void send(PcapDevice device, EthernetFrame.Builder eth) throws IOException {
		Buffer buf = eth.build().getBuffer();
		device.write(buf);
	}

	public static void send(Ipv4Packet.Builder ip) throws IOException {
		EthernetFrame.Builder eth = new EthernetFrame.Builder().data(ip);
		InetAddress dstIp = (InetAddress) eth.getDefault("dst_ip");
		PcapDevice device = PcapDeviceManager.openFor(dstIp, 1000);
		try {
			if (eth.getDefault("src_mac") == null)
				eth.src(device.getMetadata().getMacAddress());

			Injectable injectable = eth.build();
			device.write(injectable.getBuffer());
		} finally {
			if (device != null)
				device.close();
		}
	}

	public static EthernetFrame.Builder ETH() {
		return new EthernetFrame.Builder();
	}

	public static Ipv4Packet.Builder IP() {
		return new Ipv4Packet.Builder();
	}

	public static TcpPacket.Builder TCP() {
		return new TcpPacket.Builder();
	}
	
	public static UdpPacket.Builder UDP() {
		return new UdpPacket.Builder();
	}
}
