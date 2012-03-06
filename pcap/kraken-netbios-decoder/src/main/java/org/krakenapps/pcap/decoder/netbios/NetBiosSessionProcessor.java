package org.krakenapps.pcap.decoder.netbios;

import org.krakenapps.pcap.decoder.tcp.TcpSessionKey;

public interface NetBiosSessionProcessor {
	void processRx(NetBiosSessionPacket p, TcpSessionKey key);
	void processTx(NetBiosSessionPacket p, TcpSessionKey key);
}
