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

import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.decoder.netbios.NetBiosSessionData;
import org.krakenapps.pcap.util.Buffer;

public class SessionRequest implements NetBiosSessionData {
	private String calledName;
	private String callingName;
	private byte domainType;
	private SessionRequest(String calledName, String callingName) {
		this.calledName = calledName;
		this.callingName = callingName;
	}
	private SessionRequest(String calledName, String callingName , byte domainType) {
		this.calledName = calledName;
		this.callingName = callingName;
		this.domainType = domainType;
	}
	public byte getDomainType() {
		return domainType;
	}
	public void setDomainType(byte domainType) {
		this.domainType = domainType;
	}
	public String getCalledName() {
		return calledName;
	}

	public String getCallingName() {
		return callingName;
	}

	public static NetBiosSessionData parse(Buffer b) {
		byte domainType = NetBiosNameCodec.decodeDomainType(b);
		String calledName = NetBiosNameCodec.readName(b);
		String callingName = NetBiosNameCodec.readName(b);
		return new SessionRequest(calledName, callingName , domainType);
	}

	@Override
	public String toString() {
		return String.format("NetBiosSessionData SessionRequest" +
				"calledName=%s , callingName=%s", this.calledName, this.callingName);
	}
	@Override
	public Buffer getBuffer() {
		// TODO Auto-generated method stub
		return null;
	}
}
