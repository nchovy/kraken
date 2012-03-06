package org.krakenapps.pcap.decoder.rpce.tcppacket.call;

import org.krakenapps.pcap.decoder.rpce.RpcTcpHeader;
import org.krakenapps.pcap.decoder.rpce.tcppacket.association.TcpPDUInterface;
import org.krakenapps.pcap.util.Buffer;


public class TcpShutdown implements TcpPDUInterface{

	@Override
	public void parse(Buffer b, RpcTcpHeader h) {
			
	}
	//there is no implementation
}
