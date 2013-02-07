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

public class TcpPacketHandler {
	private TcpStateUpdater stateUpdater;

	public TcpPacketHandler() {
		stateUpdater = new TcpStateUpdater();
	}

	public void handle(TcpSessionTable sessionTable, TcpSessionImpl session, TcpPacket packet) {
		session.setRelativeNumbers(packet);
		TcpState serverState = session.getServerState();

		if (serverState.compareTo(TcpState.ESTABLISHED) < 0) {
			session.doEstablish(sessionTable, session, packet, stateUpdater);
		} else {
			TcpPacketReassembler.reassemble(session, packet, stateUpdater);
			stateUpdater.updateState(session, packet);
			
			if(session.getClientState() == TcpState.CLOSED && session.getServerState() == TcpState.CLOSED) 
				session.close(sessionTable, session, packet);
		}
	}
}