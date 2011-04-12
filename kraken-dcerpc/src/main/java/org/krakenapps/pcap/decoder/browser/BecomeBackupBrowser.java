package org.krakenapps.pcap.decoder.browser;

import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.decoder.rpce.RpcUdpHeader;
import org.krakenapps.pcap.decoder.rpce.packet.UdpPDUInterface;
import org.krakenapps.pcap.util.Buffer;

public class BecomeBackupBrowser implements UdpPDUInterface {

	byte opcode;
	String browserToPromote;// nullterminate String;

	public byte getOpcode() {
		return opcode;
	}

	public void setOpcode(byte opcode) {
		this.opcode = opcode;
	}

	public String getBrowserToPromote() {
		return browserToPromote;
	}

	public void setBrowserToPromote(String browserToPromote) {
		this.browserToPromote = browserToPromote;
	}

	@Override
	public void parse(Buffer b, RpcUdpHeader h) {
		opcode = b.get();
		browserToPromote = NetBiosNameCodec.readOemName(b);
	}

}
