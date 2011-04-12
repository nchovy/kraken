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
import java.util.Date;
import java.util.concurrent.TimeoutException;

import org.krakenapps.pcap.decoder.ethernet.EthernetFrame;
import org.krakenapps.pcap.decoder.ethernet.EthernetHeader;
import org.krakenapps.pcap.decoder.ethernet.EthernetType;
import org.krakenapps.pcap.decoder.ethernet.MacAddress;
import org.krakenapps.pcap.decoder.icmp.IcmpPacket;
import org.krakenapps.pcap.decoder.icmp.IcmpProcessor;
import org.krakenapps.pcap.decoder.ip.InternetProtocol;
import org.krakenapps.pcap.decoder.ip.Ipv4Packet;
import org.krakenapps.pcap.live.PcapDevice;
import org.krakenapps.pcap.live.PcapDeviceManager;
import org.krakenapps.pcap.live.PcapDeviceMetadata;
import org.krakenapps.pcap.routing.RoutingEntry;
import org.krakenapps.pcap.routing.RoutingTable;

/**
 * @author xeraph
 */
public class Ping {
	public static IcmpPacket ping(InetAddress targetIp, int timeout) throws IOException, TimeoutException {
		return ping(targetIp, 1, timeout);
	}

	public static IcmpPacket ping(InetAddress targetIp, int seq, int timeout) throws IOException, TimeoutException {
		MacAddress dstMac = Arping.query(targetIp, timeout);
		return ping(dstMac, targetIp, seq, timeout);
	}

	public static IcmpPacket ping(MacAddress dstMac, InetAddress targetIp, int seq, int timeout) throws IOException,
			TimeoutException {
		if (dstMac == null)
			throw new IllegalArgumentException("destination mac should be not null");

		RoutingEntry entry = RoutingTable.findRoute(targetIp);

		PcapDeviceMetadata metadata = PcapDeviceManager.getDeviceMetadata(entry.getInterfaceName());
		if (metadata == null)
			throw new IllegalStateException("route not found for " + targetIp);

		return ping(metadata, entry, dstMac, targetIp, seq, timeout);
	}

	private static IcmpPacket ping(PcapDeviceMetadata metadata, RoutingEntry entry, MacAddress dstMac,
			InetAddress targetIp, int seq, int timeout) throws IOException, TimeoutException {
		InetAddress sourceIp = metadata.getInet4Address();
		MacAddress srcMac = metadata.getMacAddress();

		Buffer buf = buildFrame(entry, srcMac, dstMac, sourceIp, targetIp, seq);
		PcapDevice device = PcapDeviceManager.open(metadata.getName(), timeout);
		
		try {
			device.setFilter("icmp");

			PcapLiveRunner runner = new PcapLiveRunner(device);
			IcmpCallback callback = new IcmpCallback();
			runner.getIcmpDecoder().register(callback);

			// send icmp packet
			device.write(buf);

			long begin = new Date().getTime();

			IcmpPacket last = null;
			while (true) {
				runner.runOnce();
				last = callback.getLast();

				if (last.getType() == 0 && last.getCode() == 0 && last.getId() == 1 && last.getSeq() == seq)
					break;

				long end = new Date().getTime();
				if (end - begin >= timeout)
					break;
			}

			if (last == null)
				throw new TimeoutException("ping timeout for " + targetIp.getHostAddress());

			return last;
		} finally {
			device.close();
		}
	}

	private static Buffer buildFrame(RoutingEntry entry, MacAddress srcMac, MacAddress dstMac, InetAddress sourceIp,
			InetAddress targetIp, int seq) throws IOException {
		IcmpPacket.Builder icmp = new IcmpPacket.Builder().data(new ChainBuffer(
				"abcdefghijklmnopqrstuvwabcdefghi".getBytes()));

		Ipv4Packet ipPacket = new Ipv4Packet.Builder().dst(targetIp).proto(InternetProtocol.ICMP).data(icmp).build();

		EthernetHeader ethernetHeader = new EthernetHeader(srcMac, dstMac, EthernetType.IPV4);

		EthernetFrame frame = new EthernetFrame(ethernetHeader, ipPacket.getBuffer());
		return frame.getBuffer();
	}

	private static class IcmpCallback implements IcmpProcessor {
		private IcmpPacket last;

		@Override
		public void process(IcmpPacket p) {
			last = p;
		}

		public IcmpPacket getLast() {
			return last;
		}
	}

}
