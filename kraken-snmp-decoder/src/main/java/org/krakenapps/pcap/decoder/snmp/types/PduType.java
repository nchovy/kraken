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
package org.krakenapps.pcap.decoder.snmp.types;

public enum PduType {
	Unknown(-1), GetRequest(0), GetNextRequest(1), GetResponse(2), SetRequest(3), Trap(4), GetBulkRequest(5), InformRequest(
			6), Trap2(7), Report(8);

	private PduType(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	private int code;

	public static PduType parse(int type) {
		type -= 0xA0;
		switch (type) {
		case 0:
			return GetRequest;
		case 1:
			return GetNextRequest;
		case 2:
			return GetResponse;
		case 3:
			return SetRequest;
		case 4:
			return Trap;
		case 5:
			return GetBulkRequest;
		case 6:
			return InformRequest;
		case 7:
			return Trap2;
		case 8:
			return Report;
		default:
			return Unknown;
		}
	}
}
