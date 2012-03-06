package org.krakenapps.pcap.decoder.rpce;

import org.krakenapps.pcap.decoder.rpce.tcppacket.association.TcpPDUInterface;

public class RpcTcpPacket {

	private RpcTcpHeader header;
	private TcpPDUInterface data;
	public RpcTcpPacket() {
	}
	public RpcTcpHeader getHeader() {
		return header;
	}
	public void setHeader(RpcTcpHeader header) {
		this.header = header;
	}
	public TcpPDUInterface getData() {
		return data;
	}
	public void setData(TcpPDUInterface data2) {
		this.data = data2;
	}
	@Override
	public String toString() {
		
		return new String(header.toString()+data.toString());
	}
	
}
