/*
 * Copyright 2011 Future Systems, Inc
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
package org.krakenapps.pcap.decoder.netbios;

import java.nio.ByteBuffer;

import org.krakenapps.pcap.Injectable;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ChainBuffer;

public class NetBiosSessionPacket implements Injectable{
	private NetBiosSessionHeader header;
	private NetBiosSessionData data;

	public NetBiosSessionPacket(NetBiosSessionHeader header,
			NetBiosSessionData data) {
		this.header = header;
		this.data = data;
	}

	public NetBiosSessionHeader getHeader() {
		return header;
	}

	public NetBiosSessionData getData() {
		return data;
	}

	@Override
	public String toString() {
		return header.toString() + data.toString();
	}

	@Override
	public Buffer getBuffer() {
		ByteBuffer headerb = ByteBuffer.allocate(4);
	//	ByteBuffer datab = ByteBuffer
		/*header.setType(NetBiosSessionType.parse(b.get() & 0xff));
		header.setFlags(b.get());
		header.setLength(b.getShort());*/
		headerb.put(header.getFlags());
		headerb.putShort(header.getLength());
		
		Buffer buffer = new ChainBuffer();
		buffer.addLast(headerb.array());
		return buffer;
	}
}
