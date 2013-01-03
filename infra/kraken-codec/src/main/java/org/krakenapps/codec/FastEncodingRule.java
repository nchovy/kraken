/*
 * Copyright 2013 Future Systems
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

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class FastEncodingRule {

	public byte[] encodeRawNumber(Class<?> clazz, long value) {
		int len = EncodingRule.lengthOfRawNumber(clazz, value);
		byte[] b = new byte[len];

		// fast path
		if (len == 1) {
			b[0] = (byte) value;
			return b;
		}

		for (int i = 0; i < len; ++i) {
			byte signalBit = (byte) (i != len - 1 ? 0x80 : 0);
			byte data = (byte) (signalBit | (byte) (value >> (7 * (len - i - 1)) & 0x7F));
			b[i] = data;
		}

		return b;
	}

	public ByteBuffer encode(Object value) {
		return encode(value, null);
	}

	public ByteBuffer encode(Object value, FastCustomCodec cc) {
		BinaryForm bf = preencode(value, cc);
		ByteBuffer bb = ByteBuffer.allocate(bf.totalLength);
		encode(bb, bf);
		bb.flip();
		return bb;
	}

	public void encode(ByteBuffer bb, BinaryForm bf) {
		bb.put((byte) bf.type);

		switch (bf.type) {
		case EncodingRule.NULL_TYPE:
			break;
		case EncodingRule.STRING_TYPE:
		case EncodingRule.BLOB_TYPE:
			bb.put(bf.lengthBytes);
			bb.put(bf.payloadBytes);
			break;
		case EncodingRule.ZINT16_TYPE:
		case EncodingRule.ZINT32_TYPE:
		case EncodingRule.ZINT64_TYPE:
		case EncodingRule.DATE_TYPE:
		case EncodingRule.IP4_TYPE:
		case EncodingRule.IP6_TYPE:
		case EncodingRule.BOOLEAN_TYPE:
		case EncodingRule.FLOAT_TYPE:
		case EncodingRule.DOUBLE_TYPE:
			bb.put(bf.payloadBytes);
			break;
		case EncodingRule.MAP_TYPE:
		case EncodingRule.ARRAY_TYPE:
			bb.put(bf.lengthBytes);
			for (BinaryForm c : bf.children)
				encode(bb, c);
			break;
		default:
			bb.put(bf.lengthBytes);
			if (bf.payloadBytes != null) {
				bb.put(bf.payloadBytes);
			} else if (bf.children != null) {
				for (BinaryForm c : bf.children)
					encode(bb, c);
			}
		}
	}

	public BinaryForm preencode(Object value) {
		return preencode(value, null);
	}

	public BinaryForm preencode(Object value, FastCustomCodec cc) {
		if (value == null) {
			BinaryForm bf = new BinaryForm();
			bf.type = EncodingRule.NULL_TYPE;
			bf.totalLength = 1;
			return bf;
		} else if (value instanceof String) {
			return preencodeString(value);
		} else if (value instanceof Integer) {
			return preencodeInt(value);
		} else if (value instanceof Long) {
			return preencodeLong((Long) value);
		} else if (value instanceof Short) {
			return preencodeShort((Short) value);
		} else if (value instanceof Date) {
			return preencodeDate((Date) value);
		} else if (value instanceof Inet4Address) {
			return preencodeIp4((Inet4Address) value);
		} else if (value instanceof Inet6Address) {
			return preencodeIp6((Inet6Address) value);
		} else if (value instanceof Boolean) {
			return preencodeBoolean((Boolean) value);
		} else if (value instanceof Float) {
			return preencodeFloat(value);
		} else if (value instanceof Double) {
			return preencodeDouble(value);
		} else if (value instanceof Map<?, ?>) {
			return preencodeMap(value, cc);
		} else if (value instanceof List<?>) {
			return preencodeArray((List<?>) value, cc);
		} else if (value.getClass().isArray()) {
			Class<?> c = value.getClass().getComponentType();
			if (c == byte.class) {
				return preencodeBlob((byte[]) value);
			} else if (c == int.class) {
				return preencodeArray((int[]) value);
			} else if (c == long.class) {
				return preencodeArray((long[]) value);
			} else if (c == short.class) {
				return preencodeArray((short[]) value);
			} else if (c == boolean.class) {
				return preencodeArray((boolean[]) value);
			} else if (c == double.class) {
				return preencodeArray((double[]) value);
			} else if (c == float.class) {
				return preencodeArray((float[]) value);
			} else if (c == char.class) {
				throw new UnsupportedTypeException(value.getClass().getName());
			} else {
				return preencodeArray((Object[]) value, cc);
			}
		} else {
			if (cc != null)
				return cc.preencode(this, value);
			else
				throw new UnsupportedTypeException(value.getClass().getName());
		}
	}

	private BinaryForm preencodeIp6(Inet6Address value) {
		BinaryForm bf = new BinaryForm();
		bf.type = EncodingRule.IP6_TYPE;
		bf.payloadBytes = ((Inet6Address) value).getAddress();
		bf.totalLength = 1 + bf.payloadBytes.length;
		bf.value = value;
		return bf;
	}

	private BinaryForm preencodeBoolean(boolean value) {
		BinaryForm bf = new BinaryForm();
		bf.type = EncodingRule.BOOLEAN_TYPE;
		bf.payloadBytes = new byte[] { (byte) ((Boolean) value ? 1 : 0) };
		bf.totalLength = 2;
		bf.value = value;
		return bf;
	}

	private BinaryForm preencodeMap(Object value, FastCustomCodec cc) {
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) value;

		BinaryForm bf = new BinaryForm();
		bf.type = EncodingRule.MAP_TYPE;
		bf.children = new BinaryForm[map.size() * 2];

		int i = 0;
		int payloadLength = 0;
		for (Entry<String, Object> e : map.entrySet()) {
			BinaryForm k = preencodeString(e.getKey());
			BinaryForm v = preencode(e.getValue(), cc);
			bf.children[i++] = k;
			bf.children[i++] = v;
			payloadLength += k.totalLength + v.totalLength;
		}

		bf.lengthBytes = encodeRawNumber(int.class, payloadLength);
		bf.totalLength = 1 + bf.lengthBytes.length + payloadLength;
		bf.value = value;
		return bf;
	}

	private BinaryForm preencodeDouble(Object value) {
		long v = Double.doubleToLongBits((Double) value);
		byte[] b = new byte[8];
		for (int i = 7; i >= 0; i--) {
			b[i] = (byte) (v & 0xFF);
			v >>= 8;
		}

		BinaryForm bf = new BinaryForm();
		bf.type = EncodingRule.DOUBLE_TYPE;
		bf.payloadBytes = b;
		bf.totalLength = 1 + bf.payloadBytes.length;
		bf.value = value;
		return bf;
	}

	private BinaryForm preencodeFloat(Object value) {
		int v = Float.floatToIntBits((Float) value);
		byte[] b = new byte[4];
		for (int i = 3; i >= 0; i--) {
			b[i] = (byte) (v & 0xFF);
			v >>= 8;
		}

		BinaryForm bf = new BinaryForm();
		bf.type = EncodingRule.FLOAT_TYPE;
		bf.payloadBytes = b;
		bf.totalLength = 1 + bf.payloadBytes.length;
		bf.value = value;
		return bf;
	}

	private BinaryForm preencodeIp4(Inet4Address value) {
		BinaryForm bf = new BinaryForm();
		bf.type = EncodingRule.IP4_TYPE;
		bf.payloadBytes = value.getAddress();
		bf.totalLength = 1 + bf.payloadBytes.length;
		bf.value = value;
		return bf;
	}

	private BinaryForm preencodeInt(Object value) {
		BinaryForm bf = new BinaryForm();
		bf.type = EncodingRule.ZINT32_TYPE;

		int v = (Integer) value;
		long zvalue = ((long) v << 1) ^ ((long) v >> 31);
		bf.payloadBytes = encodeRawNumber(int.class, zvalue);
		bf.totalLength = 1 + bf.payloadBytes.length;
		bf.value = v;
		return bf;
	}

	private BinaryForm preencodeLong(long value) {
		BinaryForm bf = new BinaryForm();
		bf.type = EncodingRule.ZINT64_TYPE;

		long v = (Long) value;
		long zvalue = ((long) v << 1) ^ ((long) v >> 63);
		bf.payloadBytes = encodeRawNumber(long.class, zvalue);
		bf.totalLength = 1 + bf.payloadBytes.length;
		bf.value = v;
		return bf;
	}

	private BinaryForm preencodeDate(Date value) {
		BinaryForm bf = new BinaryForm();
		bf.type = EncodingRule.DATE_TYPE;

		long l = ((Date) value).getTime();
		byte[] b = new byte[8];
		for (int i = 7; i >= 0; i--) {
			b[i] = (byte) (l & 0xff);
			l >>= 8;
		}

		bf.payloadBytes = b;
		bf.totalLength = 1 + bf.payloadBytes.length;
		bf.value = value;
		return bf;
	}

	private BinaryForm preencodeShort(short value) {
		BinaryForm bf = new BinaryForm();
		bf.type = EncodingRule.ZINT16_TYPE;

		int v = (int) value;
		long zvalue = ((long) v << 1) ^ ((long) v >> 31);
		bf.payloadBytes = encodeRawNumber(short.class, zvalue);
		bf.totalLength = 1 + bf.payloadBytes.length;
		bf.value = v;
		return bf;
	}

	private BinaryForm preencodeString(Object value) {
		BinaryForm bf = new BinaryForm();
		EncodedStringCache k = EncodedStringCache.getEncodedString((String) value);
		bf.type = EncodingRule.STRING_TYPE;
		bf.payloadBytes = k.value();
		bf.lengthBytes = encodeRawNumber(int.class, bf.payloadBytes.length);
		bf.totalLength = 1 + bf.lengthBytes.length + bf.payloadBytes.length;
		bf.value = value;
		return bf;
	}

	public BinaryForm preencodeBlob(byte[] b) {
		BinaryForm bf = new BinaryForm();
		bf.type = EncodingRule.BLOB_TYPE;
		bf.payloadBytes = b;
		bf.lengthBytes = encodeRawNumber(int.class, bf.payloadBytes.length);
		bf.totalLength = 1 + bf.lengthBytes.length + bf.payloadBytes.length;
		bf.value = b;
		return bf;
	}

	public BinaryForm preencodeArray(Object[] array) {
		return preencodeArray(array, null);
	}

	public BinaryForm preencodeArray(Object[] array, FastCustomCodec cc) {
		return preencodeArray(Arrays.asList(array), cc);
	}

	public BinaryForm preencodeArray(List<?> array, FastCustomCodec cc) {
		BinaryForm bf = new BinaryForm();
		bf.type = EncodingRule.ARRAY_TYPE;
		bf.children = new BinaryForm[array.size()];
		int payloadLength = 0;

		int p = 0;
		for (Object obj : array) {
			BinaryForm c = preencode(obj, cc);
			payloadLength += c.totalLength;
			bf.children[p++] = c;
		}

		bf.lengthBytes = encodeRawNumber(int.class, payloadLength);
		bf.totalLength = 1 + bf.lengthBytes.length + payloadLength;
		bf.value = array;
		return bf;
	}

	public BinaryForm preencodeArray(int[] array) {
		BinaryForm bf = new BinaryForm();
		bf.type = EncodingRule.ARRAY_TYPE;
		bf.children = new BinaryForm[array.length];

		int p = 0;
		int payloadLength = 0;
		for (int i : array) {
			BinaryForm c = preencodeInt(i);
			bf.children[p++] = c;
			payloadLength += c.totalLength;
		}

		bf.lengthBytes = encodeRawNumber(int.class, payloadLength);
		bf.totalLength = 1 + bf.lengthBytes.length + payloadLength;
		bf.value = array;
		return bf;
	}

	public BinaryForm preencodeArray(long[] array) {
		BinaryForm bf = new BinaryForm();
		bf.type = EncodingRule.ARRAY_TYPE;
		bf.children = new BinaryForm[array.length];

		int p = 0;
		int payloadLength = 0;
		for (long i : array) {
			BinaryForm c = preencodeLong(i);
			bf.children[p++] = c;
			payloadLength += c.totalLength;
		}

		bf.lengthBytes = encodeRawNumber(int.class, payloadLength);
		bf.totalLength = 1 + bf.lengthBytes.length + payloadLength;
		bf.value = array;
		return bf;
	}

	public BinaryForm preencodeArray(short[] array) {
		BinaryForm bf = new BinaryForm();
		bf.type = EncodingRule.ARRAY_TYPE;
		bf.children = new BinaryForm[array.length];

		int p = 0;
		int payloadLength = 0;
		for (short i : array) {
			BinaryForm c = preencodeShort(i);
			bf.children[p++] = c;
			payloadLength += c.totalLength;
		}

		bf.lengthBytes = encodeRawNumber(int.class, payloadLength);
		bf.totalLength = 1 + bf.lengthBytes.length + payloadLength;
		bf.value = array;
		return bf;
	}

	public BinaryForm preencodeArray(double[] array) {
		BinaryForm bf = new BinaryForm();
		bf.type = EncodingRule.ARRAY_TYPE;
		bf.children = new BinaryForm[array.length];

		int p = 0;
		int payloadLength = 0;
		for (double i : array) {
			BinaryForm c = preencodeDouble(i);
			bf.children[p++] = c;
			payloadLength += c.totalLength;
		}

		bf.lengthBytes = encodeRawNumber(int.class, payloadLength);
		bf.totalLength = 1 + bf.lengthBytes.length + payloadLength;
		bf.value = array;
		return bf;
	}

	public BinaryForm preencodeArray(float[] array) {
		BinaryForm bf = new BinaryForm();
		bf.type = EncodingRule.ARRAY_TYPE;
		bf.children = new BinaryForm[array.length];

		int p = 0;
		int payloadLength = 0;
		for (float i : array) {
			BinaryForm c = preencodeFloat(i);
			bf.children[p++] = c;
			payloadLength += c.totalLength;
		}

		bf.lengthBytes = encodeRawNumber(int.class, payloadLength);
		bf.totalLength = 1 + bf.lengthBytes.length + payloadLength;
		bf.value = array;
		return bf;
	}

	public BinaryForm preencodeArray(boolean[] array) {
		BinaryForm bf = new BinaryForm();
		bf.type = EncodingRule.ARRAY_TYPE;
		bf.children = new BinaryForm[array.length];

		int p = 0;
		int payloadLength = 0;
		for (boolean i : array) {
			BinaryForm c = preencodeBoolean(i);
			bf.children[p++] = c;
			payloadLength += c.totalLength;
		}

		bf.lengthBytes = encodeRawNumber(int.class, payloadLength);
		bf.totalLength = 1 + bf.lengthBytes.length + payloadLength;
		bf.value = array;
		return bf;
	}
}
