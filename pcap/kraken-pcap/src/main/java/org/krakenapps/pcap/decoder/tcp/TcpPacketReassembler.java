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

import org.krakenapps.pcap.util.Buffer;

public class TcpPacketReassembler {
	private TcpPacketReassembler() {
	}

	public static void reassemble(TcpSessionImpl session, TcpPacket packet, TcpStateUpdater stateUpdater) {
		if (isValidPacket(session, packet)) {
			if (packet.getData() != null) {
				int readable = packet.getData().readableBytes();
				/* TODO: check new code */
				if(packet.getDataLength() < readable) 
					readable = packet.getDataLength();
				/* -boundary- */
				packet.setReassembledLength(readable);
				doReassemble(session, packet, readable);
				slideWindow(session, packet, readable);
			} else {
				doReassemble(session, packet, 0);
				slideWindow(session, packet, 0);
			}
		}
	}

	private static boolean isValidPacket(TcpSessionImpl session, TcpPacket packet) {
		int seq = packet.getRelativeSeq();

		int received;
		if (packet.getDirection() == TcpDirection.ToServer)
			received = session.getServer().getLastFrameReceived();
		else
			received = session.getClient().getLastFrameReceived();
		 
		if (seq == received)
			return true;
		else if (seq + packet.getDataLength() > received) {
			if (seq > received || packet.getData() == null)
				return false;

			/* check TCP packet range. drop received data */
			int dropped = received - seq;
			Buffer tcpData = packet.getData();
			tcpData.skip(dropped);
			tcpData.discardReadBytes();
			return true;
		}
		return false;
	}

	private static void doReassemble(TcpSessionImpl session, TcpPacket packet, int lengthOfData) {
		if (packet.isAck()) {
			if (lengthOfData > 0) {
				Buffer data = packet.getData();
				if (packet.getDirection() == TcpDirection.ToServer) {
					session.pushToServer(data);
				} else {
					session.pushToClient(data);
				}
			}
		} else
			return;
	}

	private static void slideWindow(TcpSessionImpl session, TcpPacket packet, int lengthOfData) {
		TcpHost host;
		if (packet.getDirection() == TcpDirection.ToServer)
			host = session.getServer();
		else
			host = session.getClient();

		if (packet.isFin())
			host.setLastFrameReceived(1);
		host.setLastFrameReceived(lengthOfData);
	}
}