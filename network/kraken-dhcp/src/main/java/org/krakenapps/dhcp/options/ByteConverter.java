/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.dhcp.options;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ByteConverter {
	private ByteConverter() {
	}

	public static InetAddress toInetAddress(byte[] addr) {
		try {
			return InetAddress.getByAddress(addr);
		} catch (UnknownHostException e) {
			// but should not reachable
			throw new RuntimeException(e);
		}
	}

	public static byte[] convert(int v) {
		byte b1 = (byte) ((v >> 24) & 0xff);
		byte b2 = (byte) ((v >> 16) & 0xff);
		byte b3 = (byte) ((v >> 8) & 0xff);
		byte b4 = (byte) (v & 0xff);

		return new byte[] { b1, b2, b3, b4 };
	}

	public static long toUnsignedInteger(byte[] b) {
		return toInteger(b) & 0xffffffffl;
	}

	public static int toInteger(byte[] b) {
		int v = 0;
		for (int i = 0; i < 4; i++) {
			v <<= 8;
			v |= b[i] & 0xff;
		}

		return v;
	}
}
