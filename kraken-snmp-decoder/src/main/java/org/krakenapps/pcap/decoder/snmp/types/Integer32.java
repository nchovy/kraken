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

public class Integer32 extends Variable {
	private long value;

	public Integer32(byte[] buffer, int offset, int length) {
		int i = offset;

		value = 0;
		if ((buffer[offset] & 0x80) == 0x80)
			value = -1;

		for (i = offset; i < offset + length; i++) {
			int n = buffer[i];

			if (n < 0)
				n = 256 + n;

			value = (value << 8) | n;
		}

		type = Variable.INTEGER32;
	}

	public long get() {
		return value;
	}

	public String toString() {
		return "INTEGER32 " + value;
	}
}
