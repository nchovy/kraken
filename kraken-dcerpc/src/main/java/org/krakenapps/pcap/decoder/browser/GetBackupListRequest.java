package org.krakenapps.pcap.decoder.browser;

import org.krakenapps.pcap.decoder.rpce.RpcUdpHeader;
import org.krakenapps.pcap.decoder.rpce.packet.UdpPDUInterface;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class GetBackupListRequest implements UdpPDUInterface {

	byte opcode;
	byte requestedCount;
	int token;

	public byte getOpcode() {
		return opcode;
	}

	public void setOpcode(byte opcode) {
		this.opcode = opcode;
	}

	public byte getRequestedCount() {
		return requestedCount;
	}

	public void setRequestedCount(byte requestedCount) {
		this.requestedCount = requestedCount;
	}

	public int getToken() {
		return token;
	}

	public void setToken(int token) {
		this.token = token;
	}

	@Override
	public void parse(Buffer b, RpcUdpHeader h) {
		opcode = b.get();
		requestedCount = b.get();
		token = ByteOrderConverter.swap(b.getInt());
	}

}
