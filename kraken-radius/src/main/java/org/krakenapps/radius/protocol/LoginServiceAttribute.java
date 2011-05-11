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

public class LoginServiceAttribute extends RadiusAttribute {
	public enum LoginService {
		Telnet(0), Rlogin(1), TcpClear(2), PortMaster(3), Lat(4), X25Pad(5), X25T3Pos(6), TcpClearQuiet(8);

		LoginService(int code) {
			this.code = code;
		}

		private int code;

		public static LoginService parse(int code) {
			for (LoginService p : values())
				if (p.code == code)
					return p;

			return null;
		}
	}

	private int code;
	
	public LoginServiceAttribute(LoginService service) {
		this.code = service.code;
	}

	public LoginServiceAttribute(byte[] encoded, int offset, int length) {
		if (encoded[offset] != getType())
			throw new IllegalArgumentException("binary is not login service attribute");

		this.code = decodeInt(encoded, offset, length);
	}

	@Override
	public int getType() {
		return 15;
	}

	public LoginService getLoginService() {
		return LoginService.parse(code);
	}

	@Override
	public byte[] getBytes() {
		return encodeInt(getType(), code);
	}
}
