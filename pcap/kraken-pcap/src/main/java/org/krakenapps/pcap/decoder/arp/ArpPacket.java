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
package org.krakenapps.pcap.decoder.arp;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;

import org.krakenapps.pcap.Injectable;
import org.krakenapps.pcap.decoder.ethernet.MacAddress;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ChainBuffer;
import org.krakenapps.pcap.util.IpConverter;

/**
 * ARP packet.
 * 
 * @see http://en.wikipedia.org/wiki/Address_Resolution_Protocol
 * @author xeraph
 * @since 1.1
 */
public class ArpPacket implements Injectable {
	private Object l2Frame;

	private int hardwareType; // should be 0x0001
	private int protocolType; // should be 0x0800
	private int hardwareSize; // should be 6
	private int protocolSize; // should be 4
	private int opcode; // request(1), reply(2)
	private MacAddress senderMac;
	private InetAddress senderIp;
	private MacAddress targetMac;
	private InetAddress targetIp;

	public static ArpPacket createRequest(MacAddress senderMac, InetAddress senderIp, InetAddress targetIp) {
		return createRequest(senderMac, senderIp, new MacAddress("ff:ff:ff:ff:ff:ff"), targetIp);
	}

	public static ArpPacket createRequest(MacAddress senderMac, InetAddress senderIp, MacAddress targetMac,
			InetAddress targetIp) {
		ArpPacket p = new ArpPacket();
		p.hardwareType = 0x0001;
		p.protocolType = 0x0800;
		p.hardwareSize = 6;
		p.protocolSize = 4;
		p.opcode = 1;
		p.senderMac = senderMac;
		p.senderIp = senderIp;
		p.targetMac = targetMac;
		p.targetIp = targetIp;
		return p;
	}

	public static ArpPacket createReply(MacAddress senderMac, InetAddress senderIp, MacAddress targetMac,
			InetAddress targetIp) {
		ArpPacket p = new ArpPacket();
		p.hardwareType = 0x0001;
		p.protocolType = 0x0800;
		p.hardwareSize = 6;
		p.protocolSize = 4;
		p.opcode = 2;
		p.senderMac = senderMac;
		p.senderIp = senderIp;
		p.targetMac = targetMac;
		p.targetIp = targetIp;
		return p;
	}

	/* copy constructor */
	public static ArpPacket copyArpPacket(ArpPacket arp) {
		ArpPacket p = new ArpPacket();
		p.hardwareType = 0x0001;
		p.protocolType = 0x0800;
		p.hardwareSize = 6;
		p.protocolSize = 4;
		p.opcode = arp.getOpcode();
		p.senderMac = arp.getSenderMac();
		p.senderIp = arp.getSenderIp();
		p.targetMac = arp.getTargetMac();
		p.targetIp = arp.getTargetIp();
		return p;
	}

	public Object getL2Frame() {
		return l2Frame;
	}

	public void setL2Frame(Object l2Frame) {
		this.l2Frame = l2Frame;
	}

	public int getHardwareType() {
		return hardwareType;
	}

	public void setHardwareType(short hardwareType) {
		this.hardwareType = hardwareType;
	}

	public int getProtocolType() {
		return protocolType;
	}

	public void setProtocolType(int protocolType) {
		this.protocolType = protocolType;
	}

	public int getHardwareSize() {
		return hardwareSize;
	}

	public void setHardwareSize(byte hardwareSize) {
		this.hardwareSize = hardwareSize;
	}

	public int getProtocolSize() {
		return protocolSize;
	}

	public void setProtocolSize(byte protocolSize) {
		this.protocolSize = protocolSize;
	}

	public boolean isRequest() {
		return opcode == 1;
	}

	public boolean isReply() {
		return opcode == 2;
	}

	public int getOpcode() {
		return opcode;
	}

	public void setOpcode(short opcode) {
		this.opcode = opcode;
	}

	public MacAddress getSenderMac() {
		return senderMac;
	}

	public void setSenderMac(MacAddress senderMac) {
		this.senderMac = senderMac;
	}

	public InetAddress getSenderIp() {
		return senderIp;
	}

	public void setSenderIp(InetAddress senderIp) {
		this.senderIp = senderIp;
	}

	public MacAddress getTargetMac() {
		return targetMac;
	}

	public void setTargetMac(MacAddress targetMac) {
		this.targetMac = targetMac;
	}

	public InetAddress getTargetIp() {
		return targetIp;
	}

	public void setTargetIp(InetAddress targetIp) {
		this.targetIp = targetIp;
	}

	@Override
	public String toString() {
		if (isRequest())
			return String.format("Who has %s? Tell %s", targetIp.getHostAddress(), senderIp.getHostAddress());
		else
			return String.format("%s is at %s", senderIp.getHostAddress(), senderMac);
	}

	@Override
	public Buffer getBuffer() {
		ByteBuffer b = ByteBuffer.allocate(28);
		b.putShort((short) hardwareType);
		b.putShort((short) protocolType);
		b.put((byte) hardwareSize);
		b.put((byte) protocolSize);
		b.putShort((short) opcode);
		b.put(senderMac.getBytes());
		b.putInt(IpConverter.toInt((Inet4Address) senderIp));
		b.put(targetMac.getBytes());
		b.putInt(IpConverter.toInt((Inet4Address) targetIp));
		return new ChainBuffer(b.array());
	}
}
