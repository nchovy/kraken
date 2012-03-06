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
package org.krakenapps.pcap.decoder.dhcp;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

import org.krakenapps.pcap.Protocol;
import org.krakenapps.pcap.decoder.dhcp.options.DhcpOption;
import org.krakenapps.pcap.decoder.dhcp.options.ParameterRequestListOption;
import org.krakenapps.pcap.decoder.ethernet.MacAddress;
import org.krakenapps.pcap.decoder.udp.UdpPacket;
import org.krakenapps.pcap.decoder.udp.UdpProcessor;
import org.krakenapps.pcap.live.PcapDevice;
import org.krakenapps.pcap.live.PcapDeviceManager;
import org.krakenapps.pcap.live.PcapDeviceMetadata;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.IpConverter;
import org.krakenapps.pcap.util.PcapLiveRunner;

public class DhcpDecoder implements UdpProcessor {
	private Set<DhcpProcessor> callbacks = new HashSet<DhcpProcessor>();

	public void register(DhcpProcessor processor) {
		callbacks.add(processor);
	}

	public void unregister(DhcpProcessor processor) {
		callbacks.remove(processor);
	}

	@Override
	public void process(UdpPacket p) {
		Buffer b = p.getData();
		byte op = b.get();
		byte htype = b.get();
		byte hlen = b.get();
		byte hops = b.get();
		int xid = b.getInt();
		short secs = b.getShort();
		short flags = b.getShort();
		InetAddress clientAddress = IpConverter.toInetAddress(b.getInt());
		InetAddress yourAddress = IpConverter.toInetAddress(b.getInt());
		InetAddress nextServerAddress = IpConverter.toInetAddress(b.getInt());
		InetAddress gatewayAddress = IpConverter.toInetAddress(b.getInt());

		byte[] mac = new byte[6];
		b.gets(mac);
		MacAddress clientMac = new MacAddress(mac);

		byte[] temp = new byte[202];
		b.gets(temp);

		// check magic cookie
		long magicCookie = b.getInt() & 0xffffffffl;
		if (magicCookie != 0x63825363L)
			return;

		DhcpMessage msg = new DhcpMessage();
		msg.setMessageType(op);
		msg.setHardwareType(htype);
		msg.setHardwareAddressLength(hlen);
		msg.setHops(hops);
		msg.setTransactionId(xid);
		msg.setSecs(secs);
		msg.setFlags(flags);
		msg.setClientAddress(clientAddress);
		msg.setYourAddress(yourAddress);
		msg.setNextServerAddress(nextServerAddress);
		msg.setGatewayAddress(gatewayAddress);
		msg.setClientMac(clientMac);

		// parse options
		int remaining = b.readableBytes();
		for (int i = 0; i < remaining;) {
			byte type = b.get();
			if (type == (byte) 0xFF)
				break; // end of option

			int length = b.get() & 0xFF;
			byte[] value = new byte[length];
			b.gets(value);
			DhcpOption option = DhcpOptionParser.create(type, length, value);
			msg.getOptions().add(option);
			i += 2 + length;
		}

		for (DhcpProcessor callback : callbacks) {
			try {
				callback.process(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws Exception {
		for (PcapDeviceMetadata device : PcapDeviceManager.getDeviceMetadataList()) {
			System.out.println(device);
		}

		PcapDeviceMetadata metadata = PcapDeviceManager.getDeviceMetadataList().get(0);
		PcapDevice device = PcapDeviceManager.open(metadata.getName(), Integer.MAX_VALUE);

		DhcpDecoder dhcp = new DhcpDecoder();
		PcapLiveRunner runner = new PcapLiveRunner(device);
		runner.setUdpProcessor(Protocol.DHCP, dhcp);

		dhcp.register(new DhcpProcessor() {

			@Override
			public void process(DhcpMessage msg) {
				StringBuilder sb = new StringBuilder();
				String finger = null;

				int i = 0;
				for (DhcpOption option : msg.getOptions()) {
					if (i != 0)
						sb.append(",");

					sb.append(option.getType());
					if (option instanceof ParameterRequestListOption) {
						ParameterRequestListOption o = (ParameterRequestListOption) option;
						finger = "client ip: " + msg.getClientAddress() + " client mac: " + msg.getClientMac()
								+ " your ip: " + msg.getYourAddress() + " finger => " + o.getFingerprint();
					}

					i++;
				}

				String options = sb.toString();
				System.out.println("options: " + options + ", " + finger);
			}
		});

		runner.run();
	}
}
