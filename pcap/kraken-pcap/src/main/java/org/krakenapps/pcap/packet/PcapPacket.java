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
package org.krakenapps.pcap.packet;

import org.krakenapps.pcap.util.Buffer;

/**
 * PcapPacket contains header and payload.
 * 
 * @author mindori
 * 
 */
public class PcapPacket {
	private PacketHeader header;
	private Buffer payload;

	public PcapPacket(PacketHeader header, Buffer payload) {
		this.header = header;
		this.payload = payload;
	}

	public PcapPacket(PacketHeader header, PacketPayload payload) {
		this.header = header;
		this.payload = payload.getBuffer();
	}

	public PacketHeader getPacketHeader() {
		return header;
	}

	public Buffer getPacketData() {
		return payload;
	}

	@Override
	public String toString() {
		return header.toString();
	}

}