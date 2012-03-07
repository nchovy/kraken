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
package org.krakenapps.pcap.decoder.snmp.types;

public class Variable {
	public static final int UNKNOWN = 0;
	public static final int BOOLEAN = 1;
	public static final int INTEGER32 = 2;
	public static final int DISPLAY_STRING = 3;
	public static final int OCTET_STRING = 4;
	public static final int OBJECT_IDENTIFIER = 6;
	public static final int SEQUENCE = 0x30;
	public static final int TIME_TICKS = 0x43;
	public static final int NETWORK_ADDRESS = 0x40;
	public static final int PDU = 0xA0;

	protected int type;

	public int getType() {
		return type;
	}

	public static Variable decode(byte[] buffer, int offset, int length) {
		int nextType = parseNextType(buffer[offset]);
		int nextLength = Variable.getNextLength(buffer, offset + 1);
		int nextOffset = offset + 1 + Variable.getLengthByteCount(nextLength);

		switch (nextType) {
		case Variable.INTEGER32:
			return new Integer32(buffer, nextOffset, nextLength);
		case Variable.OCTET_STRING:
			return new OctetString(buffer, nextOffset, nextLength);
		case Variable.OBJECT_IDENTIFIER:
			return new ObjectIdentifier(buffer, nextOffset, nextLength);
		case Variable.TIME_TICKS:
			return new TimeTicks(buffer, nextOffset, nextLength);
		case Variable.NETWORK_ADDRESS:
			return new NetworkAddress(buffer, nextOffset, nextLength);
		case Variable.SEQUENCE:
			return new Sequence(buffer, nextOffset, nextLength);
		default:
			if (nextType >= 0xA0)
				return new RawPdu(buffer, nextOffset, nextLength, nextType);
			return null;
		}
	}

	private static int parseNextType(byte value) {
		if (value < 0)
			return 256 + value;
		return value;
	}

	private static int getUnsigned(byte b) {
		if (b < 0)
			return 256 + b;
		return b;
	}

	public static int getNextLength(byte[] buffer, int index) {
		int i = index, b;

		b = getUnsigned(buffer[i]);

		if (b == 0x81)
			return getUnsigned(buffer[i + 1]);

		if (b == 0x82)
			return getUnsigned(buffer[i + 1]) * 256 + getUnsigned(buffer[i + 2]);

		return b;
	}

	protected static int getLengthByteCount(int length) {
		if (length < 0x80)
			return 1;
		else if (length >= 0x80 && length <= 0xFF)
			return 2;
		else
			return 3;
	}
}
