package org.krakenapps.pcap.decoder.browser;

import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.decoder.rpce.RpcUdpHeader;
import org.krakenapps.pcap.decoder.rpce.packet.UdpPDUInterface;
import org.krakenapps.pcap.util.Buffer;

public class AnnounceRequestBrowser implements UdpPDUInterface {

	byte opcode;
	byte reserved;
	String responseName;

	@Override
	public void parse(Buffer b, RpcUdpHeader h) {
		opcode = b.get();
		reserved = b.get();
		responseName = NetBiosNameCodec.readOemName(b);

	}

	public byte getOpcode() {
		return opcode;
	}

	public void setOpcode(byte opcode) {
		this.opcode = opcode;
	}

	public byte getReserved() {
		return reserved;
	}

	public void setReserved(byte reserved) {
		this.reserved = reserved;
	}

	public String getResponseName() {
		return responseName;
	}

	public void setResponseName(String responseName) {
		this.responseName = responseName;
	}

}
