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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.krakenapps.pcap.decoder.ethernet.EthernetFrame;
import org.krakenapps.pcap.decoder.ethernet.MacAddress;
import org.krakenapps.pcap.live.PcapDevice;
import org.krakenapps.pcap.util.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FakeDns {
	private FakeDns() {
	}

	public static boolean isDnsPacket(int protocol, int dstPort) {
		return protocol == 17 && dstPort == 53;
	}

	public static void forgeResponse(InetAddress fakeIp, PcapDevice device, EthernetFrame frame, InetAddress src,
			int srcPort, InetAddress dst, int dstPort, Buffer buf) {
		if (fakeIp == null || device == null)
			return;

		InetSocketAddress source = new InetSocketAddress(src, srcPort);
		InetSocketAddress destination = new InetSocketAddress(dst, dstPort);
		FakeDnsResponse r = getDnsResponse(fakeIp, frame, source, destination, buf);
		if (r != null)
			sendPacket(device, r.getPacket());

		Logger logger = LoggerFactory.getLogger(FakeDns.class.getName());
		logger.trace("kraken captive portal: sent fake dns response, " + r);
	}

	private static void sendPacket(PcapDevice device, Buffer b) {
		try {
			device.write(b);
		} catch (IOException e) {
			Logger logger = LoggerFactory.getLogger(FakeDns.class.getName());
			logger.error("kraken captive portal: cannot route packet", e);
		}
	}

	private static FakeDnsResponse getDnsResponse(InetAddress fakeIp, EthernetFrame frame, InetSocketAddress src,
			InetSocketAddress dst, Buffer buf) {
		buf.rewind();
		buf.skip(28); // skip length and checksum
		int txId = buf.getUnsignedShort();
		int flags = buf.getUnsignedShort();
		int questionCount = buf.getUnsignedShort();
		int answerCount = buf.getUnsignedShort();
		int authorityCount = buf.getUnsignedShort();
		int additionalCount = buf.getUnsignedShort();

		if (flags != 0x0100)
			return null;

		boolean isQuery = (questionCount == 1 && answerCount == 0 && authorityCount == 0 && additionalCount == 0);
		if (!isQuery)
			return null;

		String domain = decodeDomain(buf);
		int type = buf.getUnsignedShort();
		int clazz = buf.getUnsignedShort();
		if (type != 1 || clazz != 1)
			return null;

		MacAddress targetMac = frame.getSource();
		return new FakeDnsResponse(targetMac, src, dst, (short) txId, domain, fakeIp);
	}

	private static String decodeDomain(Buffer buf) {
		String domain = "";

		while (true) {
			byte length = buf.get();
			if (length == 0)
				break;

			byte[] b = new byte[length];
			buf.gets(b);

			String token = new String(b);
			if (domain.length() == 0)
				domain += token;
			else
				domain += "." + token;
		}

		return domain;
	}
}
