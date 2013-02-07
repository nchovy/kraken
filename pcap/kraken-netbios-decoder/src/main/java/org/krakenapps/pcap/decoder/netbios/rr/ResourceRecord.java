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

public abstract class ResourceRecord {
	public enum Type {
		A(1), NS(2), NULL(0xA), NB(0x20), NBSTAT(0x21);
		
		Type(int value) {
			this.value = value;
		}
		
		private int value;
		
		public int getValue() { return value; }
		
		public static Type parse(int value) {
			for (Type t : values()) 
				if (t.getValue() == value)
					return t;
			throw new IllegalArgumentException("invalid rr type: " + value);
		}
	};

	protected Type type;
	
	// 2byte
	protected int cls;

	protected String name;

	protected byte domainType; // example WORKGROUP "<0x20>"<----this
	
	public byte getDomainType() {
		return domainType;
	}

	public void setDomainType(byte domainType) {
		this.domainType = domainType;
	}

	public ResourceRecord(String name) {
		this.name = name;
	}

	public final String getName() {
		return name;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public int getCls() {
		return cls;
	}

	public void setCls(int cls) {
		this.cls = cls;
	}

	abstract public void parse(Buffer b, int type);

}