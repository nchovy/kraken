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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.pcap.decoder.ip.Ipv4Packet;
import org.krakenapps.pcap.decoder.ip.IpProcessor;
import org.krakenapps.pcap.decoder.ipv6.Ipv6Packet;
import org.krakenapps.pcap.decoder.ipv6.Ipv6Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mindori
 */
public class TcpDecoder implements IpProcessor, Ipv6Processor {
	private static final int SYN_FLAG = 2;
	private static final int SYN_ACK_FLAG = 18;
	private final Logger logger = LoggerFactory.getLogger(TcpDecoder.class.getName());

	private final Map<TcpSessionKey, TcpSession> tcpSessionMap;
	private final TcpPacketRepository repository;
	private final TcpStateTransitMachine transitMachine;
	private final TcpSegmentCallbacks segmentCallbacks;

	public TcpDecoder(TcpProtocolMapper mapper) {
		tcpSessionMap = new HashMap<TcpSessionKey, TcpSession>();
		repository = new TcpPacketRepository(mapper);
		transitMachine = new TcpStateTransitMachine(mapper, tcpSessionMap);
		segmentCallbacks = new TcpSegmentCallbacks();
	}
	
	public Collection<TcpSession> getCurrentSessions() {
		return new ArrayList<TcpSession>(tcpSessionMap.values());
	}

	public void setProtocolMapper(TcpProtocolMapper mapper) {
		transitMachine.setProtocolMapper(mapper);
	}

	public TcpProtocolMapper getProtocolMapper() {
		return transitMachine.getProtocolMapper();
	}

	public void registerSegmentCallback(TcpSegmentCallback callback) {
		segmentCallbacks.register(callback);
	}

	public void unregisterSegmentCallback(TcpSegmentCallback callback) {
		segmentCallbacks.unregister(callback);
	}

	public void dispatchNewTcpSegment(TcpSession session, TcpPacket segment) {
		TcpPacket newTcp = new TcpPacket(segment);
		segmentCallbacks.fireReceiveCallbacks(session, newTcp);
	}

	public void process(Ipv4Packet packet) {
		TcpPacket newTcp = TcpPacket.parse(packet);
		
		if (newTcp.isJumbo()) {
			TcpSessionImpl session = (TcpSessionImpl) tcpSessionMap.get(newTcp.getSessionKey());
			if (session != null) {
				session.deallocateTxBuffer();
				session.deallocateRxBuffer();
				tcpSessionMap.remove(session.getKey());

				logger.error("session terminate: find jumbo packet ");
			}
		} else {
			handle(newTcp);
		}
	}

	@Override
	public void process(Ipv6Packet packet) {
		// TODO: next header handling
		TcpPacket newTcp = TcpPacket.parse(packet);

		if (newTcp.isJumbo()) {
			TcpSessionImpl session = (TcpSessionImpl) tcpSessionMap.get(newTcp.getSessionKey());
			if (session != null) {
				session.deallocateTxBuffer();
				session.deallocateRxBuffer();
				tcpSessionMap.remove(session.getKey());

				logger.error("session terminate: find jumbo packet ");
			}
		} else {
			handle(newTcp);
		}
	}

	private void handle(TcpPacket newTcp) {
		TcpSessionImpl session = (TcpSessionImpl) repository.storePacket(tcpSessionMap, newTcp);
		if (session == null) {
			dispatchNewTcpSegment(null, newTcp);
			return;
		}

		newTcp.setDirection(session);

		if ((newTcp.getFlags() == SYN_FLAG) || (newTcp.getFlags() == SYN_ACK_FLAG))
			branchWithOptions(newTcp, session);
		else {
			if (newTcp.getDirection() == TcpDirection.ToServer)
				branch(session.getSourceOption(), newTcp, session);
			else
				branch(session.getDestinationOption(), newTcp, session);
		}

		if (logger.isDebugEnabled())
			logger.debug(newTcp.toString());
	}

	private void branchWithOptions(TcpPacket newTcp, TcpSession session) {
		// Normal or SACK
		if (newTcp.getOptions() == null)
			transitMachine.transitState(this, newTcp, (TcpSessionImpl) session);
		else
			findSackScanOptions(newTcp, (TcpSessionImpl) session, 0);
	}

	private void findSackScanOptions(TcpPacket newTcp, TcpSessionImpl session, int offset) {
		if (newTcp.getOptions().length > offset) {
			switch (newTcp.getOptions()[offset]) {
			case 0:
			case 1:
				findSackScanOptions(newTcp, session, offset + 1);
				break;
			case 2:
				findSackScanOptions(newTcp, session, offset + 4);
				break;
			case 3:
			case 14:
				findSackScanOptions(newTcp, session, offset + 3);
				break;
			case 8:
				findSackScanOptions(newTcp, session, offset + 10);
				break;
			case 4:
				if (newTcp.getDirection() == TcpDirection.ToServer)
					session.setDestStreamOption(TcpStreamOption.SACK);
				else
					session.setSrcStreamOption(TcpStreamOption.SACK);

				if (logger.isDebugEnabled())
					logger.debug("tcp S/ACK permitted.");

				transitMachine.transitStateSACK(this, newTcp, session);
				break;
			default:
				transitMachine.transitState(this, newTcp, session);
				break;
			}
		} else {
			transitMachine.transitState(this, newTcp, session);
		}
	}

	private void branch(TcpStreamOption options, TcpPacket newTcp, TcpSessionImpl session) {
		if (options == TcpStreamOption.SACK) {
			transitMachine.transitStateSACK(this, newTcp, session);
		} else {
			transitMachine.transitState(this, newTcp, session);
		}
	}
}
