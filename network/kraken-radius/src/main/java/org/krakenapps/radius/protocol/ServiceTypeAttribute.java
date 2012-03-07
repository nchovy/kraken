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

public class ServiceTypeAttribute extends RadiusAttribute {
	private int code;

	public enum Type {
		Login(1), Framed(2), CallbackLogin(3), CallbackFramed(4), Outbound(5), Administrative(6), NasPrompt(7), AuthenticateOnly(
				8), CallbackNasPrompt(9), CallCheck(10), CallbackAdministrative(11);

		Type(int code) {
			this.code = code;
		}

		private int code;

		public static Type parse(int code) {
			for (Type t : values())
				if (t.code == code)
					return t;

			return null;
		}
	}

	public ServiceTypeAttribute(Type t) {
		this.code = t.code;
	}

	public ServiceTypeAttribute(byte[] encoded, int offset, int length) {
		if (encoded[offset] != getType())
			throw new IllegalArgumentException("binary is not service type attribute");

		this.code = decodeInt(encoded, offset, length);
	}

	@Override
	public int getType() {
		return 6;
	}

	public Type getServiceType() {
		return Type.parse(code);
	}

	@Override
	public byte[] getBytes() {
		return encodeInt(getType(), code);
	}
}
