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

import java.nio.ByteBuffer;

import org.krakenapps.pcap.util.Checksum;

/**
 * Calculates IP checksum
 * 
 * @author mindori
 */
public class IpChecksum {
	private IpChecksum() {
	}

	public static int sum(Ipv4Packet p) {
		ByteBuffer buf = build(p);
		return sum(buf);
	}

	public static int sum(ByteBuffer buf) {
		short[] words = new short[buf.limit() / 2];
		for (int i = 0; i < words.length; i++)
			words[i] = buf.getShort();

		return Checksum.sum(words);
	}

	private static ByteBuffer build(Ipv4Packet p) {
		ByteBuffer bb = ByteBuffer.allocate(100);
		bb.put(getVersionAndIhl(p));
		bb.put((byte) p.getTos());
		bb.putShort((short) p.getTotalLength());
		bb.putShort((short) p.getId());
		bb.putShort(getFlagAndFragmentOffset(p));
		bb.put((byte) p.getTtl());
		bb.put((byte) p.getProtocol());
		bb.putShort((short) 0); // zero header checksum
		bb.putInt(p.getSource());
		bb.putInt(p.getDestination());
		if (p.getOptions() != null) {
			bb.put(p.getOptions());
			bb.put(p.getPadding());
		}
		bb.flip();
		return bb;
	}

	private static byte getVersionAndIhl(Ipv4Packet p) {
		return (byte) ((0x40) | ((p.getIhl() >> 2) & 0xf));
	}

	private static short getFlagAndFragmentOffset(Ipv4Packet p) {
		byte flag = (byte) p.getFlags();
		short offset = (short) p.getFragmentOffset();
		return (short) (((flag << 13)) | (offset & 0x1fff));
	}
}
