package org.krakenapps.pcap.decoder.rpce.packet;

import org.krakenapps.pcap.decoder.rpce.RpcUdpHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class UdpCancelAckPDU implements UdpPDUInterface{

	private int vers; // it must 0x00
	private int cancelID;
	boolean serverIsAccepting; // true is accepting

	public int getVers() {
		return vers;
	}

	public void setVers(int vers) {
		this.vers = vers;
	}

	public int getCancelID() {
		return cancelID;
	}

	public void setCancelID(int cancelId) {
		this.cancelID = cancelId;
	}

	public boolean isServerIsAccepting() {
		return serverIsAccepting;
	}

	public void setServerIsAccepting(boolean serverIsAccepting) {
		this.serverIsAccepting = serverIsAccepting;
	}

	@Override
	public void parse(Buffer b, RpcUdpHeader h) {
		vers = ByteOrderConverter.swap(b.getInt());
		cancelID = ByteOrderConverter.swap(b.getInt());
	}
}
