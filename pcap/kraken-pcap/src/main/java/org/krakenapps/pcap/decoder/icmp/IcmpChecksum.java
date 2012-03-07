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

import java.nio.ByteBuffer;

import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.Checksum;

/**
 * Calculates ICMP checksum.
 * 
 * @author xeraph
 * 
 */
public class IcmpChecksum {
	private IcmpChecksum() {
	}

	public static int sum(IcmpPacket p) {
		Buffer data = p.getData();
		int dataLen = 0;
		if (data != null)
			dataLen = data.readableBytes();

		ByteBuffer bb = ByteBuffer.allocate(8 + dataLen);
		bb.put((byte) p.getType());
		bb.put((byte) p.getCode());
		bb.putShort((short) p.getChecksum());
		bb.putShort((short) p.getId());
		bb.putShort((short) p.getSeq());

		while (!data.isEOB())
			bb.put(data.get());

		data.rewind();
		bb.flip();

		short[] words = new short[bb.limit() / 2];
		for (int i = 0; i < words.length; i++)
			words[i] = bb.getShort();

		return Checksum.sum(words);
	}
}
