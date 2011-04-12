package org.krakenapps.pcap.decoder.browser;

import org.krakenapps.pcap.decoder.rpce.RpcUdpHeader;
import org.krakenapps.pcap.decoder.rpce.packet.UdpPDUInterface;
import org.krakenapps.pcap.util.Buffer;

public class ResetstateRequestBrowser implements UdpPDUInterface{

	byte opcode;
	byte type;
	public byte getOpCode() {
		return opcode;
	}
	public void setOpCode(byte opcode) {
		this.opcode = opcode;
	}
	public byte getType() {
		return type;
	}
	public void setType(byte type) {
		this.type = type;
	}
	@Override
	public void parse(Buffer b, RpcUdpHeader h) {
		opcode = b.get();
		type = b.get();
	}
	
}
