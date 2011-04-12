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
package org.krakenapps.pcap.decoder.icmp;

import java.net.InetAddress;
import java.nio.ByteBuffer;

import org.krakenapps.pcap.Injectable;
import org.krakenapps.pcap.PacketBuilder;
import org.krakenapps.pcap.decoder.ip.Ipv4Packet;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ChainBuffer;

/**
 * ICMP Packet
 * 
 * @author xeraph
 */
public class IcmpPacket implements Injectable {
	private Ipv4Packet ipPacket;
	private InetAddress source;
	private InetAddress destination;
	private int type;
	private int code;
	private int checksum;
	private int id;
	private int seq;
	private Buffer data;

	public IcmpPacket() {
	}

	/* copy constructor */
	public IcmpPacket(IcmpPacket other) {
		source = other.getSource();
		destination = other.getDestination();
		type = other.getType();
		code = other.getCode();
		checksum = other.getChecksum();
		id = other.getId();
		seq = other.getSeq();
		data = new ChainBuffer(other.getData());
	}

	public static class Builder implements PacketBuilder {
		private Integer type = 8;
		private Integer code = 0;
		private Integer id = 1;
		private Integer seq = 1;
		private Buffer data;
		private PacketBuilder nextBuilder;

		public Builder type(int type) {
			this.type = type;
			return this;
		}

		public Builder code(int code) {
			this.code = code;
			return this;
		}

		public Builder id(int id) {
			this.id = id;
			return this;
		}

		public Builder seq(int seq) {
			this.seq = seq;
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
			if (nextBuilder != null)
				nextBuilder.getDefault(name);

			return null;
		}

		@Override
		public IcmpPacket build() {
			// set
			IcmpPacket p = new IcmpPacket();
			p.type = type;
			p.code = code;
			p.id = id;
			p.seq = seq;
			p.data = data;

			if (p.data == null && nextBuilder != null)
				p.data = nextBuilder.build().getBuffer();

			return p;
		}
	}

	public Ipv4Packet getIpPacket() {
		return ipPacket;
	}

	public void setIpPacket(Ipv4Packet ipPacket) {
		this.ipPacket = ipPacket;
	}

	public InetAddress getSource() {
		return source;
	}

	public void setSource(InetAddress source) {
		this.source = source;
	}

	public InetAddress getDestination() {
		return destination;
	}

	public void setDestination(InetAddress destination) {
		this.destination = destination;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public int getChecksum() {
		return checksum;
	}

	public void setChecksum(int checksum) {
		this.checksum = checksum;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public Buffer getData() {
		return data;
	}

	public void setData(Buffer data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return String.format("%s, source=%s, destination=%s, id=%d, seq=%d", IcmpMessage.getMessage(type, code),
				source.getHostAddress(), destination.getHostAddress(), id, seq);
	}

	public Buffer getBuffer() {
		int sum = IcmpChecksum.sum(this);

		ByteBuffer hbuf = ByteBuffer.allocate(8);
		hbuf.put((byte) type);
		hbuf.put((byte) code);
		hbuf.putShort((short) sum);
		hbuf.putShort((short) id);
		hbuf.putShort((short) seq);

		Buffer buf = new ChainBuffer();
		buf.addLast(hbuf.array());
		buf.addLast(data);
		return buf;
	}
}
