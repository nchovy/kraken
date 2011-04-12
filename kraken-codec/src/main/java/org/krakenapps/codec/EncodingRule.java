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
package org.krakenapps.codec;

import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EncodingRule {
	public static final byte NULL_TYPE = 0;
	public static final byte BOOLEAN_TYPE = 1;
	// INT16_TYPE, INT32_TYPE, INT64_TYPE are not used after 2.0
	public static final byte INT16_TYPE = 2;
	public static final byte INT32_TYPE = 3;
	public static final byte INT64_TYPE = 4;
	public static final byte STRING_TYPE = 5; // utf-8 only
	public static final byte DATE_TYPE = 6;
	public static final byte IP4_TYPE = 7;
	public static final byte IP6_TYPE = 8;
	public static final byte MAP_TYPE = 9;
	public static final byte ARRAY_TYPE = 10;
	public static final byte BLOB_TYPE = 11;
	public static final byte ZINT16_TYPE = 12;
	public static final byte ZINT32_TYPE = 13;
	public static final byte ZINT64_TYPE = 14;

	private EncodingRule() {
	}

	@SuppressWarnings("unchecked")
	public static void encode(ByteBuffer bb, Object value) {
		if (value == null) {
			encodeNull(bb);
		} else if (value instanceof String) {
			encodeString(bb, (String) value);
		} else if (value instanceof Long) {
			encodeLong(bb, (Long) value);
		} else if (value instanceof Integer) {
			encodeInt(bb, (Integer) value);
		} else if (value instanceof Short) {
			encodeShort(bb, (Short) value);
		} else if (value instanceof Date) {
			encodeDate(bb, (Date) value);
		} else if (value instanceof Inet4Address) {
			encodeIp4(bb, (Inet4Address) value);
		} else if (value instanceof Inet6Address) {
			encodeIp6(bb, (Inet6Address) value);
		} else if (value instanceof Map<?, ?>) {
			encodeMap(bb, (Map<String, Object>) value);
		} else if (value instanceof List<?>) {
			encodeArray(bb, (List<?>) value);
		} else if (value.getClass().isArray()) {
			if (value.getClass().getName().equals("[B")) {
				encodeBlob(bb, (byte[]) value);
			} else {
				encodeArray(bb, (Object[]) value);
			}
		} else if (value instanceof Boolean) {
			encodeBoolean(bb, (Boolean) value);
		} else {
			throw new UnsupportedTypeException(value.toString());
		}
	}

	@SuppressWarnings("unchecked")
	public static int lengthOf(Object value) {
		if (value == null) {
			return lengthOfNull();
		} else if (value instanceof String) {
			return lengthOfString((String) value);
		} else if (value instanceof Long) {
			return lengthOfLong((Long) value);
		} else if (value instanceof Integer) {
			return lengthOfInt((Integer) value);
		} else if (value instanceof Short) {
			return lengthOfShort((Short) value);
		} else if (value instanceof Date) {
			return lengthOfDate();
		} else if (value instanceof Inet4Address) {
			return lengthOfIp4((Inet4Address) value);
		} else if (value instanceof Inet6Address) {
			return lengthOfIp6((Inet6Address) value);
		} else if (value instanceof Map<?, ?>) {
			return lengthOfMap((Map<String, Object>) value);
		} else if (value instanceof List<?>) {
			return lengthOfArray((List<?>) value);
		} else if (value.getClass().isArray()) {
			if (value.getClass().getName().equals("[B")) {
				return lengthOfBlob((byte[]) value);
			} else {
				return lengthOfArray((Object[]) value);
			}
		} else if (value instanceof Boolean) {
			return lengthOfBoolean((Boolean) value);
		} else {// not supported.
			throw new UnsupportedTypeException(value.toString());
		}
	}

	@Deprecated
	public static int length(Object value) {
		return lengthOf(value);
	}

	public static Object decode(ByteBuffer bb) {
		int typeByte = bb.get(bb.position());
		switch (typeByte) {
		case NULL_TYPE: {
			bb.get();
			return null;
		}
		case STRING_TYPE:
			return decodeString(bb);
		case INT32_TYPE:
			throw new UnsupportedTypeException("deprecated number type");
		case INT16_TYPE:
			throw new UnsupportedTypeException("deprecated number type");
		case INT64_TYPE:
			throw new UnsupportedTypeException("deprecated number type");
		case DATE_TYPE:
			return decodeDate(bb);
		case IP4_TYPE:
			return decodeIp4(bb);
		case IP6_TYPE:
			return decodeIp6(bb);
		case MAP_TYPE:
			return decodeMap(bb);
		case ARRAY_TYPE:
			return decodeArray(bb);
		case BOOLEAN_TYPE:
			return decodeBoolean(bb);
		case ZINT32_TYPE:
			return (int) decodeInt(bb);
		case ZINT16_TYPE:
			return (short) decodeShort(bb);
		case ZINT64_TYPE:
			return (long) decodeLong(bb);
		}

		return null;
	}

	public static void encodeNull(ByteBuffer bb) {
		bb.put(NULL_TYPE);
	}
	
	public static void encodeNumber(ByteBuffer bb, Class<?> clazz, long value) {
		if (clazz.equals(int.class)) {
			encodeInt(bb, (int)value);
		} else if (clazz.equals(long.class)) {
			encodeLong(bb, value);
		} else if (clazz.equals(short.class)) {
			encodeShort(bb, (short)value);
		} else {
			throw new UnsupportedTypeException("invalid number type: " + clazz.getName());
		}
	}
	
	public static void encodeRawNumber(ByteBuffer bb, Class<?> clazz, long value) {
		int len = lengthOfRawNumber(clazz, value);
		for (int i = 0; i < len; ++i) {
			byte signalBit = (byte) (i != len - 1 ? 0x80 : 0);
			byte data = (byte) (signalBit | (byte) (value >> (7 * (len - i - 1)) & 0x7F));
			bb.put(data);
		}
	}
	
	public static long decodeRawNumber(ByteBuffer bb) {
		long value = 0L;

		byte b;
		do {
			value = value << 7;
			b = bb.get();
			value |= b & 0x7F;
		} while ((b & 0x80) == 0x80);
		return value;
	}

	public static void encodePlainLong(ByteBuffer bb, long value) {
		bb.put(INT64_TYPE);
		encodeRawNumber(bb, long.class, value);
	}

	public static long decodePlainLong(ByteBuffer bb) {
		byte type = bb.get();
		if (type != INT64_TYPE)
			throw new TypeMismatchException(INT64_TYPE, type, bb.position() - 1);

		return (long) decodeRawNumber(bb);
	}

	public static void encodePlainInt(ByteBuffer bb, int value) {
		bb.put(INT32_TYPE);
		encodeRawNumber(bb, int.class, value);
	}

	public static int decodePlainInt(ByteBuffer bb) {
		byte type = bb.get();
		if (type != INT32_TYPE)
			throw new TypeMismatchException(INT32_TYPE, type, bb.position() - 1);

		return (int) decodeRawNumber(bb);
	}

	public static void encodePlainShort(ByteBuffer bb, short value) {
		bb.put(INT16_TYPE);
		encodeRawNumber(bb, short.class, value);
	}

	public static short decodePlainShort(ByteBuffer bb) {
		byte type = bb.get();
		if (type != INT16_TYPE)
			throw new TypeMismatchException(INT16_TYPE, type, bb.position() - 1);

		return (short) decodeRawNumber(bb);
	}
	
	public static void encodeLong(ByteBuffer bb, long value) {
		bb.put(ZINT64_TYPE);
		long zvalue = (value << 1) ^ (value >> 63);
		encodeRawNumber(bb, long.class, zvalue);
	}

	public static long decodeLong(ByteBuffer bb) {
		byte type = bb.get();
		if (type != ZINT64_TYPE)
			throw new TypeMismatchException(ZINT64_TYPE, type, bb.position() - 1);

		long zvalue = (long) decodeRawNumber(bb);
		return (zvalue >> 1) ^ (zvalue << 63 >> 63);
	}

	public static void encodeInt(ByteBuffer bb, int value) {
		bb.put(ZINT32_TYPE);
		int zvalue = (value << 1) ^ (value >> 31);
		encodeRawNumber(bb, int.class, zvalue);
	}

	public static int decodeInt(ByteBuffer bb) {
		byte type = bb.get();
		if (type != ZINT32_TYPE)
			throw new TypeMismatchException(ZINT32_TYPE, type, bb.position() - 1);

		int zvalue = (int) decodeRawNumber(bb);
		return (zvalue >> 1) ^ (zvalue << 31 >> 31);
	}

	public static void encodeShort(ByteBuffer bb, short value) {
		bb.put(ZINT16_TYPE);
		short zvalue = (short) ((value << 1) ^ (value >> 15));
		encodeRawNumber(bb, short.class, zvalue);
	}

	public static short decodeShort(ByteBuffer bb) {
		byte type = bb.get();
		if (type != ZINT16_TYPE)
			throw new TypeMismatchException(ZINT16_TYPE, type, bb.position() - 1);

		short zvalue = (short) decodeRawNumber(bb);
		return (short) ((zvalue >> 1) ^ -(zvalue & 1));
	}

	public static void encodeString(ByteBuffer bb, String value) {
		bb.put(STRING_TYPE);
		try {
			byte[] buffer = value.getBytes("utf-8");
			encodeRawNumber(bb, int.class, buffer.length);
			bb.put(buffer);
		} catch (UnsupportedEncodingException e) {
		}
	}

	public static String decodeString(ByteBuffer bb) {
		byte type = bb.get();
		if (type != STRING_TYPE)
			throw new TypeMismatchException(STRING_TYPE, type, bb.position() - 1);

		int length = (int) decodeRawNumber(bb);

		int oldLimit = bb.limit();
		bb.limit(bb.position() + length);

		CharBuffer cb = Charset.forName("utf-8").decode(bb);
		String value = cb.toString();
		bb.limit(oldLimit);
		return value;
	}

	public static void encodeDate(ByteBuffer bb, Date value) {
		bb.put(DATE_TYPE);
		bb.putLong(value.getTime());
	}

	public static Date decodeDate(ByteBuffer bb) {
		byte type = bb.get();
		if (type != DATE_TYPE)
			throw new TypeMismatchException(DATE_TYPE, type, bb.position() - 1);

		return new Date(bb.getLong());
	}

	public static void encodeBoolean(ByteBuffer bb, boolean value) {
		bb.put(BOOLEAN_TYPE);
		bb.put((byte) (value ? 1 : 0));
	}

	public static boolean decodeBoolean(ByteBuffer bb) {
		byte type = bb.get();
		if (type != BOOLEAN_TYPE)
			throw new TypeMismatchException(BOOLEAN_TYPE, type, bb.position() - 1);

		byte value = bb.get();
		return value == 1;
	}

	public static void encodeIp4(ByteBuffer bb, Inet4Address value) {
		bb.put(IP4_TYPE);
		bb.put(value.getAddress());
	}

	public static InetAddress decodeIp4(ByteBuffer bb) {
		byte type = bb.get();
		if (type != IP4_TYPE)
			throw new TypeMismatchException(IP4_TYPE, type, bb.position() - 1);

		byte[] address = new byte[4];
		bb.get(address);
		try {
			return Inet4Address.getByAddress(address);
		} catch (UnknownHostException e) {
			// bytes always correct. ignore.
			return null;
		}
	}

	public static void encodeIp6(ByteBuffer bb, Inet6Address value) {
		bb.put(IP6_TYPE);
		bb.put(value.getAddress());
	}

	public static InetAddress decodeIp6(ByteBuffer bb) {
		byte type = bb.get();
		if (type != IP6_TYPE)
			throw new TypeMismatchException(IP6_TYPE, type, bb.position() - 1);

		byte[] address = new byte[16];
		bb.get(address);
		try {
			return Inet6Address.getByAddress(address);
		} catch (UnknownHostException e) {
			// bytes always correct. ignore.
			return null;
		}
	}

	public static void encodeMap(ByteBuffer bb, Map<String, Object> map) {
		bb.put(MAP_TYPE);

		int length = 0;
		for (String key : map.keySet()) {
			length += lengthOfString(key);
			length += lengthOf(map.get(key));
		}

		encodeRawNumber(bb, int.class, length);

		for (String key : map.keySet()) {
			encodeString(bb, key);
			encode(bb, map.get(key));
		}
	}

	public static Map<String, Object> decodeMap(ByteBuffer bb) {
		byte type = bb.get();
		if (type != MAP_TYPE)
			throw new TypeMismatchException(MAP_TYPE, type, bb.position() - 1);

		int length = (int) decodeRawNumber(bb);

		Map<String, Object> m = new HashMap<String, Object>();

		while (length > 0) {
			int before = bb.remaining();
			String key = decodeString(bb);
			Object value = decode(bb);
			int after = bb.remaining();

			m.put(key, value);
			length -= before - after;
		}

		return m;
	}

	public static void encodeArray(ByteBuffer bb, List<?> array) {
		bb.put(ARRAY_TYPE);

		int length = 0;
		for (Object obj : array) {
			length += lengthOf(obj);
		}

		encodeRawNumber(bb, int.class, length);

		for (Object obj : array) {
			encode(bb, obj);
		}
	}

	public static void encodeArray(ByteBuffer bb, Object[] array) {
		encodeArray(bb, Arrays.asList(array));
	}

	public static Object[] decodeArray(ByteBuffer bb) {
		byte type = bb.get();
		if (type != ARRAY_TYPE)
			throw new TypeMismatchException(ARRAY_TYPE, type, bb.position() - 1);

		int length = (int) decodeRawNumber(bb);

		ArrayList<Object> l = new ArrayList<Object>();
		while (length > 0) {
			int before = bb.remaining();
			l.add(decode(bb));
			int after = bb.remaining();
			length -= before - after;
		}

		return l.toArray();
	}

	public static void encodeBlob(ByteBuffer bb, byte[] buffer) {
		// 
		bb.put(BLOB_TYPE);
		encodeRawNumber(bb, int.class, buffer.length);
		bb.put(buffer);
	}

	public static byte[] decodeBlob(ByteBuffer bb) {
		byte type = bb.get();
		if (type != BLOB_TYPE)
			throw new TypeMismatchException(BLOB_TYPE, type, bb.position() - 1);

		int length = (int) decodeRawNumber(bb);
		byte[] blob = new byte[length];
		bb.get(blob);
		return blob;
	}
	
	public static int lengthOfLong(long value) {
		long zvalue = (value << 1) ^ (value >> 63);
		return 1 + lengthOfRawNumber(long.class, zvalue);
	}

	public static <T> int lengthOfRawNumber(Class<T> clazz, long value) {
		if (value < 0) {
			if (long.class == clazz)
				return 10; // max length for length
			else
				return 5; // max length for int
		}
		if (value == 0)
			return 1;

		return (63 - Long.numberOfLeadingZeros(value)) / 7 + 1;
	}

	public static <T> int lengthOfNumber(Class<T> clazz, long value) {
		if (clazz.equals(int.class)) {
			return lengthOfInt((int)value);
		} else if (clazz.equals(long.class)) {
			return lengthOfLong(value);
		} else if (clazz.equals(short.class)) {
			return lengthOfShort((short)value);
		} else {
			throw new UnsupportedTypeException("invalid number type: " + clazz.getName());
		}
	}
	
	public static int lengthOfInt(int value) {
		int zvalue = (value << 1) ^ (value >> 31);
		return 1 + lengthOfRawNumber(int.class, zvalue);
	}
	
	public static int lengthOfNull() {
		return 1;
	}

	public static int lengthOfShort(short value) {
		short zvalue = (short) ((value << 1) ^ (value >> 15));
		return 1 + lengthOfRawNumber(short.class, zvalue);
	}
	
	public static int lengthOfString(String value) {
		byte[] buffer = null;
		try {
			buffer = value.getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			//
			e.printStackTrace();
		}
		return 1 + lengthOfRawNumber(int.class, buffer.length) + buffer.length;
	}

	public static int lengthOfDate() {
		return 1 + 8;
	}

	public static int lengthOfBoolean(boolean value) {
		return 2;
	}

	public static int lengthOfIp4(Inet4Address value) {
		return 1 + value.getAddress().length;
	}

	public static int lengthOfIp6(Inet6Address value) {
		return 1 + value.getAddress().length;
	}

	public static int lengthOfMap(Map<String, Object> value) {
		int contentLength = 0;
		for (String key : value.keySet()) {
			contentLength += lengthOfString(key);
			contentLength += lengthOf(value.get(key));
		}
		return 1 + lengthOfRawNumber(int.class, contentLength) + contentLength;
	}

	public static int lengthOfArray(List<?> value) {
		int contentLength = 0;
		for (Object obj : value) {
			contentLength += lengthOf(obj);
		}
		return 1 + lengthOfRawNumber(int.class, contentLength) + contentLength;
	}

	public static int lengthOfArray(Object[] value) {
		return lengthOfArray(Arrays.asList(value));
	}

	public static int lengthOfBlob(byte[] value) {
		return 1 + lengthOfRawNumber(int.class, value.length) + value.length;
	}

}
