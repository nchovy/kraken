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

public class VendorSpecificAttribute extends RadiusAttribute {

	private int vendorId;
	private byte[] data;

	public VendorSpecificAttribute(int vendorId, byte[] data) {
		this.vendorId = vendorId;
		this.data = data;
	}

	public VendorSpecificAttribute(byte[] encoded, int offset, int length) {
		if (encoded[offset] != getType())
			throw new IllegalArgumentException("binary is not vendor specific attribute");

		ByteBuffer bb = ByteBuffer.wrap(encoded);
		bb.position(offset + 1);
		int len = bb.get();
		this.vendorId = bb.getInt();

		byte[] b = new byte[len - 6];
		bb.get(b);
		this.data = b;
	}

	@Override
	public int getType() {
		return 26;
	}

	public int getVendorId() {
		return vendorId;
	}

	public byte[] getData() {
		return data;
	}

	@Override
	public byte[] getBytes() {
		int len = 6 + data.length;
		ByteBuffer bb = ByteBuffer.allocate(len);
		bb.put((byte) getType());
		bb.put((byte) len);
		bb.putInt(vendorId);
		bb.put(data);
		return bb.array();
	}
}
