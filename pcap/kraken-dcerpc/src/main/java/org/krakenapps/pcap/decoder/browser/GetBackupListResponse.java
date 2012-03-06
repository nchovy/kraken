package org.krakenapps.pcap.decoder.browser;

import org.krakenapps.pcap.decoder.rpce.RpcUdpHeader;
import org.krakenapps.pcap.decoder.rpce.packet.UdpPDUInterface;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class GetBackupListResponse implements UdpPDUInterface {

	byte opcode;
	byte backupServerCount;
	int token;
	String backupServerList;// number of backupserverCount;

	public byte getOpcode() {
		return opcode;
	}

	public void setOpcode(byte opcode) {
		this.opcode = opcode;
	}

	public byte getBackupServerCount() {
		return backupServerCount;
	}

	public void setBackupServerCount(byte backupServerCount) {
		this.backupServerCount = backupServerCount;
	}

	public int getToken() {
		return token;
	}

	public void setToken(int token) {
		this.token = token;
	}

	public String getBackupServerList() {
		return backupServerList;
	}

	public void setBackupServerList(String backupServerList) {
		this.backupServerList = backupServerList;
	}

	@Override
	public void parse(Buffer b, RpcUdpHeader h) {
		opcode = b.get();
		backupServerCount = b.get();
		token = ByteOrderConverter.swap(b.getInt());
	}

}
