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
package org.krakenapps.pcap.decoder.ethernet;

import java.io.IOException;
import java.net.InetAddress;

import org.krakenapps.pcap.Injectable;
import org.krakenapps.pcap.PacketBuilder;
import org.krakenapps.pcap.live.PcapDeviceManager;
import org.krakenapps.pcap.live.PcapDeviceMetadata;
import org.krakenapps.pcap.packet.PcapPacket;
import org.krakenapps.pcap.util.Arping;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ChainBuffer;

/**
 * Ethernet frame
 * 
 * @author mindori
 */
public class EthernetFrame implements Injectable {
	private final EthernetHeader header;
	private final Buffer payload;
	private PcapPacket pcapPacket;

	public EthernetFrame(MacAddress source, MacAddress destination, int type, Buffer payload) {
		this.header = new EthernetHeader(source, destination, type);
		this.payload = payload;
	}

	public EthernetFrame(EthernetHeader header, Buffer payload) {
		this.header = header;
		this.payload = payload;
	}

	public EthernetFrame(EthernetFrame other) { // copy constructor
		this.header = other.header;
		this.payload = other.payload;
	}

	public PcapPacket getPcapPacket() {
		return pcapPacket;
	}

	public void setPcapPacket(PcapPacket pcapPacket) {
		this.pcapPacket = pcapPacket;
	}

	public MacAddress getDestination() {
		return header.getDestination();
	}

	public MacAddress getSource() {
		return header.getSource();
	}

	public int getType() {
		return header.getType();
	}

	public Buffer getData() {
		return payload;
	}

	public EthernetFrame dup() {
		ChainBuffer buf = new ChainBuffer();
		buf.addLast(payload);
		EthernetFrame f = new EthernetFrame(this.getSource(), this.getDestination(), this.getType(), buf);
		f.setPcapPacket(pcapPacket);
		return f;
	}

	@Override
	public String toString() {
		return String.format("ethernet {dst: %s, src: %s, type: 0x%X}", getDestination(), getSource(), getType());
	}

	@Override
	public Buffer getBuffer() {
		Buffer buf = new ChainBuffer();
		buf.addLast(header.getBuffer());
		buf.addLast(payload);
		return buf;
	}

	public static class Builder implements PacketBuilder {

		private MacAddress dstMac;
		private MacAddress srcMac;
		private Integer type;
		private Buffer data;
		private PacketBuilder nextBuilder;

		public Builder dst(MacAddress dst) {
			this.dstMac = dst;
			return this;
		}

		public Builder src(MacAddress src) {
			this.srcMac = src;
			return this;
		}

		public Builder type(int type) {
			this.type = type;
			return this;
		}

		public Builder data(PacketBuilder builder) {
			this.nextBuilder = builder;
			return this;
		}

		public Builder data(Buffer data) {
			this.data = data;
			return this;
		}

		@Override
		public Injectable build() {
			// resolve
			InetAddress dstIp = (InetAddress) getDefault("dst_ip");
			if (dstMac == null && getDefault("dst_mac") == null) {
				if (dstIp == null)
					throw new IllegalStateException("destination ip not found");

				try {
					this.dstMac = Arping.query(dstIp, 1000);
					if (dstMac == null)
						throw new IllegalStateException("destination mac not resolved, arping timeout");
				} catch (IOException e) {
					throw new IllegalStateException("destination mac not found");
				}
			}

			if (srcMac == null) {
				PcapDeviceMetadata metadata = PcapDeviceManager.getDeviceMetadata(dstIp);
				srcMac = metadata.getMacAddress();
			}

			if (type == null) {
				type = (Integer) getDefault("eth_type");
				if (type == null)
					throw new IllegalStateException("ether type not found");
			}

			Buffer buffer = data;
			if (buffer == null)
				buffer = nextBuilder.build().getBuffer();

			return new EthernetFrame(srcMac, dstMac, type, buffer);
		}

		@Override
		public Object getDefault(String name) {
			if (name.equals("src_mac"))
				return srcMac;

			if (name.equals("dst_mac"))
				return dstMac;

			if (nextBuilder != null)
				return nextBuilder.getDefault(name);

			return null;
		}

	}
}
