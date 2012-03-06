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
package org.krakenapps.pcap.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.krakenapps.pcap.decoder.ethernet.MacAddress;

/**
 * @author mindori
 */
public class ByteArrayParser {
	private ByteArrayParser() {
	}

	public static int getInt(byte[] data, int offset) {
		return data[offset] << 24 | ((data[offset + 1] << 16) & 0xFFFFFF) | ((data[offset + 2] << 8) & 0xFFFF)
				| ((data[offset + 3]) & 0xFF);
	}

	public static MacAddress getMacAddress(byte[] data, int offset) {
		byte[] mac = new byte[6];
		for (int i = 0; i < 6; i++)
			mac[i] = data[offset + i];
		return new MacAddress(mac);
	}

	public static short getShort(byte[] data, int offset) {
		short s = (short) ((data[offset] << 8) & 0xFF00);
		s |= data[offset + 1] & 0xFF;
		return s;
	}

	public static InetAddress getAddress(byte[] data, int offset) {
		byte[] addr = new byte[4];
		for (int i = 0; i < 4; i++) {
			addr[i] = data[offset + i];
		}

		try {
			return InetAddress.getByAddress(addr);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		}
	}

}
