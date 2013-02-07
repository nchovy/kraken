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

public enum NetBiosSessionType {
	SessionMessage(0x00), 
	SessionRequest(0x81), 
	PositiveSessionResponse(0x82), 
	NegativeSessionResponse(0x83), 
	RetargetSessionResponse(0x84), 
	SessionKeepAlive(0x85),
	//user define type Session Reassembled
	SessionReassembled(0x11);
	
	NetBiosSessionType(int type) {
		this.type = type;
	}

	public int getValue() {
		return type;
	}
	
	public static boolean isSessionMessage(NetBiosSessionPacket p) {
		return p.getHeader().getType() == NetBiosSessionType.SessionMessage;
	}

	public static NetBiosSessionType parse(int type) {
		for (NetBiosSessionType t : values())
			if (t.getValue() == type){
				return t;
			}
		return SessionReassembled;
	}
	private int type;
}