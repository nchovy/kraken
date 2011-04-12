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
package org.krakenapps.pcap.decoder.ip;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.krakenapps.pcap.Injectable;
import org.krakenapps.pcap.PacketBuilder;
import org.krakenapps.pcap.decoder.ethernet.EthernetType;
import org.krakenapps.pcap.live.PcapDeviceManager;
import org.krakenapps.pcap.live.PcapDeviceMetadata;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ChainBuffer;
import org.krakenapps.pcap.util.IpConverter;

/**
 * IP Packet
 * 
 * @author mindori
 */
public class Ipv4Packet implements Injectable, IpPacket {
	private Object l2Frame;
	private int version;
	private int ihl;
	private int tos;
	private int totalLength;
	private int id;
	private int flags;
	private int fragmentOffset;
	private int ttl;
	private int protocol;
	private int headerChecksum;
	private int source;
	private int destination;
	private InetAddress sourceAddress;
	private InetAddress destinationAddress;
	private byte[] options; // This column need implement.
	private byte[] padding; // This column need implement.

	private Buffer data;

	private Ipv4Packet() {
	}

	@Override
	public Object getL2Frame() {
		return l2Frame;
	}

	public void setL2Frame(Object l2Frame) {
		this.l2Frame = l2Frame;
	}

	public static class Builder implements PacketBuilder {
		private Integer ihl = 20;
		private Integer tos = 0;
		private Integer totalLength;
		private Integer id = 1;
		private Integer fragmentOffset = 0;
		private Integer ttl = 128;
		private Integer protocol;
		private InetAddress srcIp;
		private InetAddress dstIp;
		private Buffer data;
		private PacketBuilder nextBuilder;

		public Builder id(int id) {
			this.id = id;
			return this;
		}

		public Builder src(String ip) {
			try {
				return src(InetAddress.getByName(ip));
			} catch (UnknownHostException e) {
				throw new IllegalArgumentException("invalid source ip format");
			}
		}

		public Builder src(InetAddress ip) {
			this.srcIp = ip;
			return this;
		}

		public Builder dst(String ip) {
			try {
				return dst(InetAddress.getByName(ip));
			} catch (UnknownHostException e) {
				throw new IllegalArgumentException("invalid destination ip format");
			}
		}

		public Builder dst(InetAddress ip) {
			this.dstIp = ip;
			return this;
		}

		public Builder proto(int protocol) {
			this.protocol = protocol;
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
			if (name.equals("src_ip")) {
				if (srcIp == null)
					return nextBuilder.getDefault(name);

				return srcIp;
			}

			if (name.equals("dst_ip")) {
				if (dstIp == null)
					return nextBuilder.getDefault(name);

				return dstIp;
			}

			if (name.equals("eth_type")) {
				return EthernetType.IPV4;
			}

			if (nextBuilder != null)
				return nextBuilder.getDefault(name);

			return null;
		}

		@Override
		public Ipv4Packet build() {
			// resolve
			if (dstIp == null) {
				dstIp = (InetAddress) getDefault("dst_ip");
				if (dstIp == null)
					throw new IllegalStateException("destination ip not found");
			}

			if (srcIp == null) {
				srcIp = (InetAddress) getDefault("src_ip");
				if (srcIp == null) {
					PcapDeviceMetadata metadata = PcapDeviceManager.getDeviceMetadata(dstIp);
					if (metadata == null)
						throw new IllegalStateException("interface not found");

					srcIp = metadata.getInet4Address();
				}
			}

			if (protocol == null) {
				protocol = (Integer) getDefault("ip_proto");
				if (protocol == null)
					throw new IllegalStateException("ip protocol is not specified");
			}

			// set
			Ipv4Packet p = new Ipv4Packet();
			p.version = 4;
			p.ihl = ihl;
			p.tos = tos;
			p.id = id;
			p.flags = 0;
			p.fragmentOffset = fragmentOffset;
			p.ttl = ttl;
			p.protocol = protocol;
			p.source = IpConverter.toInt((Inet4Address) srcIp);
			p.sourceAddress = srcIp;
			p.destination = IpConverter.toInt((Inet4Address) dstIp);
			p.destinationAddress = dstIp;
			p.data = data;
			if (data == null && nextBuilder != null)
				p.data = nextBuilder.build().getBuffer();

			int dlen = 0;
			if (p.data != null)
				dlen = p.data.readableBytes();

			if (totalLength != null)
				p.totalLength = totalLength;
			else
				p.totalLength = ihl + dlen;

			p.headerChecksum = IpChecksum.sum(p);
			return p;
		}
	}

	public static Ipv4Packet create(InetAddress source, InetAddress destination, Buffer buffer) {
		Ipv4Packet p = new Ipv4Packet();

		return p;
	}

	public static Ipv4Packet parse(Buffer buffer) {
		Ipv4Packet p = new Ipv4Packet();
		p.parseVersionIhl(buffer);
		p.tos = (buffer.get() & 0xff);
		p.totalLength = (buffer.getUnsignedShort());
		p.id = (buffer.getUnsignedShort());
		p.parseFlagsFragOffset(buffer);
		p.ttl = (buffer.get() & 0xff);
		p.protocol = (buffer.get() & 0xff);
		p.headerChecksum = (buffer.getUnsignedShort());
		p.source = buffer.getInt();
		p.sourceAddress = IpConverter.toInetAddress(p.source);
		p.destination = buffer.getInt();
		p.destinationAddress = IpConverter.toInetAddress(p.destination);
		p.parseOptions(buffer);

		/* need discard */
		p.data = buffer;
		p.data.discardReadBytes();
		return p;
	}

	private void parseVersionIhl(Buffer data) {
		byte b = data.get();
		short s = (short) (b & 0x00ff);
		version = (int) ((s >> 4) & 0xf);
		ihl = (int) ((s & 0x000f) * 4);
	}

	private void parseFlagsFragOffset(Buffer data) {
		short flagsAndFragmentOffset = data.getShort();
		flags = (int) (((short) ((flagsAndFragmentOffset >> 13) & 0x0007)) & 0xffff);
		fragmentOffset = (int) ((short) (flagsAndFragmentOffset & 0x1fff) & 0xffff);
	}

	private void parseOptions(Buffer data) {
		if (ihl <= 20)
			return;

		int optionLength = ihl - 20;
		options = new byte[optionLength];

		for (int i = 0; i < optionLength; i++)
			options[i] = data.get();

		if ((optionLength % 4) == 0)
			return;

		for (int i = 0; i < optionLength % 4; i++)
			padding[i] = data.get();
	}

	public static Ipv4Packet makeReassembled(Ipv4Packet other, Buffer data, int totalLength) {
		Ipv4Packet p = new Ipv4Packet();

		p.version = other.version;
		p.ihl = other.ihl;
		p.tos = other.tos;
		p.totalLength = totalLength;
		p.id = other.id;
		p.flags = 0;
		p.fragmentOffset = 0;
		p.ttl = other.ttl;
		p.protocol = other.protocol;
		p.headerChecksum = other.headerChecksum;
		p.source = other.source;
		p.destination = other.destination;
		p.sourceAddress = other.sourceAddress;
		p.destinationAddress = other.destinationAddress;

		p.data = data;
		if (other.options != null && other.padding != null) {
			p.options = Arrays.copyOf(other.options, other.options.length);
			p.padding = Arrays.copyOf(other.padding, other.padding.length);
		}

		return p;
	}

	@Override
	public int getVersion() {
		return version;
	}

	public int getIhl() {
		return ihl;
	}

	public int getTos() {
		return tos;
	}

	public int getTotalLength() {
		return totalLength;
	}

	public int getId() {
		return id;
	}

	public int getFlags() {
		return flags;
	}

	public int getFragmentOffset() {
		return fragmentOffset;
	}

	public int getTtl() {
		return ttl;
	}

	public int getProtocol() {
		return protocol;
	}

	public int getHeaderChecksum() {
		return headerChecksum;
	}

	public int getSource() {
		return source;
	}

	@Override
	public InetAddress getSourceAddress() {
		return sourceAddress;
	}

	public int getDestination() {
		return destination;
	}

	@Override
	public InetAddress getDestinationAddress() {
		return destinationAddress;
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
	public Buffer getBuffer() {
		ByteBuffer hbuf = ByteBuffer.allocate(20);
		hbuf.put((byte) 0x45); // version and ihl hardcoded
		hbuf.put((byte) 0);
		hbuf.putShort((short) totalLength);
		hbuf.putShort((short) id);
		hbuf.putShort((short) 0); // frag offset not supported yet
		hbuf.put((byte) ttl);
		hbuf.put((byte) protocol);
		hbuf.putShort((short) headerChecksum);
		hbuf.putInt(source);
		hbuf.putInt(destination);

		Buffer buf = new ChainBuffer();
		buf.addLast(hbuf.array());
		buf.addLast(data);
		return buf;
	}

	@Override
	public String toString() {
		return String
				.format(
						"ip {%s > %s - version: %s, header_length: %d, total_length: %d, id: %d, fragment_offset: %d, ttl: %d, header_checksum: 0x%02X}",
						sourceAddress.toString().substring(1), destinationAddress.toString().substring(1),
						getVersion(), ihl,
						totalLength, id, fragmentOffset, ttl, headerChecksum);
	}
}