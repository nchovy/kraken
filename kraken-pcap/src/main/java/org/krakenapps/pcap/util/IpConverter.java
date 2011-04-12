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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author xeraph
 */
public class IpConverter {
	private IpConverter() {
	}

	public static int toInt(Inet4Address addr) {
		byte[] b = addr.getAddress();
		int l = 0;
		for (int i = 0; i < 4; i++) {
			l <<= 8;
			l |= b[i] & 0xff;
		}
		return l;
	}

	public static InetAddress toInetAddress(int ipAddr) {
		int copyAddr = ipAddr;
		byte b1 = (byte) ((copyAddr >> 24) & 0xff);
		byte b2 = (byte) ((copyAddr >> 16) & 0xff);
		byte b3 = (byte) ((copyAddr >> 8) & 0xff);
		byte b4 = (byte) (copyAddr & 0xff);
		try {
			return InetAddress.getByAddress(new byte[] { b1, b2, b3, b4 });
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}
}
