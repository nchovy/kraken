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
package org.krakenapps.pcap.decoder.ipv6;

import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.krakenapps.pcap.decoder.ethernet.EthernetFrame;
import org.krakenapps.pcap.decoder.ethernet.EthernetProcessor;
import org.krakenapps.pcap.util.Buffer;
/**
 * @author xeraph
 */
public class Ipv6Decoder implements EthernetProcessor {
	private Map<Byte, Set<Ipv6Processor>> callbackMap = new HashMap<Byte, Set<Ipv6Processor>>();

	public void register(int nextHeader, Ipv6Processor callback) {
		byte next = (byte) nextHeader;
		if (callbackMap.get(next) == null)
			callbackMap.put(next, new HashSet<Ipv6Processor>());

		Set<Ipv6Processor> set = callbackMap.get(next);
		set.add(callback);
	}

	public void unregister(byte nextHeader, Ipv6Processor callback) {
		Set<Ipv6Processor> set = callbackMap.get(nextHeader);
		if (set != null)
			set.remove(callback);
	}

	@Override
	public void process(EthernetFrame frame) {
		Ipv6Packet p = new Ipv6Packet();
		Buffer data = frame.getData();
		
		byte b1 = data.get();
		byte b2 = data.get();
		short s = data.getShort();
		int payloadLength = data.getUnsignedShort();
		byte nextHeader = data.get();
		int hopLimit = data.get() & 0xFF;

		byte[] source = new byte[16];
		byte[] destination = new byte[16];

		data.gets(source, 0, source.length);
		data.gets(destination, 0, destination.length);

		Inet6Address src = null;
		Inet6Address dst = null;

		try {
			src = (Inet6Address) Inet6Address.getByAddress(source);
			dst = (Inet6Address) Inet6Address.getByAddress(destination);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		byte version = (byte) ((b1 & 0xF0) >> 4);
		if (version != 6)
			return;

		byte trafficClass = (byte) ((b1 & 0x0F) | ((b2 & 0xF0) >> 4));
		int flowLabel = (b2 & 0x0F) << 16 | s;

//		byte[] payload = new byte[payloadLength];
//		data.gets(payload, 0, payloadLength);

		p.setL2Frame(frame);
		p.setTrafficClass(trafficClass);
		p.setFlowLabel(flowLabel);
		p.setPayloadLength(payloadLength);
		p.setNextHeader(nextHeader);
		p.setHopLimit(hopLimit);
		p.setSource(src);
		p.setDestination(dst);
		
//		Buffer b = data.getBuffer();
//		b.discardReadBytes();
		data.discardReadBytes();
		p.setData(data);

		Set<Ipv6Processor> set = callbackMap.get(nextHeader);
		if (set == null)
			return;

		for (Ipv6Processor callback : set) {
			try {
				callback.process(p);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
