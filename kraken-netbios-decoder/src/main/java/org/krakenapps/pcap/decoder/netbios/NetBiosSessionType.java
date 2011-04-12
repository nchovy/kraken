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