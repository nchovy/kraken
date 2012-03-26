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
package org.krakenapps.pcap.decoder.udp;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

import org.krakenapps.pcap.Protocol;
import org.krakenapps.pcap.decoder.ip.Ipv4Packet;
import org.krakenapps.pcap.decoder.ip.IpProcessor;
import org.krakenapps.pcap.decoder.ipv6.Ipv6Packet;
import org.krakenapps.pcap.decoder.ipv6.Ipv6Processor;
import org.krakenapps.pcap.util.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mindori
 */
public class UdpDecoder implements IpProcessor, Ipv6Processor {
	private final Logger logger = LoggerFactory.getLogger(UdpDecoder.class.getName());
	private CopyOnWriteArraySet<UdpProcessor> callbacks = new CopyOnWriteArraySet<UdpProcessor>();
	private UdpProtocolMapper protocolMapper;

	public UdpDecoder(UdpProtocolMapper protocolMapper) {
		this.protocolMapper = protocolMapper;
	}

	public UdpProtocolMapper getProtocolMapper() {
		return protocolMapper;
	}

	public void setProtocolMapper(UdpProtocolMapper protocolMapper) {
		this.protocolMapper = protocolMapper;
	}

	public void registerUdpProcessor(UdpProcessor processor) {
		callbacks.add(processor);
	}

	public void unregisterUdpProcessor(UdpProcessor processor) {
		callbacks.remove(processor);
	}

	public void process(Ipv4Packet packet) {
		Buffer b = packet.getData();
		int sourcePort = b.getUnsignedShort();
		int destinationPort = b.getUnsignedShort();

		UdpPacket newUdp = new UdpPacket(packet, sourcePort, destinationPort);
		newUdp.setLength(b.getUnsignedShort());
		newUdp.setChecksum(b.getUnsignedShort());

		if (logger.isDebugEnabled())
			logger.debug(newUdp.toString());

		b.discardReadBytes();
		newUdp.setData(b);

		dispatch(newUdp);
	}

	@Override
	public void process(Ipv6Packet p) {
		Buffer b = p.getData();
		int sourcePort = b.getUnsignedShort();
		int destinationPort = b.getUnsignedShort();
		int length = b.getUnsignedShort();
		short checksum = b.getShort();

		UdpPacket pkt = new UdpPacket(p, sourcePort, destinationPort);
		pkt.setLength(length);
		pkt.setChecksum(checksum);

		b.discardReadBytes();
		pkt.setData(b);

		dispatch(pkt);
	}

	private void dispatch(UdpPacket newUdp) {
		/* manipulate udp packet from outside */
		for (UdpProcessor callback : callbacks) {
			try {
				callback.process(new UdpPacket(newUdp));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Protocol protocol = protocolMapper.map(newUdp);
		if (protocol == null)
			return;

		Collection<UdpProcessor> processors = protocolMapper.getUdpProcessors(protocol);
		if (processors != null) {
			for (UdpProcessor processor : processors) {
				try {
					processor.process(newUdp);
				} catch (Exception e) {
					logger.warn("kraken pcap: udp processor should now throw any exception", e);
				}
			}
		}
	}
}