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
package org.krakenapps.pcap.decoder.icmpv6;

import java.util.HashSet;
import java.util.Set;

import org.krakenapps.pcap.decoder.ipv6.Ipv6Packet;
import org.krakenapps.pcap.decoder.ipv6.Ipv6Processor;
import org.krakenapps.pcap.util.Buffer;

public class Icmpv6Decoder implements Ipv6Processor {
	private Set<Icmpv6Processor> callbacks = new HashSet<Icmpv6Processor>();

	public void register(Icmpv6Processor callback) {
		callbacks.add(callback);
	}

	public void unregister(Icmpv6Processor callback) {
		callbacks.remove(callback);
	}

	@Override
	public void process(Ipv6Packet p) {
		Buffer data = p.getData();

		Icmpv6Packet icmp = new Icmpv6Packet();
		icmp.setIpPacket(p);
		icmp.setSource(p.getSourceAddress());
		icmp.setDestination(p.getDestinationAddress());
		icmp.setType(data.get() & 0xff);
		icmp.setCode(data.get() & 0xff);
		icmp.setChecksum(data.getShort());
		data.discardReadBytes();
		icmp.setData(data);

		for (Icmpv6Processor callback : callbacks) {
			try {
				callback.process(icmp);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
