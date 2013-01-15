/*
 * Copyright 2012 Future Systems
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
package org.krakenapps.dns;

import java.nio.ByteBuffer;

public abstract class DnsResourceRecord {

	/**
	 * TYPE values
	 */
	public enum Type {
		A(1), NS(2), MD(3), MF(4), CNAME(5), SOA(6), MB(7), MG(8), MR(9), NULL(10), WKS(11), PTR(12), HINFO(13), MINFO(14), MX(15), TXT(
				16);

		private int code;

		Type(int code) {
			this.code = code;
		}

		public static Type parse(int code) {
			for (Type c : values())
				if (c.code == code)
					return c;
			return null;
		}
		
		public int getCode() {
			return code;
		}
	}

	public enum Clazz {
		IN(1), CS(2), CH(3), HS(4);

		private int code;

		Clazz(int code) {
			this.code = code;
		}

		public static Clazz parse(int code) {
			for (Clazz c : values())
				if (c.code == code)
					return c;
			return null;
		}
	}

	/**
	 * an owner name, i.e., the name of the node to which this resource record
	 * pertains.
	 */
	protected String name;

	/**
	 * label offset for message compression. 0 for not-used
	 */
	protected int nameOffset;

	/**
	 * two octets containing one of the RR TYPE codes.
	 */
	protected int type;

	/**
	 * two octets containing one of the RR CLASS codes.
	 */
	protected int clazz;

	protected int ttl;

	/**
	 * RDLENGTH. record data length.
	 */
	protected int length;

	/**
	 * RDATA.
	 */
	protected byte[] data;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getNameOffset() {
		return nameOffset;
	}

	public void setNameOffset(int nameOffset) {
		this.nameOffset = nameOffset;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getClazz() {
		return clazz;
	}

	public void setClazz(int clazz) {
		this.clazz = clazz;
	}

	public int getTtl() {
		return ttl;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public abstract void encode(ByteBuffer bb);
}
