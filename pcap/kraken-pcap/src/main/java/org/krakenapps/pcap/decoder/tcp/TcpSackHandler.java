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

public class TcpSackHandler {
	private TcpStateUpdater stateUpdater;

	public TcpSackHandler() {
		stateUpdater = new TcpStateUpdater();
	}

	public void handle(TcpSessionTable sessionTable, TcpSessionImpl session, TcpPacket packet) {
		session.setRelativeNumbers(packet);
		TcpState serverState = session.getServerState();

		if (serverState.compareTo(TcpState.ESTABLISHED) < 0)
			session.doEstablish(sessionTable, session, packet, stateUpdater);
		else {
			TcpSackReassembler.insert(session, packet);
			cleanUpWindow(session, packet);
			TcpPacket reassembledPacket;

			while (true) {
				reassembledPacket = TcpSackReassembler.reassemble(session, packet, stateUpdater);
				if (reassembledPacket == null)
					break;

				slideWindow(session, reassembledPacket);
				stateUpdater.updateState(session, reassembledPacket);
			}
			if (session.getClientState() == TcpState.CLOSED && session.getServerState() == TcpState.CLOSED)
				session.close(sessionTable, session, reassembledPacket);
		}
	}

	private void cleanUpWindow(TcpSessionImpl session, TcpPacket packet) {
		TcpHost host;
		WaitQueue queue;

		if (packet.getDirection() == TcpDirection.ToServer) {
			host = session.getClient();
			queue = session.getClientQueue();
		} else {
			host = session.getServer();
			queue = session.getServerQueue();
		}

		/* apply window size */
		host.setLastAcceptableFrame(packet.getWindow());
		int end = host.getLastAcceptableFrame();

		/* clear up receive window */
		for (int i = 0; i < queue.size(); i++) {
			TcpPacket p = queue.dequeue(i);
			int seq = p.getRelativeSeq();
			int lengthOfData = 0;
			if (p.getData() != null)
				lengthOfData = p.getData().readableBytes();

			if (seq > end)
				queue.remove(i);
			else if (seq + lengthOfData > end) {
				/* cut garbage data */
				int garbage = (seq + lengthOfData) - end;
				int remain = lengthOfData - garbage;

				Buffer data = p.getData();
				data.skip(remain);
				data.flip();
			}
		}
	}

	private void slideWindow(TcpSessionImpl session, TcpPacket packet) {
		if (packet == null || !packet.isAck())
			return;

		TcpHost host;
		if (packet.getDirection() == TcpDirection.ToServer)
			host = session.getServer();
		else
			host = session.getClient();
		host.setLastFrameReceived(packet.getReassembledLength());
		
		if(packet.isFin())
			host.setLastFrameReceived(1);
	}
}