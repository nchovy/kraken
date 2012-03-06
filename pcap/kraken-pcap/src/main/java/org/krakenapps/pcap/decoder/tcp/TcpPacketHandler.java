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