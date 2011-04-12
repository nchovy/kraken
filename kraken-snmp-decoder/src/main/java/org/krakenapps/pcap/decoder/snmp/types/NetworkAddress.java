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

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetworkAddress extends Variable {
	private byte[] value;

	public NetworkAddress(byte[] buffer, int offset, int length) {
		value = new byte[4];

		int i = offset;
		if (length == 4) {
			value[0] = buffer[i];
			value[1] = buffer[i + 1];
			value[2] = buffer[i + 2];
			value[3] = buffer[i + 3];
		} else {
		}

		type = Variable.NETWORK_ADDRESS;
	}

	public InetAddress get() {
		try {
			return InetAddress.getByAddress(value);
		} catch (UnknownHostException e) {
			return null;
		}
	}

	public String toString() {
		return String.format("NetworkAddress %d.%d.%d.%d", getUnsigned(value[0]), getUnsigned(value[1]),
				getUnsigned(value[2]), getUnsigned(value[3]));
	}

	private int getUnsigned(byte b) {
		if (b < 0)
			return 256 + b;
		return b;
	}
}