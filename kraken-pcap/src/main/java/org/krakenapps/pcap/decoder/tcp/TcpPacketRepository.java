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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.krakenapps.pcap.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mindori
 */
public class TcpPacketRepository {
	private static final int FIN = 1;
	private static final int SYN = 2;
	private static final int RST = 4;
	private static final int ACK = 16;
	private static final int FIN_ACK = 17;
	private static final int SYN_ACK = 18;
	private static final int RST_ACK = 20;
	private static final int FIN_PSH_ACK = 25;

	private final Logger logger = LoggerFactory.getLogger(TcpPacketRepository.class.getName());

	private final TcpProtocolMapper mapper;

	public TcpPacketRepository(TcpProtocolMapper mapper) {
		this.mapper = mapper;
	}

	public TcpSession storePacket(Map<TcpSessionKey, TcpSession> tcpSessionMap, TcpPacket segment) {
		switch (segment.getFlags()) {
		case SYN:
			if (!duplicatedCheck(tcpSessionMap, segment))
				return onSynSent(tcpSessionMap, segment);
			else {
				return null;
			}
		case RST:
		case RST_ACK:
			TcpSessionKey key = segment.getSessionKey();
			TcpSession session = tcpSessionMap.get(key);
			if (session != null) {
				resetArrived(tcpSessionMap, (TcpSessionImpl) session, segment, key);
				return session;
			}

			if (logger.isDebugEnabled())
				logger.debug("This packet is a garbage.");

			return null;

		default:
			return storeToSession(tcpSessionMap, segment);
		}
	}

	private boolean duplicatedCheck(Map<TcpSessionKey, TcpSession> tcpStreamMap, TcpPacket segment) {
		TcpSessionKey key = segment.getSessionKey();
		TcpSession stream = tcpStreamMap.get(key);
		if (stream != null)
			return compareSegment((TcpSessionImpl) stream, segment);

		return false;
	}

	private TcpSession onSynSent(Map<TcpSessionKey, TcpSession> tcpStreamMap, TcpPacket segment) {
		TcpSession newSession = createSession(segment);
		tcpStreamMap.put(newSession.getKey(), newSession);

		if (logger.isDebugEnabled())
			logger.debug("Create a list.(SYN)");

		return newSession;
	}

	private TcpSessionImpl createSession(TcpPacket s) {
		TcpSessionImpl session = new TcpSessionImpl(mapper);
		TcpSessionKeyImpl key = new TcpSessionKeyImpl(s.getSourceAddress(), s.getDestinationAddress(),
				s.getSourcePort(), s.getDestinationPort());

		session.setTcpSessionKey(key);
		session.setSrcFirstSeq(s.getSeq());
		session.addPacket(s);
		return session;
	}

	private TcpSession storeToSession(Map<TcpSessionKey, TcpSession> tcpStreamMap, TcpPacket segment) {
		TcpSessionKey key = segment.getSessionKey();
		if (tcpStreamMap.containsKey(key) && !duplicatedCheck(tcpStreamMap, segment)) {
			TcpSessionImpl session = (TcpSessionImpl) tcpStreamMap.get(key);
			store(session, segment);
			return session;
		}
		return null;
	}

	private void store(TcpSessionImpl stream, TcpPacket segment) {
		stream.getKey().equals(segment.getSessionKey());

		if (segment.getFlags() == SYN_ACK) {
			stream.setDestFirstSeq(segment.getSeq());
			stream.addPacket(segment);
		} else {
			if (segment.getFlags() != ACK)
				stream.addPacket(segment);
		}
	}

	private void resetArrived(Map<TcpSessionKey, TcpSession> tcpStreamMap, TcpSessionImpl session,
			TcpPacket segment, TcpSessionKey key) {
		int relativeSeq = convertRelativeSeq(session, segment);

		int currentSeq;
		if (segment.getDirection() == TcpDirection.ToServer)
			currentSeq = session.getRxBuffer().getCurrentSeq();
		else
			currentSeq = session.getTxBuffer().getCurrentSeq();

		if ((currentSeq == relativeSeq) || ((currentSeq + 1) == relativeSeq)) {
			Protocol protocol = session.getProtocol();
			Collection<TcpProcessor> processors = mapper.getTcpProcessors(protocol);
			
			if(processors == null)
				return;
			
			for(TcpProcessor p: processors) {
				p.onReset(session.getKey());
			}
			
			tcpStreamMap.remove(key);

			if (logger.isDebugEnabled())
				logger.debug("Session closed(Reason : RST)");
		}
	}

	private boolean compareSegment(TcpSessionImpl stream, TcpPacket segment) {
		if (segment.getFlags() != SYN_ACK) {
			int currentSeq = convertRelativeSeq(stream, segment);
			int currentAck = convertRelativeAck(stream, segment);
			int currentDataLength = 0;
			if (segment.getData() != null)
				currentDataLength = segment.getDataLength();

			List<TcpPacket> pList = stream.getPacketList();
			for (TcpPacket s : pList) {
				if (currentSeq == s.getRelativeSeq() && currentAck == s.getRelativeAck()
						&& segment.getFlags() == s.getFlags()) {
					if (s.getData() == null) {
						if (currentDataLength == 0) {
							return true;
						}
					} else {
						if (currentDataLength == s.getDataLength()) {
							return true;
						}
					}
				}
			}

			if (stream.getKey().getClientPort() == segment.getSourcePort()) {
				if (currentSeq == stream.getRecentSrcSeq() && currentAck == stream.getRecentSrcAck()
						&& currentDataLength == stream.getRecentSrcDataLength()
						&& segment.getFlags() == stream.getRecentSrcFlags()) {
					return true;
				}
			} else {
				if (currentSeq == stream.getRecentDestSeq() && currentAck == stream.getRecentDestAck()
						&& currentDataLength == stream.getRecentDestDataLength()
						&& segment.getFlags() == stream.getRecentDestFlags()) {
					return true;
				}
			}

			int flags = segment.getFlags();
			if (flags == FIN || flags == SYN || flags == RST || flags == FIN_ACK || flags == RST_ACK
					|| flags == FIN_PSH_ACK)
				currentDataLength += 1;

			int lastAck;
			if (stream.getKey().getClientPort() == segment.getSourcePort())
				lastAck = stream.getDestLastAck();
			else
				lastAck = stream.getSrcLastAck();

			if (currentSeq + currentDataLength < lastAck) {
				return true;
			}
		} else {
			if (stream.getClientState() == TcpState.ESTABLISHED && stream.getServerState() == TcpState.SYN_RCVD) {
				return true;
			}
		}
		return false;
	}

	private int convertRelativeSeq(TcpSessionImpl stream, TcpPacket segment) {
		if (segment.getSourcePort() == stream.getKey().getClientPort()) {
			return (stream.retRelativeSrcSeq(segment.getSeq()));
		} else {
			return (stream.retRelativeDestSeq(segment.getSeq()));
		}
	}

	private int convertRelativeAck(TcpSessionImpl stream, TcpPacket segment) {
		if (segment.getSourcePort() == stream.getKey().getClientPort()) {
			return segment.getAck() - stream.getDestFirstSeq();
		} else {
			return segment.getAck() - stream.getSrcFirstSeq();
		}
	}
}
