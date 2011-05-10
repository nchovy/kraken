/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.radius.protocol;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class RadiusPacket {
	private int code; // 1byte
	private int identifier; // 1byte
	private int length; // 2byte
	private byte[] authenticator; // 16byte
	private List<RadiusAttribute> attrs = new ArrayList<RadiusAttribute>();

	public static byte[] newRequestAuthenticator() {
		UUID uuid = UUID.randomUUID();
		ByteBuffer bb = ByteBuffer.allocate(16);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		return bb.array();
	}
	
	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public int getIdentifier() {
		return identifier;
	}

	public void setIdentifier(int identifier) {
		this.identifier = identifier;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public byte[] getAuthenticator() {
		return authenticator;
	}

	public void setAuthenticator(byte[] authenticator) {
		this.authenticator = authenticator;
	}

	public List<RadiusAttribute> getAttributes() {
		return attrs;
	}

	public void setAttributes(List<RadiusAttribute> attrs) {
		this.attrs = attrs;
	}
	
	public byte[] getBytes() {
		int len = 20;
		
		List<RadiusAttribute> attrs = getAttributes();
		for (RadiusAttribute attr : attrs)
			len += attr.getBytes().length;
		
		ByteBuffer bb = ByteBuffer.allocate(len);
		bb.put((byte) code);
		bb.put((byte) identifier);
		bb.putShort((short) len);
		bb.put(getAuthenticator());
		
		for (RadiusAttribute attr : attrs)
			bb.put(attr.getBytes());
		
		return bb.array();
	}
}
