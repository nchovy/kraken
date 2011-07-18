package org.krakenapps.pcap.decoder.tcp;

import java.util.Collection;

import org.krakenapps.pcap.decoder.ip.IpProcessor;
import org.krakenapps.pcap.decoder.ip.Ipv4Packet;
import org.krakenapps.pcap.decoder.ipv6.Ipv6Packet;
import org.krakenapps.pcap.decoder.ipv6.Ipv6Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpDecoder implements IpProcessor, Ipv6Processor {
	private TcpSegmentCallbacks segmentCallbacks;
	private TcpProtocolMapper mapper;
	private TcpSessionTable sessionTable;
	private TcpFlagHandler flagHandler;

	private TcpPacketHandler packetHandler;
	private TcpSackHandler sackHandler;
	private final Logger logger = LoggerFactory.getLogger(TcpDecoder.class.getName());

	public TcpDecoder(TcpProtocolMapper mapper) {
		this.mapper = mapper;
		segmentCallbacks = new TcpSegmentCallbacks();
		sessionTable = new TcpSessionTable(mapper);
		flagHandler = new TcpFlagHandler(mapper);

		packetHandler = new TcpPacketHandler();
		sackHandler = new TcpSackHandler();
	}

	public TcpProtocolMapper getProtocolMapper() {
		return mapper;
	}

	public void dispatchNewTcpSegment(TcpSessionImpl session, TcpPacket segment) {
	}

	public Collection<TcpSessionImpl> getCurrentSessions() {
		return sessionTable.getCurrentSessions();
	}

	public void registerSegmentCallback(TcpSegmentCallback callback) {
		segmentCallbacks.register(callback);
	}

	public void unregisterSegmentCallback(TcpSegmentCallback callback) {
		segmentCallbacks.unregister(callback);
	}

	public void process(Ipv4Packet packet) {
		TcpPacket newTcp = TcpPacket.parse(packet);

		if (newTcp.isJumbo()) {
			TcpSessionImpl session = sessionTable.getSession(newTcp.getSessionKey());
			if (session != null) {
				sessionTable.abnormalClose(session.getKey());
				logger.error("session terminate: find jumbo packet ");
			}
		} else {
			handle(newTcp);
		}
	}

	@Override
	public void process(Ipv6Packet packet) {
		// TODO: next header handling
		TcpPacket newTcp = TcpPacket.parse(packet);

		if (newTcp.isJumbo()) {
			TcpSessionImpl session = sessionTable.getSession(newTcp.getSessionKey());
			if (session != null) {
				sessionTable.abnormalClose(session.getKey());
				logger.error("session terminate: find jumbo packet ");
			}
		} else {
			handle(newTcp);
		}
	}

	private void handle(TcpPacket newTcp) {
		/* get session */
		TcpSessionImpl session = sessionTable.getSession(newTcp.getSessionKey());
		flagHandler.handle(sessionTable, session, newTcp);
		session = sessionTable.getSession(newTcp.getSessionKey());

		dispatchNewTcpSegment(session, newTcp);

		if (newTcp.isGarbage() || session == null) {
			System.out.println("{NULL Session}");
			return;
		}

		newTcp.setDirection(session);
		TcpDirection direction = newTcp.getDirection();

		/* find and set SACK option */
		int flags = newTcp.getFlags();
		if (flags == TcpFlag.SYN || flags == (TcpFlag.SYN + TcpFlag.ACK)) {
			if (isSack(newTcp)) {
				if (direction == TcpDirection.ToServer)
					session.setClientStreamOption(TcpStreamOption.SACK);
				else
					session.setServerStreamOption(TcpStreamOption.SACK);
			}
		}

		/* handle TCP segment */
		TcpStreamOption streamOption;
		if (direction == TcpDirection.ToServer)
			streamOption = session.getServerStreamOption();
		else
			streamOption = session.getClientStreamOption();

		if (streamOption == TcpStreamOption.SACK)
			sackHandler.handle(sessionTable, session, newTcp);
		else
			packetHandler.handle(sessionTable, session, newTcp);
	}

	private boolean isSack(TcpPacket packet) {
		if (packet.getOptions() == null)
			return false;
		else {
			byte[] options = packet.getOptions();
			int offset = 0;

			while (offset < options.length) {
				switch (options[offset]) {
				/* skip 1 byte option */
				case 0x00:
				case 0x01:
					offset++;
					continue;
					/* skip 4 bytes option(MSS) */
				case 0x02:
					offset += 4;
					continue;
					/* skip 3 bytes option */
				case 0x03:
				case 0x0e:
					offset += 3;
					continue;
					/* skip 10 bytes option(TimeStamps) */
				case 0x08:
					offset += 10;
					continue;
				case 0x04:
					return true;
				}
			}
			return false;
		}
	}
}