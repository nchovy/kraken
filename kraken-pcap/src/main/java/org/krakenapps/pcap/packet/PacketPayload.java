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

/**
 * @author mindori
 */

package org.krakenapps.pcap.packet;

import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ChainBuffer;

/**
 * @author mindori
 */
public class PacketPayload {
	private Buffer buffer;
	private byte[] bytes;
	
	public PacketPayload(byte[] bytes) {
		buffer = new ChainBuffer();
		buffer.addLast(bytes);
		this.bytes = bytes;
	}
	
	public Buffer getBuffer() { 
		return buffer;
	}

	public byte[] getBytes() {
		return bytes;
	}
	
	public byte get() {
		return buffer.get();
	}

	public short getShort() {
		return buffer.getShort();
	}

	public int getInt() {
		return buffer.getInt();
	}

	public void gets(byte[] source, int off, int len) { 
		buffer.gets(source, off, len);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PacketPayload other = (PacketPayload) obj;
		if (!buffer.equals(other.buffer))
			return false;
		return true;
	}
}