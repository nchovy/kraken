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

public class QuestionResourceRecord extends ResourceRecord {

	public QuestionResourceRecord(String name) {
		super(name);
	}

	public QuestionResourceRecord(String name, byte domainType) {
		super(name);
		this.setDomainType(domainType);
	}

	
	@Override
	public void parse(Buffer b, int types) {
		this.setType(Type.parse(b.getShort()));
		this.setCls(b.getShort());
	}

	@Override
	public String toString() {
		return String.format("ResrouceRecord(QuetionResourceRecord)\n"
				+ "type = %s , class(cls) = 0x%x , name = %s , domainType = 0x%s\n", this.type,
				Integer.toHexString(this.cls), this.name, Integer.toHexString(this.domainType));
	}
}
