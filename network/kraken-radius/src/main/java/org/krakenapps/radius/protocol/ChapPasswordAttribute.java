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

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class ChapPasswordAttribute extends RadiusAttribute {
	private byte chapIdent;
	private byte[] response;

	public ChapPasswordAttribute(byte chapIdent, byte[] response) {
		if (response.length != 16)
			throw new IllegalArgumentException("CHAP response length should be 16");
		
		this.chapIdent = chapIdent;
		this.response = response;
	}

	public ChapPasswordAttribute(byte[] encoded, int offset, int length) {
		ByteBuffer bb = ByteBuffer.wrap(encoded);
		bb.position(offset);

		int type = bb.get();
		if (type != getType())
			throw new IllegalArgumentException("binary is not chap password attribute");

		int len = bb.get();
		if (len > bb.remaining())
			throw new BufferUnderflowException();

		this.chapIdent = bb.get();

		byte[] response = new byte[16];
		bb.get(response);
		this.response = response;
	}

	@Override
	public int getType() {
		return 3;
	}

	public byte getChapIdent() {
		return chapIdent;
	}

	public byte[] getChapResponse() {
		return response;
	}

	@Override
	public byte[] getBytes() {
		ByteBuffer bb = ByteBuffer.allocate(19);
		bb.put((byte) getType());
		bb.put((byte) 19);
		bb.put(chapIdent);
		bb.put(response);
		return bb.array();
	}
}
