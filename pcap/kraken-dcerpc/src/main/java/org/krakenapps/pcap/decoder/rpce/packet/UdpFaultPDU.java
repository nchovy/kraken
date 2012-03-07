package org.krakenapps.pcap.decoder.rpce.packet;

import org.krakenapps.pcap.decoder.rpce.RpcUdpHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class UdpFaultPDU implements UdpPDUInterface{

	private int st; // status

	public int getSt() {
		return st;
	}

	public void setSt(int st) {
		this.st = st;
	}

	@Override
	public void parse(Buffer b, RpcUdpHeader h) {
		st = ByteOrderConverter.swap(b.getInt());
	}
	
}
