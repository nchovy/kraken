package org.krakenapps.pcap.decoder.netbios;

import java.net.InetAddress;
import java.nio.ByteBuffer;

import org.krakenapps.pcap.Injectable;
import org.krakenapps.pcap.decoder.netbios.rr.DatagramHeader;
import org.krakenapps.pcap.decoder.udp.UdpPacket;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ChainBuffer;

public class NetBiosDatagramPacket implements Injectable {
	private UdpPacket udpPacket;
	private DatagramHeader header;
	private DatagramData data;

	public NetBiosDatagramPacket(DatagramHeader header, DatagramData data) {
		this.header = header;
		this.data = data;
	}

	public UdpPacket getUdpPacket() {
		return udpPacket;
	}

	public void setUdpPacket(UdpPacket udpPacket) {
		this.udpPacket = udpPacket;
	}

	public DatagramHeader getHeader() {
		return header;
	}

	public DatagramData getData() {
		return data;
	}

	@Override
	public Buffer getBuffer() {
		ByteBuffer headerb = ByteBuffer.allocate(10);
		Buffer buffer = new ChainBuffer();
		/*
		protected NetBiosDatagramType msgType;
		protected byte flags;
		protected short dgmID;
		protected InetAddress addresses;
		protected short port;*/  //this section is  common field
		buffer.addLast(headerb.array());
		return buffer;
	}

	@Override
	public String toString() {
		return header.toString() + data.toString();
	}
}
