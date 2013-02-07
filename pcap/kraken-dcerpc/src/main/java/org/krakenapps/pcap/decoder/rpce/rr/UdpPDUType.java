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
package org.krakenapps.pcap.decoder.rpce.rr;

import java.util.HashMap;
import java.util.Map;

public enum UdpPDUType {

	REQUEST(0),
	PING(1),
	RESPONSE(2),
	FAULT(3),
	WORKING(4),
	NOCALL(5),
	REJECT(6),
	ACK(7),
	CL_CANCEL(8),
	FACK(9),
	CANCEL_ACK(10);
	UdpPDUType(int type){
		this.type = type;
	}
	private int type;
	static Map<Integer, UdpPDUType> typeMap = new HashMap<Integer, UdpPDUType>();
	static{
		for(UdpPDUType type : UdpPDUType.values()){
			typeMap.put(type.getType(), type);
		}
	}
	public int getType(){
		return type;
	}
	public static UdpPDUType parse(int type){
		return typeMap.get(type);
	}
}
