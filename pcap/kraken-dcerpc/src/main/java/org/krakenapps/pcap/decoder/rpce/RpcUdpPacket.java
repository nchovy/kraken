package org.krakenapps.pcap.decoder.rpce;

import org.krakenapps.pcap.decoder.rpce.packet.UdpPDUInterface;

public class RpcUdpPacket {

	private RpcUdpHeader header;
	private UdpPDUInterface data;
	public RpcUdpPacket() {
	}
	public RpcUdpHeader getHeader() {
		return header;
	}
	public void setHeader(RpcUdpHeader header) {
		this.header = header;
	}
	public UdpPDUInterface getData() {
		return data;
	}
	public void setData(UdpPDUInterface data) {
		this.data = data;
	}
	@Override
	public String toString() {
		return new String(header.toString() + data.toString());
	}
}
