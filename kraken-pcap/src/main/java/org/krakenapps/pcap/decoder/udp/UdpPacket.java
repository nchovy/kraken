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

import org.krakenapps.pcap.decoder.ip.IpPacket;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ChainBuffer;

/**
 * @author mindori
 */
public class UdpPacket {
	private IpPacket ipPacket;
	private InetSocketAddress source;
	private InetSocketAddress destination;
	private int length;
	private int checksum;
	private Buffer data;

	public UdpPacket(IpPacket ipPacket, int sourcePort, int destinationPort) {
		this(ipPacket.getSourceAddress(), sourcePort, ipPacket.getDestinationAddress(), destinationPort);
		this.ipPacket = ipPacket;
	}

	public UdpPacket(InetAddress sourceIp, int sourcePort, InetAddress destinationIp,
			int destinationPort) {
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
	public String toString() {
		return String.format("udp {%s:%d > %s:%d - length: %d, checksum: 0x%02x}", source
				.getAddress().getHostAddress(), source.getPort(), destination.getAddress()
				.getHostAddress(), destination.getPort(), length, checksum);
	}
}