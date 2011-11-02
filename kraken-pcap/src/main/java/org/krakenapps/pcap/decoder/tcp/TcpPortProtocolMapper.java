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
package org.krakenapps.pcap.decoder.tcp;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.krakenapps.pcap.Protocol;

public class TcpPortProtocolMapper implements TcpProtocolMapper {
	private ConcurrentMap<Integer, Protocol> tcpMap;
	private ConcurrentMap<InetSocketAddress, Protocol> temporaryTcpMap;
	private ConcurrentMap<Protocol, Set<TcpProcessor>> tcpProcessorMap;

	public TcpPortProtocolMapper() {
		tcpMap = new ConcurrentHashMap<Integer, Protocol>();
		temporaryTcpMap = new ConcurrentHashMap<InetSocketAddress, Protocol>();
		tcpProcessorMap = new ConcurrentHashMap<Protocol, Set<TcpProcessor>>();

		tcpMap.put(80, Protocol.HTTP);
		tcpMap.put(8080, Protocol.HTTP);
		tcpMap.put(25, Protocol.SMTP);
		tcpMap.put(587, Protocol.SMTP);
		tcpMap.put(110, Protocol.POP3);
		tcpMap.put(1863, Protocol.MSN);
		tcpMap.put(21, Protocol.FTP);
		tcpMap.put(138, Protocol.NETBIOS);
		tcpMap.put(139, Protocol.NETBIOS);
		tcpMap.put(445, Protocol.NETBIOS);
		tcpMap.put(22, Protocol.SSH);
		tcpMap.put(23, Protocol.TELNET);
		tcpMap.put(43, Protocol.WHOIS);
		tcpMap.put(53, Protocol.DNS);
		tcpMap.put(66, Protocol.SQLNET);
		tcpMap.put(79, Protocol.FINGER);
		tcpMap.put(143, Protocol.IMAP);
		tcpMap.put(179, Protocol.BGP);
		tcpMap.put(1433, Protocol.MSSQL);
		tcpMap.put(1434, Protocol.MSSQL);
		tcpMap.put(3306, Protocol.MYSQL);
		tcpMap.put(5432, Protocol.POSTGRES);
	}

	public void register(int port, Protocol protocol) {
		tcpMap.put(port, protocol);
	}

	public void unregister(int port) {
		if (tcpMap.containsKey(port))
			tcpMap.remove(port);
	}

	@Override
	public void register(InetSocketAddress sockAddr, Protocol protocol) {
		temporaryTcpMap.put(sockAddr, protocol);
	}

	@Override
	public void unregister(InetSocketAddress sockAddr) {
		if (temporaryTcpMap.containsKey(sockAddr))
			temporaryTcpMap.remove(sockAddr);
	}

	@Override
	public void register(Protocol protocol, TcpProcessor processor) {
		tcpProcessorMap
				.putIfAbsent(protocol, Collections.newSetFromMap(new ConcurrentHashMap<TcpProcessor, Boolean>()));
		tcpProcessorMap.get(protocol).add(processor);
	}

	@Override
	public void unregister(Protocol protocol, TcpProcessor processor) {
		tcpProcessorMap
				.putIfAbsent(protocol, Collections.newSetFromMap(new ConcurrentHashMap<TcpProcessor, Boolean>()));
		tcpProcessorMap.get(protocol).remove(processor);
	}

	@Deprecated
	@Override
	public void unregister(Protocol protocol) {
		if (tcpProcessorMap.containsKey(protocol))
			tcpProcessorMap.remove(protocol);
	}

	@Override
	public Protocol map(TcpSegment segment) {
		TcpSessionKey key = segment.getSessionKey();
		InetSocketAddress server = new InetSocketAddress(key.getServerIp(), key.getServerPort());
		if (temporaryTcpMap.containsKey(server))
			return temporaryTcpMap.get(server);
		else if (tcpMap.containsKey(key.getServerPort()))
			return tcpMap.get(key.getServerPort());

		return null;
	}

	@Override
	public Collection<TcpProcessor> getTcpProcessors(Protocol protocol) {
		if (protocol == null)
			return null;

		if (tcpProcessorMap.containsKey(protocol)) {
			Set<TcpProcessor> processors = tcpProcessorMap.get(protocol);
			if (processors.size() > 0)
				return processors;
		}
		return null;
	}

	@Deprecated
	@Override
	public TcpProcessor getTcpProcessor(Protocol protocol) {
		if (protocol == null)
			return null;

		if (tcpProcessorMap.containsKey(protocol)) {
			Set<TcpProcessor> processors = tcpProcessorMap.get(protocol);
			if (processors.size() > 0)
				return processors.iterator().next();
		}
		return null;
	}
}