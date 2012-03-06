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
package org.krakenapps.pcap.decoder.ethernet;

import java.nio.ByteBuffer;

import org.krakenapps.pcap.Injectable;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ChainBuffer;

/**
 * Ethernet header
 * 
 * @author xeraph
 * 
 */
public class EthernetHeader implements Injectable {
	private final MacAddress destination;
	private final MacAddress source;
	private final int type;

	public EthernetHeader(MacAddress source, MacAddress destination, int type) {
		if (source == null)
			throw new IllegalArgumentException("source should be not null");
		
		if (destination == null)
			throw new IllegalArgumentException("destination should be not null");
		
		this.source = source;
		this.destination = destination;
		this.type = type;
	}

	public MacAddress getDestination() {
		return destination;
	}

	public MacAddress getSource() {
		return source;
	}

	public int getType() {
		return type;
	}

	public Buffer getBuffer() {
		ByteBuffer b = ByteBuffer.allocate(14);
		b.put(destination.getBytes());
		b.put(source.getBytes());
		b.putShort((short) type);
		return new ChainBuffer(b.array());
	}
}
