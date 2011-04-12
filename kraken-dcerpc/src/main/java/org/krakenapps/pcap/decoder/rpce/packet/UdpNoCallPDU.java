package org.krakenapps.pcap.decoder.rpce.packet;

import org.krakenapps.pcap.decoder.rpce.RpcUdpHeader;
import org.krakenapps.pcap.util.Buffer;

public class UdpNoCallPDU implements UdpPDUInterface{

	@Override
	public void parse(Buffer b, RpcUdpHeader h) {
		
	}

	//there is no implementation
}
