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
	public static final byte FLOAT_TYPE = 15;
	public static final byte DOUBLE_TYPE = 16;

	private EncodingRule() {
	}

	public static void encode(ByteBuffer bb, Object value) {
		encode(bb, value, null);
	}

	@SuppressWarnings("unchecked")
	public static void encode(ByteBuffer bb, Object value, CustomCodec cc) {
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
			encodeMap(bb, (Map<String, Object>) value, cc);
		} else if (value instanceof List<?>) {
			encodeArray(bb, (List<?>) value, cc);
		} else if (value.getClass().isArray()) {
			Class<?> c = value.getClass().getComponentType();
			if (c == byte.class) {
				encodeBlob(bb, (byte[]) value);
			} else if (c == int.class) {
				encodeArray(bb, (int[]) value);
			} else if (c == long.class) {
				encodeArray(bb, (long[]) value);
			} else if (c == short.class) {
				encodeArray(bb, (short[]) value);
			} else if (c == boolean.class) {
				encodeArray(bb, (boolean[]) value);
			} else if (c == double.class) {
				encodeArray(bb, (double[]) value);
			} else if (c == float.class) {
				encodeArray(bb, (float[]) value);
			} else if (c == char.class) {
				throw new UnsupportedTypeException(value.getClass().getName());
			} else {
				encodeArray(bb, (Object[]) value, cc);
			}
		} else if (value instanceof Boolean) {
			encodeBoolean(bb, (Boolean) value);
		} else if (value instanceof Float) {
			encodeFloat(bb, (Float) value);
		} else if (value instanceof Double) {
			encodeDouble(bb, (Double) value);
		} else {
			if (cc != null)
				cc.encode(bb, value);
			else
				throw new UnsupportedTypeException(value.getClass().getName());
		}
	}

	@Deprecated
	public static int length(Object value) {
		return lengthOf(value);
	}

	public static int lengthOf(Object value) {
		return lengthOf(value, null);
	}

	@SuppressWarnings("unchecked")
	public static int lengthOf(Object value, CustomCodec cc) {
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
			return lengthOfMap((Map<String, Object>) value, cc);
		} else if (value instanceof List<?>) {
			return lengthOfArray((List<?>) value, cc);
		} else if (value.getClass().isArray()) {
			Class<?> c = value.getClass().getComponentType();
			if (c == byte.class) {
				return lengthOfBlob((byte[]) value);
			} else if (c == int.class) {
				return lengthOfArray((int[]) value);
			} else if (c == long.class) {
				return lengthOfArray((long[]) value);
			} else if (c == short.class) {
				return lengthOfArray((short[]) value);
			} else if (c == boolean.class) {
				return lengthOfArray((boolean[]) value);
			} else if (c == double.class) {
				return lengthOfArray((double[]) value);
			} else if (c == float.class) {
				return lengthOfArray((float[]) value);
			} else if (c == char.class) {
				throw new UnsupportedTypeException(value.getClass().getName());
			} else {
				return lengthOfArray((Object[]) value, cc);
			}
		} else if (value instanceof Boolean) {
			return lengthOfBoolean((Boolean) value);
		} else if (value instanceof Float) {
			return lengthOfFloat((Float) value);
		} else if (value instanceof Double) {
			return lengthOfDouble((Double) value);
		} else {
			if (cc != null)
				return cc.lengthOf(value);
			else
				throw new UnsupportedTypeException(value.getClass().getName());
		}
	}

	/*
	 * Return single object byte length(include type byte). ByteBuffer's
	 * position will not move.
	 */
	public static int getObjectLength(ByteBuffer bb) {
		return getObjectLength(bb, null);
	}

	public static int getObjectLength(ByteBuffer bb, CustomCodec cc) {
		ByteBuffer buf = bb.duplicate();
		int typeByte = buf.get();
		switch (typeByte) {
		case NULL_TYPE:
			return 1;
		case STRING_TYPE:
		case MAP_TYPE:
		case ARRAY_TYPE:
		case BLOB_TYPE: {
			int pos = buf.position();
			return 1 + (int) decodeRawNumber(buf) + (buf.position() - pos);
		}
		case INT32_TYPE:
			throw new UnsupportedTypeException("deprecated number type");
		case INT16_TYPE:
			throw new UnsupportedTypeException("deprecated number type");
		case INT64_TYPE:
			throw new UnsupportedTypeException("deprecated number type");
		case DATE_TYPE: {
			int pos = buf.position();
			buf.getLong();
			return 1 + (buf.position() - pos);
		}
		case IP4_TYPE:
			return 1 + 4;
		case IP6_TYPE:
			return 1 + 16;
		case BOOLEAN_TYPE:
			return 1 + 1;
		case ZINT32_TYPE:
		case ZINT16_TYPE:
		case ZINT64_TYPE: {
			int pos = buf.position();
			decodeRawNumber(buf);
			return 1 + (buf.position() - pos);
		}
		case FLOAT_TYPE:
			return 1 + 4;
		case DOUBLE_TYPE:
			return 1 + 8;
		}

		if (cc != null)
			return cc.getObjectLength(buf);
		else
			throw new UnsupportedTypeException("type: " + typeByte);
	}

	public static Object decode(ByteBuffer bb) {
		return decode(bb, null);
	}

	public static Object decode(ByteBuffer bb, CustomCodec cc) {
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
			return decodeMap(bb, cc);
		case ARRAY_TYPE:
			return decodeArray(bb, cc);
		case BLOB_TYPE:
			return decodeBlob(bb);
		case BOOLEAN_TYPE:
			return decodeBoolean(bb);
		case ZINT32_TYPE:
			return (int) decodeInt(bb);
		case ZINT16_TYPE:
			return (short) decodeShort(bb);
		case ZINT64_TYPE:
			return (long) decodeLong(bb);
		case FLOAT_TYPE:
			return (float) decodeFloat(bb);
		case DOUBLE_TYPE:
			return (double) decodeDouble(bb);
		}

		if (cc != null)
			return cc.decode(bb);
		else
			throw new UnsupportedTypeException("type: " + typeByte);
	}

	public static void encodeNull(ByteBuffer bb) {
		bb.put(NULL_TYPE);
	}

	public static void encodeNumber(ByteBuffer bb, Class<?> clazz, long value) {
		if (clazz.equals(int.class)) {
			encodeInt(bb, (int) value);
		} else if (clazz.equals(long.class)) {
			encodeLong(bb, value);
		} else if (clazz.equals(short.class)) {
			encodeShort(bb, (short) value);
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
		return ((zvalue >> 1) & 0x7FFFFFFFFFFFFFFFL) ^ -(zvalue & 1);
	}

	public static void encodeInt(ByteBuffer bb, int value) {
		bb.put(ZINT32_TYPE);
		long zvalue = ((long) value << 1) ^ ((long) value >> 31);
		encodeRawNumber(bb, int.class, zvalue);
	}

	public static int decodeInt(ByteBuffer bb) {
		byte type = bb.get();
		if (type != ZINT32_TYPE)
			throw new TypeMismatchException(ZINT32_TYPE, type, bb.position() - 1);

		int zvalue = (int) decodeRawNumber(bb);
		int v = (int) (((zvalue >> 1) & 0x7FFFFFFF) ^ -(zvalue & 1));
		return v;
	}

	public static void encodeShort(ByteBuffer bb, short value) {
		bb.put(ZINT16_TYPE);
		long zvalue = ((long) value << 1) ^ ((long) value >> 15);
		encodeRawNumber(bb, short.class, zvalue);
	}

	public static short decodeShort(ByteBuffer bb) {
		byte type = bb.get();
		if (type != ZINT16_TYPE)
			throw new TypeMismatchException(ZINT16_TYPE, type, bb.position() - 1);

		long zvalue = decodeRawNumber(bb);
		return (short) (((zvalue >> 1) & 0x7FFF) ^ -(zvalue & 1));
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

	private static final Charset utf8 = Charset.forName("utf-8");

	public static String decodeString(ByteBuffer bb) {
		byte type = bb.get();
		if (type != STRING_TYPE)
			throw new TypeMismatchException(STRING_TYPE, type, bb.position() - 1);

		int length = (int) decodeRawNumber(bb);

		int oldLimit = bb.limit();
		bb.limit(bb.position() + length);

		CharBuffer cb = utf8.decode(bb);
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
		encodeMap(bb, map, null);
	}

	public static void encodeMap(ByteBuffer bb, Map<String, Object> map, CustomCodec cc) {
		bb.put(MAP_TYPE);

		int length = preencodeMap(map, 0, cc);
		encodeRawNumber(bb, int.class, length);

		for (String key : map.keySet()) {
			EncodedStringCache k = EncodedStringCache.getEncodedString(key);
			bb.put(STRING_TYPE);
			encodeRawNumber(bb, int.class, k.value().length);
			bb.put(k.value());

			Object value = map.get(key);
			if (value instanceof String) {
				EncodedStringCache v = EncodedStringCache.getEncodedString((String) value);
				bb.put(STRING_TYPE);
				encodeRawNumber(bb, int.class, v.value().length);
				bb.put(v.value());
			} else
				encode(bb, value, cc);
		}
	}

	/**
	 * preencodeMap track down recursively and sum all content length
	 * 
	 * @return content length except type and length bytes
	 */
	@SuppressWarnings("unchecked")
	private static int preencodeMap(Map<String, Object> map, int depth, CustomCodec cc) {
		int length = 0;

		for (String key : map.keySet()) {
			EncodedStringCache k = EncodedStringCache.getEncodedString(key);
			length += k.length();

			Object value = map.get(key);
			if (value instanceof String)
				length += EncodedStringCache.getEncodedString((String) value).length();
			else if (value instanceof Map)
				length += preencodeMap((Map<String, Object>) value, depth + 1, cc);
			else if (value instanceof List)
				length += preencodeArray((List<?>) value, depth + 1, cc);
			else if (value instanceof Object[])
				length += preencodeArray((Object[]) value, depth + 1, cc);
			else
				length += lengthOf(value, cc);
		}

		if (depth == 0)
			return length;
		else
			return 1 + lengthOfRawNumber(int.class, length) + length;
	}

	public static Map<String, Object> decodeMap(ByteBuffer bb) {
		return decodeMap(bb, null);
	}

	public static Map<String, Object> decodeMap(ByteBuffer bb, CustomCodec cc) {
		byte type = bb.get();
		if (type != MAP_TYPE)
			throw new TypeMismatchException(MAP_TYPE, type, bb.position() - 1);

		int length = (int) decodeRawNumber(bb);

		HashMap<String, Object> m = new HashMap<String, Object>();

		while (length > 0) {
			int before = bb.remaining();
			String key = decodeString(bb);
			Object value = decode(bb, cc);
			int after = bb.remaining();

			m.put(key, value);
			length -= before - after;
		}

		return m;
	}

	public static void encodeArray(ByteBuffer bb, List<?> array) {
		encodeArray(bb, array, null);
	}

	public static void encodeArray(ByteBuffer bb, List<?> array, CustomCodec cc) {
		bb.put(ARRAY_TYPE);

		int length = preencodeArray(array, 0, cc);
		encodeRawNumber(bb, int.class, length);

		for (Object obj : array) {
			if (obj instanceof String) {
				EncodedStringCache es = EncodedStringCache.getEncodedString((String) obj);
				bb.put(STRING_TYPE);
				encodeRawNumber(bb, int.class, es.value().length);
				bb.put(es.value());
			} else
				encode(bb, obj, cc);
		}
	}

	public static void encodeArray(ByteBuffer bb, int[] array) {
		bb.put(ARRAY_TYPE);

		int contentLength = 0;
		for (int i : array)
			contentLength += lengthOfInt(i);

		encodeRawNumber(bb, int.class, contentLength);

		for (int i : array)
			encodeInt(bb, i);
	}

	public static void encodeArray(ByteBuffer bb, long[] array) {
		bb.put(ARRAY_TYPE);

		int contentLength = 0;
		for (long i : array)
			contentLength += lengthOfLong(i);

		encodeRawNumber(bb, int.class, contentLength);

		for (long i : array)
			encodeLong(bb, i);
	}

	public static void encodeArray(ByteBuffer bb, short[] array) {
		bb.put(ARRAY_TYPE);

		int contentLength = 0;
		for (short i : array)
			contentLength += lengthOfShort(i);

		encodeRawNumber(bb, int.class, contentLength);

		for (short i : array)
			encodeShort(bb, i);
	}

	public static void encodeArray(ByteBuffer bb, double[] array) {
		bb.put(ARRAY_TYPE);

		int contentLength = 0;
		for (double i : array)
			contentLength += lengthOfDouble(i);

		encodeRawNumber(bb, int.class, contentLength);

		for (double i : array)
			encodeDouble(bb, i);
	}

	public static void encodeArray(ByteBuffer bb, float[] array) {
		bb.put(ARRAY_TYPE);

		int contentLength = 0;
		for (float i : array)
			contentLength += lengthOfFloat(i);

		encodeRawNumber(bb, int.class, contentLength);
		for (float i : array)
			encodeFloat(bb, i);
	}

	public static void encodeArray(ByteBuffer bb, boolean[] array) {
		bb.put(ARRAY_TYPE);

		int contentLength = 0;
		for (boolean i : array)
			contentLength += lengthOfBoolean(i);

		encodeRawNumber(bb, int.class, contentLength);

		for (boolean i : array)
			encodeBoolean(bb, i);
	}

	/**
	 * preencodeArray track down recursively and sum all content length
	 * 
	 * @return content length except type and length bytes
	 */
	@SuppressWarnings("unchecked")
	private static int preencodeArray(List<?> array, int depth, CustomCodec cc) {
		int length = 0;

		for (Object object : array) {
			if (object instanceof String)
				length += EncodedStringCache.getEncodedString((String) object).length();
			else if (object instanceof Map)
				length += preencodeMap((Map<String, Object>) object, depth + 1, cc);
			else if (object instanceof List)
				length += preencodeArray((List<?>) object, depth + 1, cc);
			else if (object instanceof Object[])
				length += preencodeArray((Object[]) object, depth + 1, cc);
			else
				length += lengthOf(object, cc);
		}

		if (depth == 0)
			return length;
		else
			return 1 + lengthOfRawNumber(int.class, length) + length;
	}

	public static void encodeArray(ByteBuffer bb, Object[] array) {
		encodeArray(bb, array, null);
	}

	public static void encodeArray(ByteBuffer bb, Object[] array, CustomCodec cc) {
		encodeArray(bb, Arrays.asList(array), cc);
	}

	private static int preencodeArray(Object[] array, int depth, CustomCodec cc) {
		return preencodeArray(Arrays.asList(array), depth, cc);
	}

	public static Object[] decodeArray(ByteBuffer bb) {
		return decodeArray(bb, null);
	}

	public static Object[] decodeArray(ByteBuffer bb, CustomCodec cc) {
		byte type = bb.get();
		if (type != ARRAY_TYPE)
			throw new TypeMismatchException(ARRAY_TYPE, type, bb.position() - 1);

		int length = (int) decodeRawNumber(bb);

		ArrayList<Object> l = new ArrayList<Object>();
		while (length > 0) {
			int before = bb.remaining();
			l.add(decode(bb, cc));
			int after = bb.remaining();
			length -= before - after;
		}

		return l.toArray();
	}

	public static void encodeBlob(ByteBuffer bb, byte[] buffer) {
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

	public static void encodeFloat(ByteBuffer bb, float value) {
		bb.put(FLOAT_TYPE);
		int v = Float.floatToIntBits(value);
		byte[] b = new byte[4];
		for (int i = 3; i >= 0; i--) {
			b[i] = (byte) (v & 0xFF);
			v >>= 8;
		}
		bb.put(b);
	}

	public static float decodeFloat(ByteBuffer bb) {
		byte type = bb.get();
		if (type != FLOAT_TYPE)
			throw new TypeMismatchException(FLOAT_TYPE, type, bb.position() - 1);

		byte[] b = new byte[4];
		bb.get(b);
		int v = 0;
		for (int i = 0; i < 4; i++) {
			v <<= 8;
			v |= b[i] & 0xFF;
		}
		return Float.intBitsToFloat(v);
	}

	public static void encodeDouble(ByteBuffer bb, double value) {
		bb.put(DOUBLE_TYPE);
		long v = Double.doubleToLongBits(value);
		byte[] b = new byte[8];
		for (int i = 7; i >= 0; i--) {
			b[i] = (byte) (v & 0xFF);
			v >>= 8;
		}
		bb.put(b);
	}

	public static double decodeDouble(ByteBuffer bb) {
		byte type = bb.get();
		if (type != DOUBLE_TYPE)
			throw new TypeMismatchException(DOUBLE_TYPE, type, bb.position() - 1);

		byte[] b = new byte[8];
		bb.get(b);
		long v = 0;
		for (int i = 0; i < 8; i++) {
			v <<= 8;
			v |= b[i] & 0xFF;
		}
		return Double.longBitsToDouble(v);
	}

	public static int lengthOfLong(long value) {
		long zvalue = (value << 1) ^ (value >> 63);
		return 1 + lengthOfRawNumber(long.class, zvalue);
	}

	public static <T> int lengthOfRawNumber(Class<T> clazz, long value) {
		if (value < 0) {
			if (long.class == clazz)
				return 10; // max length for long
			else if (int.class == clazz)
				return 5; // max length for int
			else
				return 3; // max length for short
		} else {
			if (value <= 127)
				return 1;
			if (value <= 16383)
				return 2;
		}

		return (63 - Long.numberOfLeadingZeros(value)) / 7 + 1;
	}

	public static <T> int lengthOfNumber(Class<T> clazz, long value) {
		if (clazz.equals(int.class)) {
			return lengthOfInt((int) value);
		} else if (clazz.equals(long.class)) {
			return lengthOfLong(value);
		} else if (clazz.equals(short.class)) {
			return lengthOfShort((short) value);
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
		return lengthOfMap(value, null);
	}

	public static int lengthOfMap(Map<String, Object> value, CustomCodec cc) {
		int contentLength = 0;
		for (String key : value.keySet()) {
			contentLength += lengthOfString(key);
			contentLength += lengthOf(value.get(key), cc);
		}
		return 1 + lengthOfRawNumber(int.class, contentLength) + contentLength;
	}

	public static int lengthOfArray(List<?> value) {
		return lengthOfArray(value, null);
	}

	public static int lengthOfArray(int[] value) {
		int contentLength = 0;
		for (int obj : value) {
			contentLength += lengthOfInt(obj);
		}
		return 1 + lengthOfRawNumber(int.class, contentLength) + contentLength;
	}

	public static int lengthOfArray(long[] value) {
		int contentLength = 0;
		for (long obj : value) {
			contentLength += lengthOfLong(obj);
		}
		return 1 + lengthOfRawNumber(int.class, contentLength) + contentLength;
	}

	public static int lengthOfArray(short[] value) {
		int contentLength = 0;
		for (short obj : value) {
			contentLength += lengthOfShort(obj);
		}
		return 1 + lengthOfRawNumber(int.class, contentLength) + contentLength;
	}

	public static int lengthOfArray(boolean[] value) {
		int contentLength = 0;
		for (boolean obj : value) {
			contentLength += lengthOfBoolean(obj);
		}
		return 1 + lengthOfRawNumber(int.class, contentLength) + contentLength;
	}

	public static int lengthOfArray(double[] value) {
		int contentLength = 0;
		for (double obj : value) {
			contentLength += lengthOfDouble(obj);
		}
		return 1 + lengthOfRawNumber(int.class, contentLength) + contentLength;
	}

	public static int lengthOfArray(float[] value) {
		int contentLength = 0;
		for (float obj : value) {
			contentLength += lengthOfFloat(obj);
		}
		return 1 + lengthOfRawNumber(int.class, contentLength) + contentLength;
	}

	public static int lengthOfArray(List<?> value, CustomCodec cc) {
		int contentLength = 0;
		for (Object obj : value) {
			contentLength += lengthOf(obj, cc);
		}
		return 1 + lengthOfRawNumber(int.class, contentLength) + contentLength;
	}

	public static int lengthOfArray(Object[] value) {
		return lengthOfArray(value, null);
	}

	public static int lengthOfArray(Object[] value, CustomCodec cc) {
		return lengthOfArray(Arrays.asList(value), cc);
	}

	public static int lengthOfBlob(byte[] value) {
		return 1 + lengthOfRawNumber(int.class, value.length) + value.length;
	}

	public static int lengthOfFloat(float value) {
		return 1 + 4;
	}

	public static int lengthOfDouble(double value) {
		return 1 + 8;
	}
}
