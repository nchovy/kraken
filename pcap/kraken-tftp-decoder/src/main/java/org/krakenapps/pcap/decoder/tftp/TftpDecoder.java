package org.krakenapps.pcap.decoder.tftp;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

import org.krakenapps.pcap.Protocol;
import org.krakenapps.pcap.decoder.udp.UdpPacket;
import org.krakenapps.pcap.decoder.udp.UdpProcessor;
import org.krakenapps.pcap.decoder.udp.UdpProtocolMapper;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.BufferInputStream;

public class TftpDecoder implements UdpProcessor {
	private final UdpProtocolMapper mapper;
	private Set<TftpSession> sessions;
	private Set<TftpProcessor> callbacks;

	public TftpDecoder(UdpProtocolMapper mapper) {
		this.mapper = mapper;
		sessions = new HashSet<TftpSession>();
		callbacks = new HashSet<TftpProcessor>();
	}

	public void register(TftpProcessor processor) {
		callbacks.add(processor);
	}

	public void unregister(TftpProcessor processor) {
		callbacks.remove(processor);
	}

	public void registerTftp(InetSocketAddress sockAddr) {
		mapper.registerTemporaryMapping(sockAddr, Protocol.TFTP);
	}

	public void unregisterTftp(InetSocketAddress sockAddr) {
		mapper.unregisterTemporaryMapping(sockAddr);
	}

	@Override
	public void process(UdpPacket p) {
		Buffer tftpData = p.getData();

		short opCode = tftpData.getShort();
		if (opCode == 1 || opCode == 2) {
			createSession(opCode, p, tftpData);
			registerTftp(p.getSource());
		} else if (opCode == 3) {
			handleDataPacket(p, tftpData);
		} else if (opCode == 4) {
			handleAckPacket(p, tftpData);
		} else {
			/* error case */
		}
	}

	private void createSession(int opCode, UdpPacket p, Buffer tftpData) {
		int len = tftpData.bytesBefore(new byte[] { 0x00 });
		if (len == 0)
			return;

		byte[] fileName = new byte[len];
		tftpData.gets(fileName);
		/* skip 00 */
		tftpData.get();

		int len2 = tftpData.bytesBefore(new byte[] { 0x00 });
		if (len2 == 0)
			return;

		byte[] mode = new byte[len2];
		tftpData.gets(mode);
		/* skip 00 */
		tftpData.get();

		TftpSession session = new TftpSession(opCode, p.getSource(), p.getDestination().getAddress(), fileName, mode);
		sessions.add(session);

		dispatchCommand(session.toString());
	}

	private TftpSession getSession(UdpPacket p) {
		InetSocketAddress src = p.getSource();
		InetSocketAddress dest = p.getDestination();

		for (TftpSession s : sessions) {
			if (s.equals(src, dest.getAddress()) || s.equals(dest, src.getAddress()))
				return s;
		}
		return null;
	}

	private void terminate(TftpSession session) {
		unregisterTftp(session.getSrcAddress());
		session = null;
	}

	private void handleDataPacket(UdpPacket p, Buffer tftpData) {
		/* retrieve tftp session */
		TftpSession session = getSession(p);
		if (session == null)
			return;

		int block = tftpData.getShort();
		if (block == (session.getSendNum() + 1)) {
			/* valid sequence */
			int remain = tftpData.readableBytes();
			byte[] data = new byte[remain];
			tftpData.gets(data);

			session.putData(data);
			session.incSendNum();

			if (remain < 512) {
				/* last data packet */
				Buffer b = session.getData();
				BufferInputStream is = new BufferInputStream(b);
				dispatchFile(is, session.getFileName());

				terminate(session);
			}
		} else {
			terminate(session);
		}
	}

	private void handleAckPacket(UdpPacket p, Buffer tftpData) {
		/* retrieve tftp session */
		TftpSession session = getSession(p);
		if (session == null)
			return;

		int block = tftpData.getShort();
		if ((block == 0) && (session.getAckNum() == 0))
			return;

		if (block == (session.getAckNum() + 1)) {
			/* valid sequence */
			session.incAckNum();
		} else {
			terminate(session);
		}
	}

	private void dispatchCommand(String command) {
		for (TftpProcessor processor : callbacks) {
			processor.onCommand(command);
		}
	}

	private void dispatchFile(InputStream is, String fileName) {
		for (TftpProcessor processor : callbacks) {
			processor.onExtractFile(is, fileName);
		}
	}
}