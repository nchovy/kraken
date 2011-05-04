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
package org.krakenapps.pcap.decoder.udp;

import java.net.Inet4Address;
import java.nio.ByteBuffer;

import org.krakenapps.pcap.util.Checksum;
import org.krakenapps.pcap.util.IpConverter;

public class UdpChecksum {
	private static final int HEADER_SIZE = 20;

	private UdpChecksum() {
	}

	public static int sum(UdpPacket p) {
		int length = HEADER_SIZE;
		int headerWordCount = HEADER_SIZE / 2;
		int dataLength = p.getData().readableBytes();

		// calculate total length and allocate buffer
		length += dataLength;
		boolean padding = dataLength % 2 == 1;
		if (padding)
			length++; // padding

		short[] words = new short[length / 2];

		// pseudo header
		ByteBuffer header = ByteBuffer.allocate(HEADER_SIZE);
		header.putInt(IpConverter.toInt((Inet4Address) p.getSource().getAddress()));
		header.putInt(IpConverter.toInt((Inet4Address) p.getDestination().getAddress()));
		header.put((byte) 0x00);
		header.put((byte) 17); // protocol
		header.putShort((short) p.getLength());
		
		// udp header
		header.putShort((short) p.getSourcePort());
		header.putShort((short) p.getDestinationPort());
		header.putShort((short) p.getLength());
		header.putShort((short) 0);
		header.flip();

		for (int i = 0; i < headerWordCount; i++)
			words[i] = header.getShort();

		int limit = words.length - headerWordCount;
		if (padding)
			limit--;

		for (int i = 0; i < limit; i++)
			words[headerWordCount + i] = p.getData().getShort();

		if (padding)
			words[words.length - 1] = (short) (p.getData().get() << 8);

		p.getData().rewind();
		return Checksum.sum(words);
	}
}
