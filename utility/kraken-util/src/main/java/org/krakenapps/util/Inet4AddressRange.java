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
package org.krakenapps.util;

import java.net.Inet4Address;

public class Inet4AddressRange {
	private Inet4Address single;
	private int[] begin;
	private int[] end;

	public Inet4AddressRange(Inet4Address singleAddr) {
		this.single = singleAddr;
	}

	public Inet4AddressRange(Inet4Address begin, Inet4Address end) {
		if (begin.equals(end))
			this.single = begin;
		else {
			this.begin = unsignedByteArray(begin.getAddress());
			this.end = unsignedByteArray(end.getAddress());
		}
	}

	public Inet4AddressRange(Inet4Address prefix, int bits) {
		if (bits == 32) {
			this.single = prefix;
			return;
		}
		this.begin = unsignedByteArray(prefix.getAddress());
		this.end = unsignedByteArray(prefix.getAddress());
		for (int i = bits; i < 32; ++i) {
			// [........][........][........][........]
			//  01234567  89012345  67890123  45678901
			int d = i / 8;
			int r = i % 8;
			this.end[d] |= (0x1 << (7 - r));
		}
	}

	private int[] unsignedByteArray(byte[] address) {
		int[] ret = new int[address.length];
		for (int i = 0; i < address.length; ++i) {
			ret[i] = address[i] & 0xFF;
		}
		return ret;
	}

	public boolean contains(Inet4Address src) {
		if (single != null) {
			return src.equals(single);
		} else {
			// don't use unsignedByteArray to avoid allocation 
			byte[] address = src.getAddress();
			for (int i = 0; i < 4; ++i) {
				if ((address[i] & 0xFF) < begin[i] || end[i] < (address[i] & 0xFF))
					return false;
			}
			return true;
		}
	}

	public String toString() {
		if (single == null)
			return String.format("Inet4AddressRange [%d.%d.%d.%d ~ %d.%d.%d.%d]",
					begin[0], begin[1], begin[2], begin[3],
					end[0], end[1], end[2], end[3]);
		else {
			int[] tmp = unsignedByteArray(single.getAddress());
			return String.format("Inet4AddressRange [%d.%d.%d.%d]",
					tmp[0], tmp[1], tmp[2], tmp[3]);
		}
	}
}
