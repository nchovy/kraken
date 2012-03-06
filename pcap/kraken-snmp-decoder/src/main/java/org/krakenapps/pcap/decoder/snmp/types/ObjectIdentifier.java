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

import java.util.ArrayList;
import java.util.List;

public class ObjectIdentifier extends Variable {
	private List<Integer> identifiers;
	private int curLength;
	private int curOffset;

	public ObjectIdentifier(byte[] buffer, int offset, int length) {
		identifiers = new ArrayList<Integer>();

		curOffset = offset;
		curLength = length;

		while (curLength > 0) {
			int subId = parseSubIdentifier(buffer);
			identifiers.add(new Integer(subId));
		}

		int compoundId = identifiers.get(0).intValue();
		identifiers.remove(0);

		int firstId = compoundId / 40;
		int secondId = compoundId % 40;

		identifiers.add(0, new Integer(secondId));
		identifiers.add(0, new Integer(firstId));

		type = Variable.OBJECT_IDENTIFIER;
	}

	public String get() {
		String out = "";
		for (int i = 0; i < identifiers.size(); i++) {
			Integer item = identifiers.get(i);
			if (i != 0)
				out += ".";
			if (item != null)
				out += item.toString();
		}
		return out;
	}

	private int parseSubIdentifier(byte[] buffer) {
		int value = 0;
		boolean isLastByte = false;

		while (!isLastByte && curLength > 0) {
			if ((buffer[curOffset] & 0x80) == 0)
				isLastByte = true;

			byte b = (byte) (buffer[curOffset] & 0x7F);
			value = (value << 7) | b;

			curLength--;
			curOffset++;
		}

		return value;
	}

	public String toString() {
		return "OID " + get();
	}
}
