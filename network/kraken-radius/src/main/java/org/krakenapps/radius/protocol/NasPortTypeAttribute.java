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

public class NasPortTypeAttribute extends RadiusAttribute {

	public enum Type {
		Async(0), 
		Sync(1), 
		IsdnSync(2), 
		IsdnAsyncV120(3), 
		IsdnAsyncV110(4), 
		Virtual(5), 
		PIAFS(6), 
		HdlcClearChannel(7), 
		X25(8), 
		X75(9), 
		G3Fax(10), 
		Sdsl(11), 
		AdslCap(12), 
		AdslDmt(13), 
		Idsl(14), 
		Ethernet(15), 
		Xdsl(16), 
		Cable(17), 
		WirelessOther(18), 
		Wireless80211(19);

		Type(int code) {
			this.code = code;
		}

		private int code;

		public int getCode() {
			return code;
		}

		public static Type parse(int code) {
			for (Type t : values())
				if (t.code == code)
					return t;

			return null;
		}
	}

	private int code;

	public NasPortTypeAttribute(Type type) {
		this.code = type.code;
	}

	public NasPortTypeAttribute(byte[] encoded, int offset, int length) {
		if (encoded[offset] != getType())
			throw new IllegalArgumentException("binary is not nas port type attribute");

		this.code = decodeInt(encoded, offset, length);
	}
	
	@Override
	public int getType() {
		return 61;
	}

	public Type getPortType() {
		return Type.parse(code);
	}

	@Override
	public byte[] getBytes() {
		return encodeInt(getType(), code);
	}
}
