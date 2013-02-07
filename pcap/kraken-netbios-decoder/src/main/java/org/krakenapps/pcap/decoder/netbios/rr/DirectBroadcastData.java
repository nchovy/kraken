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
import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.util.Buffer;

public class DirectBroadcastData implements DatagramData {
	private String sourceName;
	private String DestName;
	private Buffer userData;
	private byte domainType;
	
	private DirectBroadcastData() {

	}
	
	public byte getDomainType() {
		return domainType;
	}

	public void setDomainType(byte domainType) {
		this.domainType = domainType;
	}

	public String getSoruceName() {
		return sourceName;
	}

	public void setSoruceName(String soruceName) {
		this.sourceName = soruceName;
	}

	public String getDestName() {
		return DestName;
	}

	public void setDestName(String destName) {
		DestName = destName;
	}

	public Buffer getUserData() {
		return userData;
	}

	public void setUserData(Buffer userData) {
		this.userData = userData;
	}

	public static DirectBroadcastData parse(Buffer b) {
		DirectBroadcastData data = new DirectBroadcastData();
		data.setDomainType(NetBiosNameCodec.decodeDomainType(b));
		data.setSoruceName(NetBiosNameCodec.readName(b));
		data.setDestName(NetBiosNameCodec.readName(b));
		data.userData = b;
		return data;
	}

	@Override
	public String toString() {
		return String.format("DatagramData DirectBroadcastData\n"+
				"sourceName=%s, DestName=%s\n", sourceName, DestName);
	}
}
