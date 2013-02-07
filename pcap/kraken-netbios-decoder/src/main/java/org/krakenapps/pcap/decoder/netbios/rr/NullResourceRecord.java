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

import org.krakenapps.pcap.util.Buffer;

public class NullResourceRecord extends ResourceRecord {
	private int ttl;
	private short rdLength; // it must 0x0002
	private short nmFlags;

	public NullResourceRecord(String name) {
		super(name);
	}

	public short getNmFlags() {
		return nmFlags;
	}

	public void setNmFlags(short nmFlags) {
		this.nmFlags = nmFlags;
	}

	public int getTtl() {
		return ttl;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}

	public short getRdLength() {
		return rdLength;
	}

	public void setRdLength(short rdLength) {
		this.rdLength = rdLength;
	}

	@Override
	public void parse(Buffer b, int type) {
		this.setType(Type.parse(type));
		this.setCls(b.getShort());
		this.setTtl(b.getInt());
		this.setRdLength(b.getShort());
		this.setNmFlags(b.getShort());
	}
	@Override
	public String toString(){
		return String.format("ResoruceRecord(NullResourceRecord)\n"+
				"type = %s , cls(class) = 0x%s , ttl = 0x%s\n"+
				"Rdlength = 0x%s , Nmflags = 0x%s\n"
				, this.type , Integer.toHexString(this.cls) , Integer.toHexString(this.ttl),
				Integer.toHexString(this.rdLength) , Integer.toHexString(this.nmFlags));
	}
	
}
