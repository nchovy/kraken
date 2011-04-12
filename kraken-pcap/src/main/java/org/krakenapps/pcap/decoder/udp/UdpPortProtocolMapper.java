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

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.krakenapps.pcap.Protocol;

public class UdpPortProtocolMapper implements UdpProtocolMapper {
	private ConcurrentMap<Integer, Protocol> udpMap;
	private ConcurrentMap<InetSocketAddress, Protocol> temporaryUdpMap;
	private ConcurrentMap<Protocol, Set<UdpProcessor>> udpProcessorMap;

	public UdpPortProtocolMapper() {
		udpMap = new ConcurrentHashMap<Integer, Protocol>();
		temporaryUdpMap = new ConcurrentHashMap<InetSocketAddress, Protocol>();
		udpProcessorMap = new ConcurrentHashMap<Protocol, Set<UdpProcessor>>();

		udpMap.put(67, Protocol.DHCP);
		udpMap.put(68, Protocol.DHCP);
		udpMap.put(161, Protocol.SNMP);
		udpMap.put(162, Protocol.SNMP);
		udpMap.put(137, Protocol.NETBIOS);
		udpMap.put(138, Protocol.NETBIOS);
		udpMap.put(53, Protocol.DNS);
		udpMap.put(69, Protocol.TFTP);
		udpMap.put(123, Protocol.NTP);
		udpMap.put(514, Protocol.SYSLOG);
	}

	public void register(int port, Protocol protocol) {
		udpMap.put(port, protocol);
	}

	public void unregister(int port) {
		if (udpMap.containsKey(port))
			udpMap.remove(port);
	}

	@Override
	public void register(Protocol protocol, UdpProcessor processor) {
		udpProcessorMap.putIfAbsent(protocol, Collections.newSetFromMap(new ConcurrentHashMap<UdpProcessor, Boolean>()));
		udpProcessorMap.get(protocol).add(processor);
	}

	@Override
	public void unregister(Protocol protocol, UdpProcessor processor) {
		udpProcessorMap.putIfAbsent(protocol, Collections.newSetFromMap(new ConcurrentHashMap<UdpProcessor, Boolean>()));
		udpProcessorMap.get(protocol).remove(processor);
	}

	@Override
	public void registerTemporaryMapping(InetSocketAddress sockAddr, Protocol protocol) {
		temporaryUdpMap.put(sockAddr, protocol);
	}

	@Override
	public void unregisterTemporaryMapping(InetSocketAddress sockAddr) {
		if (temporaryUdpMap.containsKey(sockAddr))
			temporaryUdpMap.remove(sockAddr);
	}

	@Deprecated
	@Override
	public void unregister(Protocol protocol) {
		if (udpProcessorMap.containsKey(protocol))
			udpProcessorMap.remove(protocol);
	}

	@Override
	public Protocol map(UdpPacket packet) {
		int port = packet.getDestinationPort();

		if (temporaryUdpMap.containsKey(packet.getDestination()))
			return temporaryUdpMap.get(packet.getDestination());
		else if (temporaryUdpMap.containsKey(packet.getSource()))
			return temporaryUdpMap.get(packet.getSource());
		else if (udpMap.containsKey(port))
			return udpMap.get(port);

		return null;
	}

	@Override
	public Collection<UdpProcessor> getUdpProcessors(Protocol protocol) {
		if (protocol == null)
			return null;

		if (udpProcessorMap.containsKey(protocol))
			return udpProcessorMap.get(protocol);

		return null;
	}

	@Deprecated
	@Override
	public UdpProcessor getUdpProcessor(Protocol protocol) {
		if (protocol == null)
			return null;

		if (udpProcessorMap.containsKey(protocol)) {
			Set<UdpProcessor> set = udpProcessorMap.get(protocol);
			if (set.size() > 0)
				return set.iterator().next();
		}
		return null;
	}
}