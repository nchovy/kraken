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

	public List<TcpSessionImpl> getCurrentSessions() { 
		List<TcpSessionImpl> sessions = new ArrayList<TcpSessionImpl>(map.values());
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