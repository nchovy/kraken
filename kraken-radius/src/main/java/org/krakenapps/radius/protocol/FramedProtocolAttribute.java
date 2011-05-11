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

public class FramedProtocolAttribute extends RadiusAttribute {

	public enum Protocol {
		PPP(1), SLIP(2), ARAP(3), Gandalf(4), Xylogics(5), X75(6);

		Protocol(int code) {
			this.code = code;
		}

		private int code;

		public static Protocol parse(int code) {
			for (Protocol p : values())
				if (p.code == code)
					return p;

			return null;
		}
	}

	private int code;

	public FramedProtocolAttribute(Protocol p) {
		this.code = p.code;
	}

	public FramedProtocolAttribute(byte[] encoded, int offset, int length) {
		if (encoded[offset] != getType())
			throw new IllegalArgumentException("binary is not framed protocol attribute");

		this.code = decodeInt(encoded, offset, length);
	}

	@Override
	public int getType() {
		return 7;
	}
	
	public Protocol getProtocol() {
		return Protocol.parse(code);
	}

	@Override
	public byte[] getBytes() {
		return encodeInt(getType(), code);
	}
}
