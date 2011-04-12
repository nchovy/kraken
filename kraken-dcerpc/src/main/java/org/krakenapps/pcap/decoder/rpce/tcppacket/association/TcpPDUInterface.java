package org.krakenapps.pcap.decoder.rpce.tcppacket.association;

import org.krakenapps.pcap.decoder.rpce.RpcTcpHeader;
import org.krakenapps.pcap.util.Buffer;

public interface TcpPDUInterface {
	public void parse(Buffer b , RpcTcpHeader h );
}
