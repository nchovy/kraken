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

import org.krakenapps.pcap.Protocol;

public class TcpFlagHandler {
	private final TcpProtocolMapper mapper;

	public TcpFlagHandler(TcpProtocolMapper mapper) {
		this.mapper = mapper;
	}

	public void handle(TcpSessionTable sessionTable, TcpSessionImpl session, TcpPacket packet) {
		switch (packet.getFlags()) {
		case TcpFlag.SYN:
			if (session == null)
				onSynSent(sessionTable, packet);
			else
				packet.setGarbage(true);
			break;
		case (TcpFlag.SYN + TcpFlag.ACK):
			if (session != null && isVaildSynAck(sessionTable, session, packet))
				onSynAckSent(sessionTable, packet);
			else
				packet.setGarbage(true);
			break;
		case TcpFlag.RST:
		case (TcpFlag.RST + TcpFlag.ACK):
			if (session != null)
				onResetArrived(sessionTable, session, packet);
			else
				packet.setGarbage(true);
			break;
		default:
			return;
		}
	}

	private void onSynSent(TcpSessionTable sessionTable, TcpPacket packet) {
		TcpSessionKey key = new TcpSessionKeyImpl(packet.getSourceAddress(), packet.getDestinationAddress(), packet.getSourcePort(), packet.getDestinationPort());
		sessionTable.openSession(mapper, key, packet);
		TcpSessionImpl session = sessionTable.getSession(key);

		session.setClientFirstSeq(packet.getSeq());
	}

	private boolean isVaildSynAck(TcpSessionTable sessionTable, TcpSessionImpl session, TcpPacket packet) {
		if (session == null)
			return false;
		else {
			if ((session.getClientState() == TcpState.SYN_SENT) && (session.getServerState() == TcpState.SYN_RCVD))
				return true;
			else
				return false;
		}
	}

	private void onSynAckSent(TcpSessionTable manager, TcpPacket packet) {
		TcpSessionKey key = packet.getSessionKey();
		TcpSessionImpl session = manager.getSession(key);

		session.createServer(packet);
		session.setServerFirstSeq(packet.getSeq());
	}

	private void onResetArrived(TcpSessionTable sessionTable, TcpSessionImpl session, TcpPacket packet) {
		int seq;
		int received;

		packet.setDirection(session);

		if (packet.getDirection() == TcpDirection.ToServer) {
			seq = session.retRelativeClientSeq(packet.getSeq());
			received = session.getServer().getLastFrameReceived();
		} else {
			seq = session.retRelativeServerSeq(packet.getSeq());
			received = session.getClient().getLastFrameReceived();
		}

		if ((received == seq) || ((received + 1) == seq)) {
			Protocol protocol = session.getProtocol();
			Collection<TcpProcessor> processors = mapper.getTcpProcessors(protocol);

			if (processors == null)
				return;

			for (TcpProcessor p : processors) {
				p.onReset(session.getKey());
			}

			sessionTable.abnormalClose(packet.getSessionKey());
		}

		/* invalid RST packet */
		else
			packet.setGarbage(true);
	}
}