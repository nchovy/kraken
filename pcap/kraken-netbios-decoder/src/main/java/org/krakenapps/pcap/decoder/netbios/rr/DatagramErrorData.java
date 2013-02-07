/*
 * Copyright 2011 Future Systems, Inc
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
package org.krakenapps.pcap.decoder.netbios.rr;

import org.krakenapps.pcap.decoder.netbios.DatagramData;
import org.krakenapps.pcap.util.Buffer;

public class DatagramErrorData implements DatagramData {

	public final static byte DestinationNameNotPresent = (byte) 0x82;
	public final static byte InvalidSourceNameFormat = (byte) 0x83;
	public final static byte InvalidDestinationNameFormat = (byte) 0x84;
	private byte errorCode;

	private DatagramErrorData() {

	}

	public byte getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(byte errorCode) {
		this.errorCode = errorCode;
	}

	public static DatagramErrorData parse(Buffer b) {
		DatagramErrorData data = new DatagramErrorData();
		data.setErrorCode(b.get());
		return data;
	}

	@Override
	public String toString() {
		return String.format("DatagramErrorData\n"+"ErrorCode=0x%s\n", Integer.toHexString(errorCode));
	}
}
