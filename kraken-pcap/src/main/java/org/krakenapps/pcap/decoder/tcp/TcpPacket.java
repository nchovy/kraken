/*
 * Copyright 2010 NCHOVY
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.pcap.decoder.tcp;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.krakenapps.pcap.Injectable;
import org.krakenapps.pcap.PacketBuilder;
import org.krakenapps.pcap.decoder.ip.InternetProtocol;
import org.krakenapps.pcap.decoder.ip.IpPacket;
import org.krakenapps.pcap.decoder.ip.Ipv4Packet;
import org.krakenapps.pcap.decoder.ipv6.Ipv6Packet;
import org.krakenapps.pcap.live.PcapDeviceManager;
import org.krakenapps.pcap.live.PcapDeviceMetadata;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ChainBuffer;

/**
 * @author mindori
 */
public class TcpPacket implements TcpSegment, Injectable {
	private IpPacket ipPacket;
	private TcpSessionKeyImpl sessionKey;
	private InetAddress sourceAddr;
	private InetAddress destinationAddr;

	/**
	 * tcp header + data
	 */
	private int tcpLength;
	private TcpDirection direction;

	private int srcPort;
	private int dstPort;
	private int seq;
	private int ack;
	private int relativeSeq;
	private int relativeAck;

	/**
	 * header length = data start offset
	 */
	private int dataOffset;
	private int flags;
	private int window;
	private int checksum;
	private int urgentPointer;
	private byte[] options;
	private byte[] padding;
	private Buffer data;
	private int dataLength;

	private boolean isJumbo = false;
	private boolean isGarbage = false;
	private int reassembledLength = 0;

	private TcpPacket() {
	}

	/* copy constructor */
	public TcpPacket(TcpPacket other) {
		ipPacket = other.ipPacket;
		sessionKey = (TcpSessionKeyImpl) other.getSessionKey();
		sourceAddr = other.getSourceAddress();
		destinationAddr = other.getDestinationAddress();

		tcpLength = other.getTotalLength();
		direction = other.getDirection();

		srcPort = other.getSourcePort();
		dstPort = other.getDestinationPort();

		seq = other.getSeq();
		ack = other.getAck();
		relativeSeq = other.getRelativeSeq();
		relativeAck = other.getRelativeAck();

		dataOffset = other.getDataOffset();
		flags = other.getFlags();
		window = other.getWindow();
		checksum = other.getChecksum();
		urgentPointer = other.getUrgentPointer();
		options = other.getOptions();
		padding = other.getPadding();

		if (other.getData() != null)
			data = new ChainBuffer(other.getData());
		dataLength = other.getDataLength();
	}

	public static TcpPacket parse(Ipv4Packet p) throws BufferUnderflowException {
		InetAddress source = p.getSourceAddress();
		InetAddress destination = p.getDestinationAddress();
		Buffer data = p.getData();
		int tcpLength = p.getTotalLength() - p.getIhl();

		return parse(p, source, destination, tcpLength, data);
	}
	
	public static TcpPacket parse(Ipv6Packet p) throws BufferUnderflowException {
		InetAddress source = p.getSourceAddress();
		InetAddress destination = p.getDestinationAddress();
		int tcpLength = p.getPayloadLength() - 40;
		Buffer data = p.getData();

		return parse(p, source, destination, tcpLength, data);
	}

	private static TcpPacket parse(IpPacket p, InetAddress source, InetAddress destination, int tcpLength, Buffer data) throws BufferUnderflowException {
		TcpPacket s = new TcpPacket();

		s.ipPacket = p;
		s.tcpLength = tcpLength;
		s.sourceAddr = source;
		s.destinationAddr = destination;
		s.srcPort = data.getUnsignedShort();
		s.dstPort = data.getUnsignedShort();

		s.sessionKey = new TcpSessionKeyImpl(s.sourceAddr, s.destinationAddr, s.srcPort, s.dstPort);

		s.seq = data.getInt();
		s.ack = data.getInt();
		s.parseDataOffsetAndFlags(data);
		s.window = data.getUnsignedShort();
		s.checksum = data.getUnsignedShort();
		s.urgentPointer = data.getUnsignedShort();
		s.parseOptions(data);
		s.parseData(data);

		return s;
	}
	
	private void parseDataOffsetAndFlags(Buffer dataBuffer) {
		byte dataOffsetAndReserved = dataBuffer.get();
		byte reservedAndFlags = dataBuffer.get();
		dataOffset = ((dataOffsetAndReserved >> 4) & 0x0f) & 0xff;
		flags = (reservedAndFlags & 0x3f) & 0xff;
	}

	private void parseOptions(Buffer dataBuffer) {
		int headerLength = dataOffset * 4;
		if (headerLength <= 20)
			return;

		int optionLength = headerLength - 20;
		options = new byte[optionLength];

		for (int i = 0; i < optionLength; i++)
			options[i] = dataBuffer.get();

		if ((optionLength % 4) == 0)
			return;

		for (int i = 0; i < (optionLength % 4); i++)
			padding[i] = dataBuffer.get();
	}

	private void parseData(Buffer dataBuffer) throws BufferUnderflowException {
		/* set length of data */
		dataLength = tcpLength - (dataOffset * 4);

		if (dataLength > 0) {
			try {
				dataBuffer.discardReadBytes();
				data = dataBuffer;
			} catch (BufferUnderflowException e) {
				data = null;
				isJumbo = true;
			}
		}
	}

	@Override
	public IpPacket getIpPacket() {
		return ipPacket;
	}

	public void setIpPacket(IpPacket ipPacket) {
		this.ipPacket = ipPacket;
	}

	@Override
	public boolean isSyn() {
		return (flags & 0x02) != 0;
	}

	@Override
	public boolean isAck() {
		return (flags & 0x10) != 0;
	}

	@Override
	public boolean isPsh() {
		return (flags & 0x08) != 0;
	}

	@Override
	public boolean isFin() {
		return (flags & 0x01) != 0;
	}

	@Override
	public boolean isRst() {
		return (flags & 0x04) != 0;
	}

	@Override
	public boolean isUrg() {
		return (flags & 0x20) != 0;
	}

	@Override
	public TcpSessionKey getSessionKey() {
		return sessionKey;
	}

	@Override
	public InetSocketAddress getSource() {
		return new InetSocketAddress(getSourceAddress(), getSourcePort());
	}

	@Override
	public InetSocketAddress getDestination() {
		return new InetSocketAddress(getDestinationAddress(), getDestinationPort());
	}

	@Override
	public InetAddress getSourceAddress() {
		return sourceAddr;
	}

	@Override
	public InetAddress getDestinationAddress() {
		return destinationAddr;
	}

	@Override
	public int getTotalLength() {
		return tcpLength;
	}

	@Override
	public int getSourcePort() {
		return srcPort;
	}

	@Override
	public int getDestinationPort() {
		return dstPort;
	}

	@Override
	public int getSeq() {
		return seq;
	}

	@Override
	public int getAck() {
		return ack;
	}

	@Override
	public int getRelativeSeq() {
		return relativeSeq;
	}

	public void setRelativeSeq(int relativeSeq) {
		this.relativeSeq = relativeSeq;
	}

	@Override
	public int getRelativeAck() {
		return relativeAck;
	}

	public void setRelativeAck(int relativeAck) {
		this.relativeAck = relativeAck;
	}

	/**
	 * Header Length = dataOffset * 4
	 * 
	 * @return
	 */
	public int getDataOffset() {
		return dataOffset;
	}

	public int getFlags() {
		return flags;
	}

	public int getWindow() {
		return window;
	}

	public int getChecksum() {
		return checksum;
	}

	public int getUrgentPointer() {
		return urgentPointer;
	}

	public byte[] getOptions() {
		return options;
	}

	public byte[] getPadding() {
		return padding;
	}

	@Override
	public Buffer getData() {
		return data;
	}

	@Override
	public TcpDirection getDirection() {
		return direction;
	}

	public void setDirection(TcpSession session) {
		if (session.getKey().getClientPort() == srcPort) {
			direction = TcpDirection.ToServer;
		} else {
			direction = TcpDirection.ToClient;
			this.sessionKey.flip();
		}
	}

	public void setDirection(TcpSessionImpl session) {
		if (session.getKey().getClientPort() == srcPort) {
			direction = TcpDirection.ToServer;
		} else {
			direction = TcpDirection.ToClient;
			this.sessionKey.flip();
		}
	}

	public int getDataLength() {
		return dataLength;
	}

	public boolean isJumbo() {
		return isJumbo;
	}

	public boolean isGarbage() {
		return isGarbage;
	}

	public void setGarbage(boolean isGarbage) {
		this.isGarbage = isGarbage;
	}

	public int getReassembledLength() {
		return reassembledLength;
	}

	public void setReassembledLength(int reassembledLength) {
		this.reassembledLength = reassembledLength;
	}

	@Override
	public String toString() {
		if (getRelativeSeq() == -1 && getRelativeAck() == -1)
			return String.format("tcp {%s:%d > %s:%d - %s window: %d, urgent: %d}", sourceAddr.getHostAddress(), getSourcePort(), destinationAddr.getHostAddress(), getDestinationPort(), getFlagSymbol(flags), window, urgentPointer);
		else if (getRelativeAck() == -1)
			return String.format("tcp {%s:%d > %s:%d - %s seq: %d, window: %d, urgent: %d}", sourceAddr.getHostAddress(), getSourcePort(), destinationAddr.getHostAddress(), getDestinationPort(), getFlagSymbol(flags), getRelativeSeq(), window, urgentPointer);
		else
			return String.format("tcp {%s:%d > %s:%d - %s seq: %d, ack: %d, window: %d, urgent: %d}", sourceAddr.getHostAddress(), getSourcePort(), destinationAddr.getHostAddress(), getDestinationPort(), getFlagSymbol(flags), getRelativeSeq(), getRelativeAck(), window, urgentPointer);
	}

	private String getFlagSymbol(int flags) {
		if (isUrg())
			return "Urg";
		else if (isPsh())
			return "P";
		else if (isRst())
			return "R";
		else if (isSyn())
			return "S";
		else if (isFin())
			return "F";
		else
			return ".";
	}

	@Override
	public Buffer getBuffer() {
		ByteBuffer hbuf = ByteBuffer.allocate(20); // hardcoded header size
		hbuf.putShort((short) srcPort);
		hbuf.putShort((short) dstPort);
		hbuf.putInt(seq);
		hbuf.putInt(ack);
		hbuf.put((byte) (dataOffset << 4));
		hbuf.put((byte) flags);
		hbuf.putShort((short) window);
		hbuf.putShort((short) checksum);
		hbuf.putShort((short) urgentPointer);

		Buffer buf = new ChainBuffer(hbuf.array());
		buf.addLast(data);
		return buf;
	}

	public static class Builder implements PacketBuilder {
		private InetAddress srcIp;
		private InetAddress dstIp;
		private Integer srcPort;
		private Integer dstPort;
		private Integer seq = 1;
		private Integer ack = 0;
		private Integer flags = 0;
		private Integer window = 8192;
		private Buffer data;
		private PacketBuilder nextBuilder;

		public Builder syn() {
			this.flags |= TcpFlag.SYN;
			return this;
		}

		public Builder ack() {
			this.flags |= TcpFlag.ACK;
			return this;
		}

		public Builder fin() {
			this.flags |= TcpFlag.FIN;
			return this;
		}

		public Builder rst() {
			this.flags |= TcpFlag.RST;
			return this;
		}

		public Builder urg() {
			this.flags |= TcpFlag.URG;
			return this;
		}

		public Builder psh() {
			this.flags |= TcpFlag.PSH;
			return this;
		}

		public Builder src(InetSocketAddress addr) {
			return src(addr.getAddress(), addr.getPort());
		}

		public Builder src(String ip, int port) {
			try {
				return src(InetAddress.getByName(ip), port);
			} catch (UnknownHostException e) {
				throw new IllegalArgumentException("invalid ip format");
			}
		}

		public Builder src(InetAddress ip, int port) {
			this.srcIp = ip;
			this.srcPort = port;
			return this;
		}

		public Builder dst(InetSocketAddress addr) {
			return dst(addr.getAddress(), addr.getPort());
		}

		public Builder dst(String ip, int port) {
			try {
				return dst(InetAddress.getByName(ip), port);
			} catch (UnknownHostException e) {
				throw new IllegalArgumentException("invalid ip format");
			}
		}

		public Builder dst(InetAddress ip, int port) {
			this.dstIp = ip;
			this.dstPort = port;
			return this;
		}

		public Builder seq(int seq) {
			this.seq = seq;
			return this;
		}

		public Builder ack(int ack) {
			this.ack = ack;
			return this;
		}

		public Builder window(int window) {
			this.window = window;
			return this;
		}

		public Builder data(Buffer data) {
			this.data = data;
			return this;
		}

		public Builder data(PacketBuilder builder) {
			this.nextBuilder = builder;
			return this;
		}

		@Override
		public Object getDefault(String name) {
			if (name.equals("src_ip"))
				return srcIp;

			if (name.equals("dst_ip"))
				return dstIp;

			if (name.equals("ip_proto"))
				return InternetProtocol.TCP;

			if (name.equals("src_port"))
				return srcPort;

			if (name.equals("dst_port"))
				return dstPort;

			if (nextBuilder != null)
				return nextBuilder.getDefault(name);

			return null;
		}

		@Override
		public TcpPacket build() {
			// resolve all values
			if (dstIp == null)
				throw new IllegalStateException("destination ip not found");

			if (srcIp == null) {
				PcapDeviceMetadata metadata = PcapDeviceManager.getDeviceMetadata(dstIp);
				if (metadata == null)
					throw new IllegalArgumentException("route not found for destination " + dstIp.getHostAddress());

				srcIp = metadata.getInet4Address();
			}

			if (srcPort == null)
				srcPort = 40000;

			if (dstPort == null)
				throw new IllegalStateException("destination port not found");

			// set
			TcpPacket p = new TcpPacket();
			p.sourceAddr = srcIp;
			p.destinationAddr = dstIp;
			p.srcPort = srcPort;
			p.dstPort = dstPort;
			p.seq = seq;
			p.ack = ack;
			p.dataOffset = 5; // default header size 20 (5 * 4)
			p.flags = flags;
			p.window = window;
			p.data = data;
			if (data != null)
				p.dataLength = data.readableBytes();

			p.tcpLength = (p.dataOffset * 4) + p.dataLength;
			p.checksum = TcpChecksum.sum(p);
			return p;
		}
	}
}