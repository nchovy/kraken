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
package org.krakenapps.pcap.decoder.tcp;

import java.net.Inet4Address;
import java.nio.ByteBuffer;

import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.Checksum;
import org.krakenapps.pcap.util.IpConverter;

/**
 * @author mindori
 */
public class TcpChecksum {
	private TcpChecksum() {
	}

	public static int sum(TcpPacket s) {
		ByteBuffer buf = build(s);
		Buffer data = s.getData();
		int length = buf.limit();
		if (data != null)
			length += data.readableBytes();
		
		short[] checksumBytes = new short[length / 2];
		
		int i = 0;
		for (; i < checksumBytes.length; i++) {
			checksumBytes[i] = buf.getShort();
		}

		/* add payload to checksum */
		if (data != null) {
			while (data.isEOB()) {
				// TODO: buffer underflow handling (padding)
				checksumBytes[i++] = data.getShort();
			}
		}
		
		if (data != null)
			data.rewind();

		return Checksum.sum(checksumBytes);
	}

	private static ByteBuffer build(TcpPacket s) {
		/* except option and padding: 16 shorts */
		ByteBuffer bb = ByteBuffer.allocate(12 + s.getTotalLength());

		// TODO: IPv6 handling
		// pseudo header
		bb.putInt(IpConverter.toInt((Inet4Address) s.getSourceAddress()));
		bb.putInt(IpConverter.toInt((Inet4Address) s.getDestinationAddress()));
		bb.put((byte) 0); // padding
		bb.put((byte) 6); // tcp
		bb.putShort((short) (s.getTotalLength()));

		//
		bb.putShort((short) s.getSourcePort());
		bb.putShort((short) s.getDestinationPort());
		bb.putInt(s.getSeq());
		bb.putInt(s.getAck());
		bb.put((byte) (s.getDataOffset() << 4));
		bb.put((byte) s.getFlags());
		bb.putShort((short) s.getWindow());
		bb.putShort((short) 0); // checksum
		bb.putShort((short) s.getUrgentPointer());
		if (s.getOptions() != null)
			bb.put(s.getOptions());
		if (s.getPadding() != null) 
			bb.put(s.getPadding());

		bb.flip();
		return bb;
	}

}
