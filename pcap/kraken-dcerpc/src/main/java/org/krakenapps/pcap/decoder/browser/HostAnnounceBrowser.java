package org.krakenapps.pcap.decoder.browser;

import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.decoder.rpce.RpcUdpHeader;
import org.krakenapps.pcap.decoder.rpce.packet.UdpPDUInterface;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

// this packet type is udp only
public class HostAnnounceBrowser implements UdpPDUInterface {

	byte opcode;
	byte updateCount;
	int periodicity;
	String serverName;
	byte osVersionMajor;
	int serverType;
	byte browserVersionMajor;
	byte browserversionMinor;
	short signature;
	String comments;

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public byte getOpcode() {
		return opcode;
	}

	public void setOpcode(byte opcode) {
		this.opcode = opcode;
	}

	public byte getUpdateCount() {
		return updateCount;
	}

	public void setUpdateCount(byte updateCount) {
		this.updateCount = updateCount;
	}

	public int getPeriodicity() {
		return periodicity;
	}

	public void setPeriodicity(int periodicity) {
		this.periodicity = periodicity;
	}

	public byte getOsVersionMajor() {
		return osVersionMajor;
	}

	public void setOsVersionMajor(byte osVersionMajor) {
		this.osVersionMajor = osVersionMajor;
	}

	public int getServerType() {
		return serverType;
	}

	public void setServerType(int serverType) {
		this.serverType = serverType;
	}

	public byte getBrowserVersionMajor() {
		return browserVersionMajor;
	}

	public void setBrowserVersionMajor(byte browserVersionMajor) {
		this.browserVersionMajor = browserVersionMajor;
	}

	public byte getBrowserversionMinor() {
		return browserversionMinor;
	}

	public void setBrowserversionMinor(byte browserversionMinor) {
		this.browserversionMinor = browserversionMinor;
	}

	public short getSignature() {
		return signature;
	}

	public void setSignature(short signature) {
		this.signature = signature;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	@Override
	public void parse(Buffer b, RpcUdpHeader h) {
		opcode = b.get();
		updateCount = b.get();
		periodicity = b.getInt();
		serverName = NetBiosNameCodec.readOemName(b, 16);
		osVersionMajor = b.get();
		serverType = ByteOrderConverter.swap(b.getInt());
		browserVersionMajor = b.get();
		browserversionMinor = b.get();
		signature = ByteOrderConverter.swap(b.getShort());
		comments = NetBiosNameCodec.readOemName(b);

	}

}
