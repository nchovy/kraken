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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.krakenapps.pcap.Protocol;

public class TcpSessionTable {
	private final TcpProtocolMapper mapper;
	private Map<TcpSessionKey, TcpSessionImpl> map;

	public TcpSessionTable(TcpProtocolMapper mapper) {
		this.mapper = mapper;
		map = new ConcurrentHashMap<TcpSessionKey, TcpSessionImpl>();
	}

	public void openSession(TcpProtocolMapper mapper, TcpSessionKey key, TcpPacket packet) {
		TcpSessionImpl session = new TcpSessionImpl(mapper);
		session.setKey(key);
		session.createClient(packet);
		
		map.put(key, session);
	}

	public void doEstablish(TcpSessionImpl session, TcpPacket packet, TcpStateUpdater stateUpdater) { 
		if (isCorrectConnection(session, packet)) {
			if (!session.isRegisterProtocol()) {
				registerTcpProcessor(session, packet);
				session.setRegisterProtocol(true);
			}

			stateUpdater.updateState(session, packet);
			if (session.getClientState() == TcpState.ESTABLISHED && session.getServerState() == TcpState.ESTABLISHED) {
				session.getClient().setLastFrameReceived(1);
				session.getServer().setLastFrameReceived(1);
			}
		} else
			abnormalClose(packet.getSessionKey());
	}
	
	public void close(TcpPacket packet) {
		if(packet == null)
			return;
				
		TcpSessionKey key = packet.getSessionKey();
		Protocol protocol = map.get(key).getProtocol();
		
		if (map.containsKey(key)) 
			map.remove(key);

		Collection<TcpProcessor> processors = mapper.getTcpProcessors(protocol);
		if (processors == null)
			return;

		for (TcpProcessor p : processors) 
			p.onFinish(key);
	}
	
	public void abnormalClose(TcpSessionKey key) {
		if (map.containsKey(key)) 
			map.remove(key);
	}

	public TcpSessionImpl getSession(TcpSessionKey key) {
		return map.get(key);
	}

	public List<TcpSession> getCurrentSessions() { 
		List<TcpSession> sessions = new ArrayList<TcpSession>(map.values());
		return sessions;
	}
	
	public boolean isExist(TcpSessionKey key) {
		return map.containsKey(key);
	}
	
	private boolean isCorrectConnection(TcpSessionImpl session, TcpPacket packet) {
		TcpState clientState = session.getClientState();

		switch (clientState) {
		case LISTEN:
			if (packet.getRelativeSeq() == 0 && packet.getDataLength() == 0)
				return true;
			else
				return false;

		case SYN_SENT:
			if (packet.getDirection() == TcpDirection.ToServer) {
				if (packet.getRelativeSeq() == 1 && packet.getRelativeAck() == 1 && packet.getDataLength() == 0)
					return true;
				else
					return false;
			} else {
				if (packet.getRelativeSeq() == 0 && packet.getRelativeAck() == 1 && packet.getDataLength() == 0)
					return true;
				else
					return false;
			}

			/* Never access this case */
		default:
			return true;
		}
	}
	
	private void registerTcpProcessor(TcpSessionImpl session, TcpPacket segment) {
		Protocol protocol = mapper.map(segment);
		session.registerProtocol(protocol);

		Collection<TcpProcessor> processors = mapper.getTcpProcessors(protocol);
		if (processors == null)
			return;

		for (TcpProcessor p : processors) {
			p.onEstablish(segment.getSessionKey());
		}
	}
}