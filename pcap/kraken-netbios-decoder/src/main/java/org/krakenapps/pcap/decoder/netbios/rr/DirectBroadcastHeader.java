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
package org.krakenapps.pcap.decoder.netbios.rr;

import java.net.InetAddress;
import java.net.UnknownHostException;


import org.krakenapps.pcap.decoder.netbios.NetBiosDatagramType;
import org.krakenapps.pcap.util.Buffer;

public class DirectBroadcastHeader extends DatagramHeader {

	private DirectBroadcastHeader() {
	}

	private short dgmLength;
	private short packetOffset;

	public short getDgmLength() {
		return dgmLength;
	}

	public void setDgmLength(short dgmLength) {
		this.dgmLength = dgmLength;
	}

	public short getPacketOffset() {
		return packetOffset;
	}

	public void setPacketOffset(short packetOffset) {
		this.packetOffset = packetOffset;
	}

	public static DirectBroadcastHeader parse(Buffer b) {
		DirectBroadcastHeader header = new DirectBroadcastHeader();
		byte[] buff = new byte[4];
		b.rewind();
		header.setMsgType(NetBiosDatagramType.parse(b.get() & 0xff));
		header.setFlags(b.get());
		header.setDgmID(b.getShort());
		b.gets(buff);
		try {
			header.setAddresses(InetAddress.getByAddress(buff));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		header.setPort(b.getShort());
		header.setDgmLength(b.getShort());
		header.setPacketOffset(b.getShort());
		return header;
	}

	@Override
	public String toString() {
		return String.format("DatagramHeader DirectBroadcastHeader"+
				"type=%s, flags=0x%s, dgmid=0x%s , sourceIP =%s," +
				"dgmLength=0x%s , packetOffset = 0x%s\n",
				this.msgType, Integer.toHexString(this.flags), Integer.toHexString(this.dgmID), this.addresses ,
				Integer.toHexString(this.dgmLength) , Integer.toHexString(this.packetOffset));
	}
}
