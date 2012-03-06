package org.krakenapps.pcap.decoder.rpce.packet;

import org.krakenapps.pcap.decoder.rpce.RpcUdpHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class UdpCancelPDU implements UdpPDUInterface{

	private int vers; // it must 0x00;
	private int cancelID;
	public int getVers() {
		return vers;
	}
	public void setVers(int vers) {
		this.vers = vers;
	}
	public int getCancelID() {
		return cancelID;
	}
	public void setCancelID(int cancelID) {
		this.cancelID = cancelID;
	}
	@Override
	public void parse(Buffer b, RpcUdpHeader h) {
		vers = ByteOrderConverter.swap(b.getInt());
		cancelID = ByteOrderConverter.swap(b.getInt());
	}
	
}
