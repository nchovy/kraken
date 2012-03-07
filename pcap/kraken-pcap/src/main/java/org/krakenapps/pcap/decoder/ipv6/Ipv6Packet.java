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
package org.krakenapps.pcap.decoder.ipv6;

import java.net.InetAddress;

import org.krakenapps.pcap.decoder.ip.IpPacket;
import org.krakenapps.pcap.util.Buffer;

/**
 * @author xeraph
 */
public class Ipv6Packet implements IpPacket {
	private Object l2Frame;
	private byte trafficClass;
	private int flowLabel; // 20 bits
	private int payloadLength; // 2 bytes
	private byte nextHeader; // 1 byte
	private int hopLimit; // 1 byte
	private InetAddress source;
	private InetAddress destination;
	private Buffer data;

	@Override
	public Object getL2Frame() {
		return l2Frame;
	}

	public void setL2Frame(Object l2Frame) {
		this.l2Frame = l2Frame;
	}

	@Override
	public int getVersion() {
		return 6;
	}

	public byte getTrafficClass() {
		return trafficClass;
	}

	public void setTrafficClass(byte trafficClass) {
		this.trafficClass = trafficClass;
	}

	public int getFlowLabel() {
		return flowLabel;
	}

	public void setFlowLabel(int flowLabel) {
		this.flowLabel = flowLabel;
	}

	public int getPayloadLength() {
		return payloadLength;
	}

	public void setPayloadLength(int payloadLength) {
		this.payloadLength = payloadLength;
	}

	public byte getNextHeader() {
		return nextHeader;
	}

	public void setNextHeader(byte nextHeader) {
		this.nextHeader = nextHeader;
	}

	public int getHopLimit() {
		return hopLimit;
	}

	public void setHopLimit(int hopLimit) {
		this.hopLimit = hopLimit;
	}

	@Override
	public InetAddress getSourceAddress() {
		return source;
	}

	public void setSource(InetAddress source) {
		this.source = source;
	}

	@Override
	public InetAddress getDestinationAddress() {
		return destination;
	}

	public void setDestination(InetAddress destination) {
		this.destination = destination;
	}

	@Override
	public Buffer getData() {
		return data;
	}

	public void setData(Buffer data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return String.format("ipv6 %s -> %s", source.getCanonicalHostName(), destination.getCanonicalHostName());
	}
}
