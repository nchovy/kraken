/*
 * Copyright 2011 Future Systems, Inc
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
package org.krakenapps.pcap.decoder.netbios;

public enum NetBiosDatagramType {
	DirectUniqueDatagram(0x10),
	DirectGroupDatagram(0x11),
	BroadcastDatagram(0x12),
	DatagramError(0x13),
	DatagramQueryRequest(0x14),
	DatagramPositiveQueryResponse(0x15),
	DatagramNegativeQueryResponse(0x16);
	
	NetBiosDatagramType(int type){
		this.type = type;
	}
	
	public static NetBiosDatagramType parse(int type){
		switch(type){
		case 0x10:
			return DirectUniqueDatagram;
		case 0x11:
			return DirectGroupDatagram;
		case 0x12:
			return BroadcastDatagram;
		case 0x13:
			return DatagramError;
		case 0x14:
			return DatagramQueryRequest;
		case 0x15:
			return DatagramPositiveQueryResponse;
		case 0x16:
			return DatagramNegativeQueryResponse;
		default :
			throw new IllegalStateException("invalid type");
		}
	}
	
	public int getType() {
		return type;
	}
	
	private int type;
}
