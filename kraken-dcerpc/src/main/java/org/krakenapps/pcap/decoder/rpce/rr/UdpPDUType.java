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
