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
package org.krakenapps.radius.protocol;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public abstract class RadiusAttribute {
	public abstract byte[] getBytes();

	/**
	 * RADIUS RFC treats string as a binary data
	 */
	public static byte[] encodeString(int type, byte[] b) {
		// type, length, string
		ByteBuffer bb = ByteBuffer.allocate(2 + b.length);
		bb.put((byte) type);
		bb.put((byte) (2 + b.length));
		bb.put(b);
		return bb.array();
	}

	public static byte[] encodeInt(int type, int value) {
		ByteBuffer bb = ByteBuffer.allocate(6);
		bb.put((byte) type);
		bb.put((byte) 6);
		bb.putInt(value);
		return bb.array();

	}

	public static byte[] encodeIp(int type, InetAddress ip) {
		ByteBuffer bb = ByteBuffer.allocate(6);
		bb.put((byte) type);
		bb.put((byte) 6);
		bb.put(ip.getAddress());
		return bb.array();
	}

	public static byte[] encodeText(int type, String text) {
		try {
			byte[] b = text.getBytes("utf-8");
			int len = 2 + b.length;
			ByteBuffer bb = ByteBuffer.allocate(len);
			bb.put((byte) type);
			bb.put((byte) len);
			bb.put(b);
			return bb.array();
		} catch (UnsupportedEncodingException e) {
			// not reachable
			return null;
		}
	}

	public static int decodeInt(byte[] b, int offset, int length) {
		int len = b[offset + 1];
		if (len != 6)
			throw new IllegalArgumentException("invalid length (should be 6): " + len);

		if (len > length)
			throw new BufferUnderflowException();

		ByteBuffer bb = ByteBuffer.wrap(b);
		bb.position(offset + 2);
		return bb.getInt();
	}

	public static InetAddress decodeIp(byte[] b, int offset, int length) throws UnknownHostException {
		int len = b[offset + 1];
		if (len != 6)
			throw new IllegalArgumentException("invalid length (should be 6): " + len);

		if (len > length)
			throw new BufferUnderflowException();

		byte[] ip = new byte[] { b[2 + offset], b[3 + offset], b[4 + offset], b[5 + offset] };
		return InetAddress.getByAddress(ip);
	}

	public static byte[] decodeString(byte[] b, int offset, int length) {
		int len = b[offset + 1];
		ByteBuffer bb = ByteBuffer.allocate(len - 2);
		bb.put(b, offset + 2, len - 2);
		return bb.array();
	}

	public static String decodeText(byte[] b, int offset, int length) {
		int len = b[offset + 1];
		if (len < 3)
			throw new IllegalArgumentException("empty text is not allowed");

		if (len > length)
			throw new BufferUnderflowException();

		try {
			return new String(b, offset + 2, length - 2, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// not reachable
			return null;
		}
	}
}
