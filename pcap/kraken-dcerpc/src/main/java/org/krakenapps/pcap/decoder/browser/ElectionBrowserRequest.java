package org.krakenapps.pcap.decoder.browser;

import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.decoder.rpce.RpcUdpHeader;
import org.krakenapps.pcap.decoder.rpce.packet.UdpPDUInterface;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class ElectionBrowserRequest implements UdpPDUInterface {

	byte opcode;
	byte version;
	int criteria;
	int uptime;
	int unused;
	String serverName;

	public byte getOpcode() {
		return opcode;
	}

	public void setOpcode(byte opcode) {
		this.opcode = opcode;
	}

	public byte getVersion() {
		return version;
	}

	public void setVersion(byte version) {
		this.version = version;
	}

	public int getCriteria() {
		return criteria;
	}

	public void setCriteria(int criteria) {
		this.criteria = criteria;
	}

	public int getUptime() {
		return uptime;
	}

	public void setUptime(int uptime) {
		this.uptime = uptime;
	}

	public int getUnused() {
		return unused;
	}

	public void setUnused(int unused) {
		this.unused = unused;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	@Override
	public void parse(Buffer b, RpcUdpHeader h) {
		opcode = b.get();
		version = b.get();
		criteria = ByteOrderConverter.swap(b.getInt());
		uptime = ByteOrderConverter.swap(b.getInt());
		unused = ByteOrderConverter.swap(b.getInt());
		serverName = NetBiosNameCodec.readOemName(b);
	}

}
