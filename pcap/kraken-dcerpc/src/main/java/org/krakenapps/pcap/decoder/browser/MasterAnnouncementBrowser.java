package org.krakenapps.pcap.decoder.browser;

import org.krakenapps.pcap.decoder.rpce.RpcUdpHeader;
import org.krakenapps.pcap.decoder.rpce.packet.UdpPDUInterface;
import org.krakenapps.pcap.util.Buffer;

public class MasterAnnouncementBrowser implements UdpPDUInterface{

	byte opcode;
	byte type;
	// type is
	// RESET_STATE_STOP_MASTER 0x01
	// RESET_STATE_CLEAR_ALL 0x02
	// RESET_STATE_STOP 0x03
	public byte getOpcode() {
		return opcode;
	}
	public void setOpcode(byte opcode) {
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
