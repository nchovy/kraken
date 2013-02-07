/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.rpc;

public enum RpcTrustLevel {
	Untrusted(1), Low(2), Medium(3), High(4);

	RpcTrustLevel(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static RpcTrustLevel parse(int code) {
		switch (code) {
		case 2:
			return Low;
		case 3:
			return Medium;
		case 4:
			return High;
		default:
			return Untrusted;
		}
	}

	private int code;
}
