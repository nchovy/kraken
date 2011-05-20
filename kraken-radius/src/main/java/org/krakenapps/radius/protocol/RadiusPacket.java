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

import java.net.UnknownHostException;
import java.nio.BufferUnderflowException;
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
	protected boolean finalized = false;

	public static byte[] newRequestAuthenticator() {
		UUID uuid = UUID.randomUUID();
		ByteBuffer bb = ByteBuffer.allocate(16);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		return bb.array();
	}

	public static RadiusPacket parse(String sharedSecret, byte[] buf) {
		return parse(sharedSecret, buf, 0, buf.length);
	}

	public static RadiusPacket parse(String sharedSecret, byte[] buf, int offset, int length) {
		ByteBuffer bb = ByteBuffer.wrap(buf);
		bb.position(offset);
		int code = bb.get();

		RadiusPacket p = null;
		switch (code) {
		case 1:
			p = new AccessRequest();
			break;
		case 2:
			p = new AccessAccept();
			break;
		case 3:
			p = new AccessReject();
			break;
		case 4:
			p = new AccountingRequest();
			break;
		case 5:
			p = new AccountingResponse();
			break;
		case 11:
			p = new AccessChallenge();
			break;
		}

		p.setIdentifier(bb.get());
		p.setLength(bb.getShort() & 0xffff);

		if (p.getLength() > length)
			throw new BufferUnderflowException();

		byte[] authenticator = new byte[16];
		bb.get(authenticator);
		p.setAuthenticator(authenticator);

		int attrLength = p.getLength() - 20;
		for (int i = 0; i < attrLength;) {
			bb.mark();
			bb.get();
			int len = bb.get();
			bb.reset();

			try {
				RadiusAttribute attr = RadiusAttribute.parse(authenticator, sharedSecret, bb.array(), bb.position(),
						bb.remaining());
				if (attr != null)
					p.getAttributes().add(attr);
			} catch (UnknownHostException e) {
			}

			i += len;
			bb.position(bb.position() + len);
		}

		return p;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		guard();
		this.code = code;
	}

	public int getIdentifier() {
		return identifier;
	}

	public void setIdentifier(int identifier) {
		guard();
		this.identifier = identifier;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		guard();
		this.length = length;
	}

	public byte[] getAuthenticator() {
		return authenticator;
	}

	public void setAuthenticator(byte[] authenticator) {
		guard();
		this.authenticator = authenticator;
	}

	public List<RadiusAttribute> getAttributes() {
		return attrs;
	}

	public void setAttributes(List<RadiusAttribute> attrs) {
		guard();
		this.attrs = attrs;
	}

	public RadiusAttribute findAttribute(int type) {
		for (RadiusAttribute attr : attrs)
			if (attr.getType() == type)
				return attr;
		return null;
	}

	public byte[] getBytes() {
		if (!finalized)
			throw new IllegalStateException("invoke finalize() before byte serialization");

		ByteBuffer bb = ByteBuffer.allocate(length);
		bb.put((byte) code);
		bb.put((byte) identifier);
		bb.putShort((short) length);
		bb.put(getAuthenticator());

		for (RadiusAttribute attr : attrs)
			bb.put(attr.getBytes());

		return bb.array();
	}

	private void guard() {
		if (finalized)
			throw new IllegalStateException("already finalized packet");
	}

	public boolean isFinalized() {
		return finalized;
	}

	public void finalize() {
		if (finalized)
			return;
		
		int len = 20;

		List<RadiusAttribute> attrs = getAttributes();
		for (RadiusAttribute attr : attrs)
			len += attr.getBytes().length;

		this.length = len;
		finalized = true;
	}
}
