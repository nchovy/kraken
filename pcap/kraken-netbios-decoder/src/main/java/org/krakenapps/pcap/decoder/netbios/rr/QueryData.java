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

public class QueryData implements DatagramData {
	private String destName;
	private byte domainType;
	
	public byte getDomainType() {
		return domainType;
	}

	public void setDomainType(byte domainType) {
		this.domainType = domainType;
	}

	public QueryData(String destName) {
		this.destName = destName;
	}
	public QueryData(String destName , byte domainType) {
		this.domainType = domainType;
		this.destName = destName;
	}

	public String getDestName() {
		return destName;
	}

	public static QueryData parse(Buffer b) {
		byte domainType = NetBiosNameCodec.decodeDomainType(b);
		return new QueryData(NetBiosNameCodec.readName(b) , domainType);
	}

	@Override
	public String toString() {
		return String.format("DatagramData QueryData"+
				"query data: dest name=%s", destName);
	}
}
