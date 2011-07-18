package org.krakenapps.pcap.decoder.tcp;

import org.krakenapps.pcap.util.Buffer;

public class TcpSackReassembler {
	private TcpSackReassembler() {
	}

	public static void insert(TcpSessionImpl session, TcpPacket packet) {
		WaitQueue queue;
		if (packet.getDirection() == TcpDirection.ToServer)
			queue = session.getServerQueue();
		else
			queue = session.getClientQueue();
		queue.enqueue(packet);
	}

	/* return: reassembled packet */
	public static TcpPacket reassemble(TcpSessionImpl session, TcpPacket packet, TcpStateUpdater stateUpdater) {
		TcpHost host;
		WaitQueue queue;

		if (packet.getDirection() == TcpDirection.ToServer) {
			host = session.getServer();
			queue = session.getServerQueue();
		} else {
			host = session.getClient();
			queue = session.getClientQueue();
		}

		return doReassemble(queue, session, host, packet);
	}

	private static TcpPacket doReassemble(WaitQueue queue, TcpSessionImpl session, TcpHost host, TcpPacket packet) {
		TcpPacket p;
		
		while (true) {
			p = findNextDatagram(queue, host.getLastFrameReceived());
			if (p == null) {
				return null;
			}
				
			if (p.getData() != null) {
				Buffer data = p.getData();
				int readable = data.readableBytes();
				
				if(p.getDataLength() < readable) {
					p.setReassembledLength(p.getDataLength());
					data.skip(p.getDataLength());
					data.flip();
				}
				else 
					p.setReassembledLength(readable);
				
				if (packet.getDirection() == TcpDirection.ToServer) 
					session.pushToClientSack(data);
				else
					session.pushToServerSack(data);
			}
			return p;
		}
	}
	
	private static TcpPacket findNextDatagram(WaitQueue queue, int lastReceived) {
		for (int i = 0; i < queue.size(); i++) {
			TcpPacket p = queue.dequeue(i);

			int seq = p.getRelativeSeq();
			if (seq == lastReceived) {
				queue.remove(i);
				return p;
			} else if (seq + p.getDataLength() > lastReceived) {
				if (seq > lastReceived || p.getData() == null)
					continue;

				/* check TCP packet range. drop received data(=garbage) */
				int garbage = lastReceived - seq;
				Buffer tcpData = p.getData();
				tcpData.skip(garbage);
				tcpData.discardReadBytes();
				queue.remove(i);
				return p;
			}
		}
		return null;
	}
}