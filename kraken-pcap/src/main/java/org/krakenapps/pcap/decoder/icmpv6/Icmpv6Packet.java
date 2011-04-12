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
package org.krakenapps.pcap.decoder.icmpv6;

import java.net.InetAddress;

import org.krakenapps.pcap.decoder.ipv6.Ipv6Packet;
import org.krakenapps.pcap.util.Buffer;

/**
 * @author xeraph
 */
public class Icmpv6Packet {
	private Ipv6Packet ipPacket;
	private InetAddress source;
	private InetAddress destination;
	private int type;
	private int code;
	private int checksum;
	private Buffer data;

	public Ipv6Packet getIpPacket() {
		return ipPacket;
	}

	public void setIpPacket(Ipv6Packet ipPacket) {
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

	public Buffer getData() {
		return data;
	}

	public void setData(Buffer data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return String.format("ICMPv6 %s (%s -> %s)", Icmpv6Message.getMessage(type), source.getCanonicalHostName(),
				destination.getCanonicalHostName());
	}
}
