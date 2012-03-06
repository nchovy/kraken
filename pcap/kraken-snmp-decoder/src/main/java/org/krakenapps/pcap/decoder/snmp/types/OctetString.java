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

import java.io.UnsupportedEncodingException;

public class OctetString extends Variable {
	private String value;

	public OctetString(byte[] buffer, int offset, int length) {
		try {
			value = new String(buffer, offset, length, "US-ASCII");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			value = "";
		}

		type = Variable.OCTET_STRING;
	}

	public String get() {
		return value;
	}

	public String toString() {
		String out = "OCTETSTRING " + value;
		return out;
	}
}