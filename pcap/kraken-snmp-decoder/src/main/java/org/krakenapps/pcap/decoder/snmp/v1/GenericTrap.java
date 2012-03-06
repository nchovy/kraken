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
package org.krakenapps.pcap.decoder.snmp.v1;

public enum GenericTrap {
	Unknown(-1), ColdStart(0), WarmStart(1), LinkDown(2), LinkUp(3), AuthenticationFailure(4), EgpNeighborLoss(5), EnterpriseSpecific(
			6);

	private GenericTrap(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static GenericTrap parse(int code) {
		switch (code) {
		case 0:
			return ColdStart;
		case 1:
			return WarmStart;
		case 2:
			return LinkDown;
		case 3:
			return LinkUp;
		case 4:
			return AuthenticationFailure;
		case 5:
			return EgpNeighborLoss;
		case 6:
			return EnterpriseSpecific;
		default:
			return Unknown;
		}
	}

	private int code;

}
