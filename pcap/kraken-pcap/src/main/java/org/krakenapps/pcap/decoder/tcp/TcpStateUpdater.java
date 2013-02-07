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

public class TcpStateUpdater {
	private static final int FIN = 1;
	private static final int SYN = 2;
	private static final int ACK = 16;
	private static final int FIN_ACK = 17;
	private static final int SYN_ACK = 18;
	private static final int PSH_ACK = 24;
	private static final int FIN_PSH_ACK = 25;

	private final TcpTransitionMapper tMap;

	public TcpStateUpdater() {
		tMap = new TcpTransitionMapper();
	}

	public void updateState(TcpSessionImpl session, TcpPacket packet) {
		if (packet == null)
			return;

		TcpState clientState = session.getClientState();
		TcpState serverState = session.getServerState();

		switch (packet.getFlags()) {
		case SYN:
			clientState = tMap.map(packet, clientState, Action.SEND_SYN);
			serverState = tMap.map(packet, serverState, Action.RECV_SYN);
			break;

		case SYN_ACK:
			clientState = tMap.map(packet, clientState, Action.RECV_SYNACKED);
			break;

		case ACK:
		case PSH_ACK:
			if (serverState == TcpState.SYN_RCVD)
				serverState = tMap.map(packet, serverState, Action.RECV_SYNACKED);

			if (session.getPacketCountAfterFin() >= 1) {
				// Except case
				if (session.getFirstFinSeq() == packet.getRelativeAck() && session.getFirstFinAck() == packet.getRelativeSeq())
					return;
				else if (session.getFirstFinSeq() > packet.getRelativeAck())
					return;
				session.setPacketCountAfterFin(session.getPacketCountAfterFin() + 1);
			}

			if (session.getPacketCountAfterFin() == 2) {
				if (packet.getDirection() == TcpDirection.ToServer) {
					clientState = tMap.map(packet, clientState, Action.RECV_FIN);
					serverState = tMap.map(packet, serverState, Action.RECV_ACKED);
				} else {
					serverState = tMap.map(packet, serverState, Action.RECV_FIN);
					clientState = tMap.map(packet, clientState, Action.RECV_ACKED);
				}
			}

			else if (session.getPacketCountAfterFin() == 3) {
				if (packet.getDirection() == TcpDirection.ToServer) {
					if (serverState == TcpState.LAST_ACK)
						serverState = tMap.map(packet, serverState, Action.RECV_FINACKED);
					else
						// simultaneous close.
						clientState = tMap.map(packet, clientState, Action.RECV_FIN);
				}

				else {
					if (clientState == TcpState.LAST_ACK)
						clientState = tMap.map(packet, clientState, Action.RECV_FINACKED);
					else
						// simultaneous close.
						serverState = tMap.map(packet, serverState, Action.RECV_FIN);
				}
			}

			else if (session.getPacketCountAfterFin() == 4) {
				if (packet.getDirection() == TcpDirection.ToServer) {
					serverState = tMap.map(packet, serverState, Action.RECV_FINACKED);
					clientState = tMap.map(packet, clientState, Action.RECV_FIN);
				} else {
					clientState = tMap.map(packet, clientState, Action.RECV_FINACKED);
					serverState = tMap.map(packet, serverState, Action.RECV_FIN);
				}
			}
			break;
		case FIN:
		case FIN_ACK:
		case FIN_PSH_ACK:
			session.setPacketCountAfterFin(session.getPacketCountAfterFin() + 1);

			if (session.getPacketCountAfterFin() == 1) {
				if (packet.getDirection() == TcpDirection.ToServer)
					clientState = tMap.map(packet, clientState, Action.SEND_FIN);
				else
					serverState = tMap.map(packet, serverState, Action.SEND_FIN);
				session.setFirstFinSeq(packet.getRelativeSeq());
				session.setFirstFinAck(packet.getRelativeAck());
			}

			else if (session.getPacketCountAfterFin() == 2) {
				if (packet.getDirection() == TcpDirection.ToServer) {
					if ((packet.getRelativeSeq() == session.getFirstFinAck()) && (packet.getRelativeAck() == session.getFirstFinSeq())) {
						clientState = tMap.map(packet, clientState, Action.SEND_FIN);
					}

					else {
						clientState = tMap.map(packet, clientState, Action.RECV_FIN);
						serverState = tMap.map(packet, serverState, Action.RECV_FINACKED);
						clientState = tMap.map(packet, clientState, Action.SEND_FIN);

					}
				}

				else {
					if ((packet.getRelativeSeq() == session.getFirstFinAck()) && (packet.getRelativeAck() == session.getFirstFinSeq())) {
						serverState = tMap.map(packet, serverState, Action.SEND_FIN);
					} else {
						serverState = tMap.map(packet, serverState, Action.RECV_FIN);
						clientState = tMap.map(packet, clientState, Action.RECV_FINACKED);
						serverState = tMap.map(packet, serverState, Action.SEND_FIN);
					}
				}
			}

			else if (session.getPacketCountAfterFin() == 3) {
				if (packet.getDirection() == TcpDirection.ToServer) {
					clientState = tMap.map(packet, clientState, Action.SEND_FIN);
					serverState = tMap.map(packet, serverState, Action.RECV_FIN);
				} else {
					clientState = tMap.map(packet, clientState, Action.RECV_FIN);
					serverState = tMap.map(packet, serverState, Action.SEND_FIN);
				}
			}
			break;
		}

		session.setClientState(clientState);
		session.setServerState(serverState);
	}
}