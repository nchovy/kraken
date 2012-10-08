/*
 * Copyright 2012 Future Systems
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
package org.krakenapps.logdb.sort;

import java.io.IOException;
import java.io.InputStream;

class IoHelper {
	private IoHelper() {
	}

	public static void encodeLong(byte[] b, long n) {
		b[0] = (byte) ((n >> 56) & 0xff);
		b[1] = (byte) ((n >> 48) & 0xff);
		b[2] = (byte) ((n >> 40) & 0xff);
		b[3] = (byte) ((n >> 32) & 0xff);
		b[4] = (byte) ((n >> 24) & 0xff);
		b[5] = (byte) ((n >> 16) & 0xff);
		b[6] = (byte) ((n >> 8) & 0xff);
		b[7] = (byte) (n & 0xff);
	}

	public static void encodeInt(byte[] b, int n) {
		b[0] = (byte) ((n >> 24) & 0xff);
		b[1] = (byte) ((n >> 16) & 0xff);
		b[2] = (byte) ((n >> 8) & 0xff);
		b[3] = (byte) (n & 0xff);
	}

	public static long decodeLong(byte[] b) {
		int l = 0;
		for (int i = 0; i < 8; i++) {
			l <<= 8;
			l |= b[i] & 0xff;
		}
		return l;
	}

	public static int decodeInt(byte[] b) {
		int l = 0;
		for (int i = 0; i < 4; i++) {
			l <<= 8;
			l |= b[i] & 0xff;
		}
		return l;
	}

	public static int ensureRead(InputStream is, byte[] b, int length) throws IOException {
		int total = 0;
		int len = length;
		int offset = 0;

		while (total < length) {
			int r = is.read(b, offset, len);
			if (r <= 0)
				break;

			total += r;
			offset += r;
			len -= r;
		}

		return total;
	}
}
