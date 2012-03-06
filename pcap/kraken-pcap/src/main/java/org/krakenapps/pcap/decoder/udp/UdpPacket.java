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
package org.krakenapps.pcap.decoder.udp;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.krakenapps.pcap.Injectable;
import org.krakenapps.pcap.PacketBuilder;
import org.krakenapps.pcap.decoder.ip.InternetProtocol;
import org.krakenapps.pcap.decoder.ip.IpPacket;
import org.krakenapps.pcap.live.PcapDeviceManager;
import org.krakenapps.pcap.live.PcapDeviceMetadata;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ChainBuffer;

/**
 * @author mindori
 */
public class UdpPacket implements Injectable {
	private IpPacket ipPacket;
	private InetSocketAddress source;
	private InetSocketAddress destination;
	private int length;
	private int checksum;
	private Buffer data;
	
	private UdpPacket() {
	}

	public UdpPacket(IpPacket ipPacket, int sourcePort, int destinationPort) {
		this(ipPacket.getSourceAddress(), sourcePort, ipPacket.getDestinationAddress(), destinationPort);
		this.ipPacket = ipPacket;
	}

	public UdpPacket(InetAddress sourceIp, int sourcePort, InetAddress destinationIp, int destinationPort) {
		this.source = new InetSocketAddress(sourceIp, sourcePort);
		this.destination = new InetSocketAddress(destinationIp, destinationPort);
	}

	/* copy constructor */
	public UdpPacket(UdpPacket other) {
		ipPacket = other.getIpPacket();
		source = other.getSource();
		destination = other.getDestination();
		length = other.getLength();
		checksum = other.getChecksum();
		data = new ChainBuffer(other.getData());
	}

	public IpPacket getIpPacket() {
		return ipPacket;
	}

	public InetSocketAddress getSource() {
		return source;
	}

	public InetSocketAddress getDestination() {
		return destination;
	}

	public int getSourcePort() {
		return source.getPort();
	}

	public int getDestinationPort() {
		return destination.getPort();
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getChecksum() {
		return checksum;
	}

	public void setChecksum(int checksum) {
		this.checksum = checksum;
	}

	public Buffer getData() {
		return data;
	}

	public void setData(Buffer data) {
		this.data = data;
	}

	@Override
	public Buffer getBuffer() {
		ByteBuffer hbuf = ByteBuffer.allocate(8);
		hbuf.putShort((short) source.getPort());
		hbuf.putShort((short) destination.getPort());
		hbuf.putShort((short) length);
		hbuf.putShort((short) checksum);

		Buffer buf = new ChainBuffer(hbuf.array());
		buf.addLast(data);
		return buf;
	}

	@Override
	public String toString() {
		return String.format("udp {%s:%d > %s:%d - length: %d, checksum: 0x%02x}",
				source.getAddress().getHostAddress(), source.getPort(), destination.getAddress().getHostAddress(),
				destination.getPort(), length, checksum);
	}

	public static class Builder implements PacketBuilder {
		private InetAddress srcIp;
		private InetAddress dstIp;
		private Integer srcPort;
		private Integer dstPort;
		private Buffer data;
		private PacketBuilder nextBuilder;

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

		public Builder data(Buffer data) {
			this.data = data;
			return this;
		}
		
		public Builder data(PacketBuilder builder) {
			this.nextBuilder = builder;
			return this;
		}

		@Override
		public Injectable build() {
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

			UdpPacket p = new UdpPacket();
			p.source = new InetSocketAddress(srcIp, srcPort);
			p.destination = new InetSocketAddress(dstIp, dstPort);
			p.length = data.readableBytes() + 8;
			p.data = data;
			p.checksum = UdpChecksum.sum(p);
			
			return p;
		}

		@Override
		public Object getDefault(String name) {
			if (name.equals("src_ip"))
				return srcIp;

			if (name.equals("dst_ip"))
				return dstIp;

			if (name.equals("ip_proto"))
				return InternetProtocol.UDP;

			if (name.equals("src_port"))
				return srcPort;

			if (name.equals("dst_port"))
				return dstPort;

			if (nextBuilder != null)
				return nextBuilder.getDefault(name);

			return null;
		}
	}
}