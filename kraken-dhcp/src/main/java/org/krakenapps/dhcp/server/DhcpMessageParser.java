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
package org.krakenapps.dhcp.server;

import java.net.InetAddress;
import java.nio.ByteBuffer;

import org.krakenapps.dhcp.DhcpMessage;
import org.krakenapps.dhcp.DhcpOption;
import org.krakenapps.dhcp.MacAddress;

public class DhcpMessageParser {
	public static DhcpMessage parse(ByteBuffer b) {
		byte op = b.get();
		byte htype = b.get();
		byte hlen = b.get();
		byte hops = b.get();
		int xid = b.getInt();
		short secs = b.getShort();
		short flags = b.getShort();
		InetAddress clientAddress = IpConverter.toInetAddress(b.getInt());
		InetAddress yourAddress = IpConverter.toInetAddress(b.getInt());
		InetAddress nextServerAddress = IpConverter.toInetAddress(b.getInt());
		InetAddress gatewayAddress = IpConverter.toInetAddress(b.getInt());

		byte[] mac = new byte[6];
		b.get(mac);
		MacAddress clientMac = new MacAddress(mac);

		byte[] temp = new byte[202];
		b.get(temp);

		// check magic cookie
		long magicCookie = b.getInt() & 0xffffffffl;
		if (magicCookie != 0x63825363L)
			return null;

		DhcpMessage msg = new DhcpMessage();
		msg.setMessageType(op);
		msg.setHardwareType(htype);
		msg.setHardwareAddressLength(hlen);
		msg.setHops(hops);
		msg.setTransactionId(xid);
		msg.setSecs(secs);
		msg.setFlags(flags);
		msg.setClientAddress(clientAddress);
		msg.setYourAddress(yourAddress);
		msg.setNextServerAddress(nextServerAddress);
		msg.setGatewayAddress(gatewayAddress);
		msg.setClientMac(clientMac);

		// parse options
		int remaining = b.remaining();
		for (int i = 0; i < remaining;) {
			byte type = b.get();
			if (type == (byte) 0xFF)
				break; // end of option

			int length = b.get() & 0xFF;
			byte[] value = new byte[length];
			b.get(value);
			DhcpOption option = DhcpOptionParser.create(type, length, value);
			msg.getOptions().add(option);
			i += 2 + length;
		}
		
		return msg;
	}
}
