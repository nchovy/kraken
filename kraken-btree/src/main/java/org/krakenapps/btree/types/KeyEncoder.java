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
package org.krakenapps.btree.types;

import java.nio.charset.Charset;

public class KeyEncoder {
	private static final Charset UTF8 = Charset.forName("utf-8");
	
	private KeyEncoder() {
	}

	public static byte[] encode(Object[] keys) {
		int length = 0;

		for (int i = 0; i < keys.length; i++) {
			Object item = keys[i];

			if (item instanceof Integer)
				length += 4;
			else if (item instanceof Long)
				length += 8;
			else if (item instanceof String)
				length += 2 + ((String) item).getBytes(UTF8).length;
		}

		byte[] b = new byte[length];
		int offset = 0;

		for (int i = 0; i < keys.length; i++) {
			Object item = keys[i];

			if (item instanceof Integer) {
				encodeInt(b, offset, (Integer) item);
				offset += 4;
			} else if (item instanceof Long) {
				encodeLong(b, offset, (Long) item);
				offset += 8;
			} else if (item instanceof String) {
				offset += encodeString(b, offset, (String) item);
			}
		}

		return b;
	}

	public static Object[] decode(Class<?>[] keyTypes, byte[] b) {
		Object[] keys = new Object[keyTypes.length];

		int i = 0;
		int offset = 0;
		for (Class<?> clazz : keyTypes) {
			if (clazz.equals(Integer.class)) {
				keys[i] = decodeInt(b, offset);
				offset += 4;
			} else if (clazz.equals(Long.class)) {
				keys[i] = decodeLong(b, offset);
				offset += 8;
			} else if (clazz.equals(String.class)) {
				int length = decodeStringLength(b, offset);
				keys[i] = new String(b, offset + 2, length, UTF8);
				offset += length + 2;
			}

			i++;
		}

		return keys;
	}

	public static void encodeInt(byte[] b, int offset, int l) {
		for (int i = 0; i < 4; i++) {
			b[offset + i] = (byte) ((l >> ((3 - i) * 8)) & 0xff);
		}
	}

	public static void encodeLong(byte[] b, int offset, long l) {
		for (int i = 0; i < 8; i++) {
			b[offset + i] = (byte) ((l >> ((7 - i) * 8)) & 0xff);
		}
	}

	public static int encodeString(byte[] b, int offset, String s) {
		byte[] sb = s.getBytes(UTF8);
		if (sb.length >= 0xffff)
			throw new IllegalArgumentException("string is too long: " + s);

		b[offset] = (byte) ((sb.length >> 8) & 0xff);
		b[offset + 1] = (byte) (sb.length & 0xff);
		for (int i = 0; i < sb.length; i++)
			b[offset + i + 2] = sb[i];

		return sb.length + 2;
	}

	public static int decodeInt(byte[] b, int offset) {
		int l = 0;
		for (int i = 0; i < 4; i++) {
			l <<= 8;
			l |= b[offset + i] & 0xff;
		}
		return l;
	}

	public static long decodeLong(byte[] b, int offset) {
		int l = 0;
		for (int i = 0; i < 8; i++) {
			l <<= 8;
			l |= b[i + offset] & 0xff;
		}

		return l;
	}

	public static int decodeStringLength(byte[] b, int offset) {
		return (b[offset] << 8) | (b[offset + 1] & 0xff);
	}
}
