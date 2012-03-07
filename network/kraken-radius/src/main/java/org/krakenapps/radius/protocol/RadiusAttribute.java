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
	public abstract int getType();

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
			return new String(b, offset + 2, len - 2, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// not reachable
			return null;
		}
	}

	public static RadiusAttribute parse(byte[] authenticator, String sharedSecret, byte[] b, int offset, int length)
			throws UnknownHostException {
		int type = b[offset];

		switch (type) {
		case 1:
			return new UserNameAttribute(b, offset, length);
		case 2:
			return new UserPasswordAttribute(authenticator, sharedSecret, b, offset, length);
		case 3:
			return new ChapPasswordAttribute(b, offset, length);
		case 4:
			return new NasIpAddressAttribute(b, offset, length);
		case 5:
			return new NasPortAttribute(b, offset, length);
		case 6:
			return new ServiceTypeAttribute(b, offset, length);
		case 7:
			return new FramedProtocolAttribute(b, offset, length);
		case 8:
			return new FramedIpAddressAttribute(b, offset, length);
		case 9:
			return new FramedIpNetmaskAttribute(b, offset, length);
		case 10:
			return new FramedRoutingAttribute(b, offset, length);
		case 11:
			return new FilterIdAttribute(b, offset, length);
		case 12:
			return new FramedMtuAttribute(b, offset, length);
		case 13:
			return new FramedCompressionAttribute(b, offset, length);
		case 14:
			return new LoginIpHostAttribute(b, offset, length);
		case 15:
			return new LoginServiceAttribute(b, offset, length);
		case 16:
			return new LoginTcpPortAttribute(b, offset, length);
		case 18:
			return new ReplyMessageAttribute(b, offset, length);
		case 19:
			return new CallbackNumberAttribute(b, offset, length);
		case 20:
			return new CallbackIdAttribute(b, offset, length);
		case 22:
			return new FramedRouteAttribute(b, offset, length);
		case 23:
			return new FramedIpxNetworkAttribute(b, offset, length);
		case 24:
			return new StateAttribute(b, offset, length);
		case 25:
			return new ClassAttribute(b, offset, length);
		case 26:
			return new VendorSpecificAttribute(b, offset, length);
		case 27:
			return new SessionTimeoutAttribute(b, offset, length);
		case 28:
			return new IdleTimeoutAttribute(b, offset, length);
		case 29:
			return new TerminationActionAttribute(b, offset, length);
		case 30:
			return new CalledStationIdAttribute(b, offset, length);
		case 31:
			return new CallingStationIdAttribute(b, offset, length);
		case 32:
			return new NasIdentifierAttribute(b, offset, length);
		case 33:
			return new ProxyStateAttribute(b, offset, length);
		case 34:
			return new LoginLatServiceAttribute(b, offset, length);
		case 35:
			return new LoginLatNodeAttribute(b, offset, length);
		case 36:
			return new LoginLatGroupAttribute(b, offset, length);
		case 37:
			return new FramedAppleTalkLinkAttribute(b, offset, length);
		case 38:
			return new FramedAppleTalkNetworkAttribute(b, offset, length);
		case 39:
			return new FramedAppleTalkZoneAttribute(b, offset, length);
		case 60:
			return new ChapChallengeAttribute(b, offset, length);
		case 61:
			return new NasPortTypeAttribute(b, offset, length);
		case 62:
			return new PortLimitAttribute(b, offset, length);
		case 63:
			return new LoginLatPortAttribute(b, offset, length);
		default:
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public String toString() {
		return "type=" + getType() + ", len=" + getBytes().length;
	}
}
