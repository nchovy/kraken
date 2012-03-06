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

import java.util.Arrays;

/**
 * MacAddress represents ethernet address.
 * 
 * @author xeraph
 * 
 */
public class MacAddress {
	private final byte[] mac;

	public MacAddress(String s) {
		String[] tokens = s.split(":");
		if (tokens.length != 6)
			throw new IllegalArgumentException("mac should be six bytes");

		byte[] b = new byte[6];
		int i = 0;
		for (String token : tokens) {
			if (token.length() != 2)
				throw new IllegalArgumentException("invalid mac token: " + token);

			char c0 = token.charAt(0);
			char c1 = token.charAt(1);
			b[i++] = (byte) (hex(c0) * 16 + hex(c1));
		}

		mac = b;
	}

	private int hex(char c) {
		if (c >= 'a' && c <= 'z')
			return (c - 'a') + 10;

		if (c >= 'A' && c <= 'Z')
			return (c - 'A') + 10;

		if (c >= '0' && c <= '9')
			return c - '0';

		throw new IllegalArgumentException("invalid hex char: " + c);
	}

	public MacAddress(byte[] mac) {
		if (mac == null || mac.length != 6)
			throw new IllegalArgumentException("mac should be six bytes");

		this.mac = mac;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(mac);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MacAddress other = (MacAddress) obj;
		if (!Arrays.equals(mac, other.mac))
			return false;
		return true;
	}

	public byte[] getBytes() {
		return mac;
	}

	@Override
	public String toString() {
		return String.format("%02X:%02X:%02X:%02X:%02X:%02X", mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);
	}

}
