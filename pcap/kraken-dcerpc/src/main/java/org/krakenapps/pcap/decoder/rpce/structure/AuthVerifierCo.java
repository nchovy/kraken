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
package org.krakenapps.pcap.decoder.rpce.structure;

import org.krakenapps.pcap.decoder.rpce.rr.AuthenticationLevel;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class AuthVerifierCo {

	private byte[] authPad; // align 4byte
	private byte authType;
	private byte authLevel;
	private byte PadLength;
	private byte authReserved;
	private int authContextId;
	private AuthValue authValue; // size of authLength, maybe not use

	public AuthVerifierCo() {
		authPad = new byte[4];
	}
	public void parse(Buffer b) {
		b.gets(authPad);
		authType = b.get();
		authLevel = b.get();
		PadLength = b.get();
		authReserved = b.get();
		authContextId = ByteOrderConverter.swap(b.getInt());
		//System.out.println("authType = " + authType);
		//System.out.println("authLevel = " + authLevel);
		//System.out.println("PadLength = " + PadLength);
		//System.out.println("authReserved = " + authReserved);
		
		authValue = getAuthType(AuthenticationLevel.parse(authLevel));
		authValue.parse(b);
	}

	public AuthValue getAuthType(AuthenticationLevel level){
		switch (level) {
			case RPC_C_AUTHN_LEVEL_NONE:
				return new AuthValueLevelNone();
			case RPC_C_AUTHN_LEVEL_CONNECT:
				return new AuthValueLevelConnect();
			case RPC_C_AUTHN_LEVEL_CALL:
				return new AuthValueLevelCall();
			case RPC_C_AUTHN_LEVEL_PKT:
				return new AuthValueLevelPKT();
			case RPC_C_AUTHN_LEVEL_PKT_INTEGRITY:
				return new AuthValueLevelPKTIntegrity();
			case RPC_C_AUTHN_LEVEL_PKT_PRIVACY:
				return new AuthValueLevelPKTPrivacy();
			default:
				throw new IllegalArgumentException(level + " is invalid AuthenticationLevel");
		}
	}
	public byte[] getAuthPad() {
		return authPad;
	}

	public void setAuthPad(byte[] authPad) {
		this.authPad = authPad;
	}

	public byte getAuthType() {
		return authType;
	}

	public void setAuthType(byte authType) {
		this.authType = authType;
	}

	public byte getAuthLevel() {
		return authLevel;
	}

	public void setAuthLevel(byte authLevel) {
		this.authLevel = authLevel;
	}

	public byte getPadLength() {
		return PadLength;
	}

	public void setPadLength(byte padLength) {
		PadLength = padLength;
	}

	public byte getAuthReserved() {
		return authReserved;
	}

	public void setAuthReserved(byte authReserved) {
		this.authReserved = authReserved;
	}

	public int getAuthContextId() {
		return authContextId;
	}

	public void setAuthContextId(int authContextId) {
		this.authContextId = authContextId;
	}

	public AuthValue getAuthValue() {
		return authValue;
	}

	public void setAuthValue(AuthValue authValue) {
		this.authValue = authValue;
	}
}
