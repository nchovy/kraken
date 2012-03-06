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
package org.krakenapps.pcap.decoder.arp;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import org.krakenapps.pcap.decoder.ethernet.EthernetFrame;
import org.krakenapps.pcap.decoder.ethernet.EthernetProcessor;
import org.krakenapps.pcap.decoder.ethernet.MacAddress;
import org.krakenapps.pcap.util.Buffer;

/**
 * ARP decoder. Register {@link ArpProcessor} for receiving {@link ArpPacket}.
 * 
 * @author xeraph
 * @since 1.1
 */
public class ArpDecoder implements EthernetProcessor {
	private Set<ArpProcessor> callbacks = new HashSet<ArpProcessor>();

	public void register(ArpProcessor callback) {
		callbacks.add(callback);
	}

	public void unregister(ArpProcessor callback) {
		callbacks.remove(callback);
	}	

	@Override
	public void process(EthernetFrame frame) {
		ArpPacket p = new ArpPacket();
		Buffer data = frame.getData();

		p.setL2Frame(frame);
		p.setHardwareType(data.getShort());
		p.setProtocolType(data.getShort());
		p.setHardwareSize(data.get());
		p.setProtocolSize(data.get());
		p.setOpcode(data.getShort());

		if (p.getHardwareSize() == 6 && p.getProtocolSize() == 4 && p.getHardwareType() == 1
				&& p.getProtocolType() == (short) 0x0800) {
			try {
				byte[] senderMac = new byte[6];
				byte[] senderIp = new byte[4];
				byte[] targetMac = new byte[6];
				byte[] targetIp = new byte[4];

				data.gets(senderMac, 0, 6);
				data.gets(senderIp, 0, 4);
				data.gets(targetMac, 0, 6);
				data.gets(targetIp, 0, 4);
				
				p.setSenderMac(new MacAddress(senderMac));
				p.setSenderIp(Inet4Address.getByAddress(senderIp));
				p.setTargetMac(new MacAddress(targetMac));
				p.setTargetIp(Inet4Address.getByAddress(targetIp));

				/* manipulate arp packet from outside */
				for (ArpProcessor callback : callbacks) {
					try {
						callback.process(ArpPacket.copyArpPacket(p));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				for (ArpProcessor callback : callbacks) {
					try {
						callback.process(p);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (UnknownHostException e) {
			}
		}
	}
}
