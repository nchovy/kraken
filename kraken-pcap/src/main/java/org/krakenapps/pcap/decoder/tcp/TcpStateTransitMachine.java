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
import java.util.Map;

import org.krakenapps.pcap.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mindori
 */
public class TcpStateTransitMachine {
	private final Logger logger = LoggerFactory.getLogger(TcpStateTransitMachine.class.getName());
	private static final int FIN = 1;
	private static final int SYN = 2;
	private static final int ACK = 16;
	private static final int FIN_ACK = 17;
	private static final int SYN_ACK = 18;
	private static final int PSH_ACK = 24;
	private static final int FIN_PSH_ACK = 25;

	private final TcpTransitionMapper tcpTransitionMap;

	private Map<TcpSessionKey, TcpSession> tcpSessionMap;
	private TcpProtocolMapper mapper;

	public TcpStateTransitMachine(TcpProtocolMapper mapper, Map<TcpSessionKey, TcpSession> tcpSessionMap) {
		this.tcpTransitionMap = new TcpTransitionMapper();
		this.mapper = mapper;
		this.tcpSessionMap = tcpSessionMap;
	}

	public void transitState(TcpDecoder tcp, TcpPacket segment, TcpSessionImpl session) {
		/*
		 * 1. calculate relative seq / ack. 2. check packet's order. 3. update
		 * client_state, server_state.
		 */
		setRelativeNumbers(session, segment);
		tcp.dispatchNewTcpSegment(session, segment);

		if (isValidSeq(session, segment)) {
			/* received segment(include first data) => register tcp processor */
			if (!session.isReceiveData() && isReceiveData(session, segment))
				registerTcpProcessor(session, segment);

			setNextState(segment, session, tcpSessionMap);
			handleSegment(segment, session);
		}

		else {
			terminateSession(session);
			if (logger.isDebugEnabled())
				logger.debug("Message : Terminate to session.");
		}
	}

	public void transitStateSACK(TcpDecoder tcp, TcpPacket segment, TcpSessionImpl session) {
		setRelativeNumbers(session, segment);
		tcp.dispatchNewTcpSegment(session, segment);

		int flags = segment.getFlags();

		/* 3-way handshaking */
		if (flags == SYN || flags == SYN_ACK || isResponseSynAck(segment)) {
			if (session.getClientState().compareTo(TcpState.ESTABLISHED) >= 0
					&& session.getServerState().compareTo(TcpState.ESTABLISHED) >= 0) {
				return;
			}

			if (isValidSeq(session, segment)) {
				setNextState(segment, session, tcpSessionMap);
				handleSegment(segment, session);
			}

			else {
				terminateSession(session);
				if (logger.isDebugEnabled())
					logger.debug("Message : Terminate to session.");
			}
		}

		else {
			/* received segment(include first data) => register tcp processor */
			if (!session.isReceiveData() && isReceiveData(session, segment))
				registerTcpProcessor(session, segment);

			if (segment.getDirection() == TcpDirection.ToServer) {
				/* enqueue segment */
				session.getTxWaitQueue().enqueue(segment);
				/* retrieve packet */
				TcpPacket element = session.getTxBuffer().retrieveNextPacket(tcp, session, session.getTxWaitQueue());
				/* update state */
				if (element != null) {
					setNextState(element, session, tcpSessionMap);
				}
			}

			else {
				session.getRxWaitQueue().enqueue(segment);
				TcpPacket element = session.getRxBuffer().retrieveNextPacket(tcp, session, session.getRxWaitQueue());
				if (element != null) {
					setNextState(element, session, tcpSessionMap);
				}
			}
		}
	}

	private void setRelativeNumbers(TcpSessionImpl session, TcpPacket segment) {
		switch (segment.getFlags()) {
		case SYN:
			segment.setRelativeSeq(session.retRelativeSrcSeq(segment.getSeq()));
			break;
		case SYN_ACK:
			segment.setRelativeSeq(session.retRelativeDestSeq(segment.getSeq()));
			segment.setRelativeAck(session.retRelativeSrcSeq(segment.getAck()));
			break;
		default:
			if (segment.getSourcePort() == session.getKey().getClientPort()) {
				segment.setRelativeSeq(session.retRelativeSrcSeq(segment.getSeq()));
				segment.setRelativeAck(session.retRelativeDestSeq(segment.getAck()));

			} else {
				segment.setRelativeSeq(session.retRelativeDestSeq(segment.getSeq()));
				segment.setRelativeAck(session.retRelativeSrcSeq(segment.getAck()));
			}
			break;
		}
	}

	private boolean isValidSeq(TcpSessionImpl session, TcpPacket segment) {
		/* rx : client side, tx: server side */
		SegmentBuffer rxBuffer = session.getRxBuffer();
		SegmentBuffer txBuffer = session.getTxBuffer();

		// case 1. Two sides is different.
		int currSeq = segment.getRelativeSeq();
		int currAck = segment.getRelativeAck();

		if (segment.getDirection() == TcpDirection.ToServer) {
			if (currAck == rxBuffer.getCurrentSeq() + rxBuffer.getCurrentDataLength()
					&& (currSeq == rxBuffer.getCurrentAck())) {
				return true;
			}
		} else {
			if ((currAck == txBuffer.getCurrentSeq() + txBuffer.getCurrentDataLength())
					&& (currSeq == txBuffer.getCurrentAck())) {
				return true;
			}
		}

		// case 2. Two sides is same.
		if (segment.getDirection() == TcpDirection.ToServer) {
			if (currSeq == txBuffer.getCurrentSeq() + txBuffer.getCurrentDataLength())
				return true;
		} else {
			if (currSeq == rxBuffer.getCurrentSeq() + rxBuffer.getCurrentDataLength())
				return true;
		}

		// case 3. Simultaneous close.
		if (segment.getDirection() == TcpDirection.ToServer) {
			if (currSeq == rxBuffer.getCurrentAck() && currAck == rxBuffer.getCurrentSeq())
				return true;
		} else {
			if (currSeq == txBuffer.getCurrentAck() && currAck == txBuffer.getCurrentSeq())
				return true;
		}

		// case 4. Simultaneous close(2).
		if (segment.getDirection() == TcpDirection.ToServer) {
			// +1 : FIN packet length.
			if ((currSeq == txBuffer.getCurrentSeq() + txBuffer.getCurrentDataLength() + 1)
					&& (currAck == rxBuffer.getCurrentSeq() + rxBuffer.getCurrentDataLength() + 1))
				return true;
		} else {
			if ((currSeq == rxBuffer.getCurrentSeq() + rxBuffer.getCurrentDataLength() + 1)
					&& (currAck == txBuffer.getCurrentSeq() + txBuffer.getCurrentDataLength() + 1))
				return true;
		}

		// case 5. Simultaneous close(3).
		if (segment.getDirection() == TcpDirection.ToServer) {
			if (currSeq == rxBuffer.getCurrentAck() && currAck == rxBuffer.getCurrentSeq())
				return true;

		} else {
			if (currSeq == txBuffer.getCurrentAck() && currAck == txBuffer.getCurrentSeq())
				return true;
		}
		return false;
	}

	private void setNextState(TcpPacket segment, TcpSessionImpl session, Map<TcpSessionKey, TcpSession> tcpStreamMap) {
		TcpState clientState = session.getClientState();
		TcpState serverState = session.getServerState();

		switch (segment.getFlags()) {
		case FIN:
		case FIN_ACK:
		case FIN_PSH_ACK:
			session.setPacketCountAfterFin(session.getPacketCountAfterFin() + 1);

			if (session.getPacketCountAfterFin() == 1) {
				if (segment.getDirection() == TcpDirection.ToServer)
					clientState = tcpTransitionMap.map(segment, clientState, Action.SEND_FIN);
				else
					serverState = tcpTransitionMap.map(segment, serverState, Action.SEND_FIN);
				session.setFirstFinSeq(segment.getRelativeSeq());
				session.setFirstFinAck(segment.getRelativeAck());
			}

			else if (session.getPacketCountAfterFin() == 2) {
				if (segment.getDirection() == TcpDirection.ToServer) {
					if ((segment.getRelativeSeq() == session.getFirstFinAck())
							&& (segment.getRelativeAck() == session.getFirstFinSeq())) {
						clientState = tcpTransitionMap.map(segment, clientState, Action.SEND_FIN);
					}

					else {
						clientState = tcpTransitionMap.map(segment, clientState, Action.RECV_FIN);
						serverState = tcpTransitionMap.map(segment, serverState, Action.RECV_FINACKED);
						clientState = tcpTransitionMap.map(segment, clientState, Action.SEND_FIN);

					}
				}

				else {
					if ((segment.getRelativeSeq() == session.getFirstFinAck())
							&& (segment.getRelativeAck() == session.getFirstFinSeq())) {
						serverState = tcpTransitionMap.map(segment, serverState, Action.SEND_FIN);
					} else {
						serverState = tcpTransitionMap.map(segment, serverState, Action.RECV_FIN);
						clientState = tcpTransitionMap.map(segment, clientState, Action.RECV_FINACKED);
						serverState = tcpTransitionMap.map(segment, serverState, Action.SEND_FIN);
					}
				}
			}

			else if (session.getPacketCountAfterFin() == 3) {
				if (segment.getDirection() == TcpDirection.ToServer) {
					clientState = tcpTransitionMap.map(segment, clientState, Action.SEND_FIN);
					serverState = tcpTransitionMap.map(segment, serverState, Action.RECV_FIN);
				} else {
					clientState = tcpTransitionMap.map(segment, clientState, Action.RECV_FIN);
					serverState = tcpTransitionMap.map(segment, serverState, Action.SEND_FIN);
				}
			}
			break;

		case SYN:
			clientState = tcpTransitionMap.map(segment, clientState, Action.SEND_SYN);
			serverState = tcpTransitionMap.map(segment, serverState, Action.RECV_SYN);
			break;

		case ACK:
		case PSH_ACK:
			if (session.getPacketCountAfterFin() >= 1) {
				// Except case.
				if (session.getFirstFinSeq() == segment.getRelativeAck()
						&& session.getFirstFinAck() == segment.getRelativeSeq())
					return;
				else if (session.getFirstFinSeq() > segment.getRelativeAck())
					return;
				session.setPacketCountAfterFin(session.getPacketCountAfterFin() + 1);
			}

			if (serverState == TcpState.SYN_RCVD)
				serverState = tcpTransitionMap.map(segment, serverState, Action.RECV_SYNACKED);

			if (session.getPacketCountAfterFin() == 2) {
				if (segment.getDirection() == TcpDirection.ToServer) {
					clientState = tcpTransitionMap.map(segment, clientState, Action.RECV_FIN);
					serverState = tcpTransitionMap.map(segment, serverState, Action.RECV_ACKED);
				} else {
					serverState = tcpTransitionMap.map(segment, serverState, Action.RECV_FIN);
					clientState = tcpTransitionMap.map(segment, clientState, Action.RECV_ACKED);
				}
			}

			else if (session.getPacketCountAfterFin() == 3) {
				if (segment.getDirection() == TcpDirection.ToServer) {
					if (serverState == TcpState.LAST_ACK)
						serverState = tcpTransitionMap.map(segment, serverState, Action.RECV_FINACKED);
					else
						// simultaneous close.
						clientState = tcpTransitionMap.map(segment, clientState, Action.RECV_FIN);
				}

				else {
					if (clientState == TcpState.LAST_ACK)
						clientState = tcpTransitionMap.map(segment, clientState, Action.RECV_FINACKED);
					else
						// simultaneous close.
						serverState = tcpTransitionMap.map(segment, serverState, Action.RECV_FIN);
				}
			}

			else if (session.getPacketCountAfterFin() == 4) {
				if (segment.getDirection() == TcpDirection.ToServer) {
					serverState = tcpTransitionMap.map(segment, serverState, Action.RECV_FINACKED);
					clientState = tcpTransitionMap.map(segment, clientState, Action.RECV_FIN);
				} else {
					clientState = tcpTransitionMap.map(segment, clientState, Action.RECV_FINACKED);
					serverState = tcpTransitionMap.map(segment, serverState, Action.RECV_FIN);
				}
			}
			break;

		case SYN_ACK:
			clientState = tcpTransitionMap.map(segment, clientState, Action.RECV_SYNACKED);
			break;
		}
		setTcpState(session, clientState, serverState);
		if (clientState == TcpState.CLOSED || serverState == TcpState.CLOSED) {
			endSession(segment, session);
		}
	}

	private void handleSegment(TcpPacket segment, TcpSessionImpl session) {
		TcpState clientState = session.getClientState();
		TcpState serverState = session.getServerState();

		if (clientState == TcpState.SYN_SENT) {
			session.getTxBuffer().initTxBuffer(segment);
		}

		else if (clientState == TcpState.ESTABLISHED && serverState == TcpState.SYN_RCVD) {
			session.getRxBuffer().initRxBuffer(segment);
			session.setDestLastAck(1);
			session.setRecentDest(0, 1, 0, 18);
		}

		else if (clientState == TcpState.ESTABLISHED && serverState == TcpState.ESTABLISHED) {
			if (session.isEstablished() == false) {
				session.setEstablished(true);
				session.setSrcLastAck(1);
				session.setRecentSrc(1, 1, 0, 16);
			}
			boolean flushBit = false;
			if (segment.getFlags() == PSH_ACK)
				flushBit = true;

			if (segment.getDirection() == TcpDirection.ToServer)
				session.getTxBuffer().store(session, session.getTxWaitQueue(), segment, flushBit);
			else
				session.getRxBuffer().store(session, session.getRxWaitQueue(), segment, flushBit);
		}

		else if (clientState.compareTo(TcpState.ESTABLISHED) > 0 || serverState.compareTo(TcpState.ESTABLISHED) > 0) {
			if (clientState == TcpState.CLOSED || serverState == TcpState.CLOSED) {
				endSession(segment, session);

				if (logger.isDebugEnabled())
					logger.debug("Message : Terminate to session.");
				return;
			}

			int controlBits = segment.getFlags();
			if (controlBits == FIN || controlBits == FIN_ACK || controlBits == FIN_PSH_ACK || controlBits == PSH_ACK) {
				if (segment.getDirection() == TcpDirection.ToServer)
					session.getTxBuffer().store(session, session.getTxWaitQueue(), segment, true);
				else
					session.getRxBuffer().store(session, session.getRxWaitQueue(), segment, true);
			}

			else if (controlBits == ACK) {
				if (segment.getDirection() == TcpDirection.ToServer) {
					session.setSrcLastAck(segment.getRelativeAck());
					if (segment.getData() != null) {
						session.setRecentSrc(segment.getRelativeSeq(), segment.getRelativeAck(),
								segment.getDataLength(), 16);
					} else
						session.setRecentSrc(segment.getRelativeSeq(), segment.getRelativeAck(), 0, 16);
				} else {
					session.setDestLastAck(segment.getRelativeAck());
					if (segment.getData() != null) {
						session.setRecentDest(segment.getRelativeSeq(), segment.getRelativeAck(),
								segment.getDataLength(), 16);
					} else
						session.setRecentDest(segment.getRelativeSeq(), segment.getRelativeAck(), 0, 16);
				}
			}

			else {
				endSession(segment, session);

				if (logger.isDebugEnabled())
					logger.debug("Message : Terminate to session.");
			}
		}
	}

	private boolean isResponseSynAck(TcpPacket segment) {
		if (segment.getFlags() == ACK && segment.getRelativeSeq() == 1 && segment.getRelativeAck() == 1
				&& segment.getData() == null) {
			return true;
		}
		return false;
	}

	private boolean isReceiveData(TcpSessionImpl session, TcpPacket segment) {
		int flags = segment.getFlags();
		if (flags == ACK || flags == PSH_ACK) {
			if (segment.getRelativeSeq() == 1 && segment.getRelativeAck() == 1 && segment.getData() != null) {
				session.setReceiveData(true);
				return true;
			}
		}
		return false;
	}

	private void registerTcpProcessor(TcpSession session, TcpPacket segment) {
		Protocol protocol = mapper.map(segment);
		session.registerProtocol(protocol);

		Collection<TcpProcessor> processors = mapper.getTcpProcessors(protocol);
		if (processors == null)
			return;

		for (TcpProcessor p : processors) {
			p.onEstablish(segment.getSessionKey());
		}
	}

	private void setTcpState(TcpSessionImpl session, TcpState clientState, TcpState serverState) {
		session.setClientState(clientState);
		session.setServerState(serverState);
	}

	private void endSession(TcpPacket segment, TcpSessionImpl session) {
		Protocol protocol = session.getProtocol();
		Collection<TcpProcessor> processors = mapper.getTcpProcessors(protocol);

		terminateSession(session);

		if (processors == null)
			return;

		for (TcpProcessor p : processors) {
			p.onFinish(segment.getSessionKey());
		}
	}

	private void terminateSession(TcpSessionImpl session) {
		TcpSessionKey key = session.getKey();
		session.deallocateTxBuffer();
		session.deallocateRxBuffer();
		tcpSessionMap.remove(key);
	}

	public TcpProtocolMapper getProtocolMapper() {
		return mapper;
	}

	public void setProtocolMapper(TcpProtocolMapper mapper) {
		this.mapper = mapper;
	}
}