/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.captiveportal.impl;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.krakenapps.pcap.decoder.ethernet.MacAddress;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ChainBuffer;
import org.krakenapps.pcap.util.IpConverter;

import static org.krakenapps.pcap.util.PacketManipulator.*;

public class FakeDnsResponse {
	private MacAddress targetMac;
	private InetSocketAddress dnsServer;
	private InetSocketAddress targetHost;
	private int txId; // 2byte
	private String domain;
	private InetAddress fakeIp;

	public FakeDnsResponse(MacAddress targetMac, InetSocketAddress targetHost, InetSocketAddress dnsServer, int txId,
			String domain, InetAddress fakeIp) {
		this.targetMac = targetMac;
		this.targetHost = targetHost;
		this.dnsServer = dnsServer;
		this.txId = txId;
		this.domain = domain;
		this.fakeIp = fakeIp;
	}

	public Buffer getPacket() {
		byte[] encodedDomain = encodeDomain(domain);

		ByteBuffer bb = ByteBuffer.allocate(32 + encodedDomain.length);
		bb.putShort((short) txId);
		bb.put(new byte[] { (byte) 0x81, (byte) 0x80 });
		bb.putShort((short) 1); // question count
		bb.putShort((short) 1); // answer count
		bb.putShort((short) 0); // authority count
		bb.putShort((short) 0); // additional count
		bb.put(encodedDomain);
		bb.putShort((short) 1); // Type A (host address)
		bb.putShort((short) 1);// Class IN
		bb.put(new byte[] { (byte) 0xc0, 0x0c }); // pointer to domain offset
		bb.putShort((short) 1); // Type A (host address)
		bb.putShort((short) 1); // Class IN
		bb.putInt(5); // TTL
		bb.putShort((short) 4); // data length
		bb.putInt(IpConverter.toInt((Inet4Address) fakeIp));

		Buffer dns = new ChainBuffer(bb.array());
		return ETH().dst(targetMac).data(IP().data(UDP().src(dnsServer).dst(targetHost).data(new ChainBuffer(dns))))
				.build().getBuffer();
	}

	private static byte[] encodeDomain(String s) {
		String[] tokens = s.split("\\.");
		int length = 1; // last dot
		for (String t : tokens)
			length += 1 + t.getBytes().length;

		int p = 0;
		byte[] b = new byte[length];
		for (String t : tokens) {
			byte[] tb = t.getBytes();
			b[p++] = (byte) tb.length;
			for (int i = 0; i < tb.length; i++)
				b[p++] = tb[i];
		}

		b[b.length - 1] = 0;
		return b;
	}

	@Override
	public String toString() {
		return "txid=" + txId + ", domain=" + domain + ", fake ip=" + fakeIp;
	}
}
