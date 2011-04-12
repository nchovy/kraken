package org.krakenapps.pcap.decoder.rpce.rr;

import java.util.HashMap;
import java.util.Map;

public enum TcpPDUType {

	REQUEST(00),
	RESPONSE(02),
	FAULT(03),
	BIND(11),
	BIND_ACK(12),
	BIND_NACK(13),
	ALTER_CONTEXT(14),
	ALTER_CONTEXT_RESP(15),
	SHUTDOWN(17),
	CO_CANCAL(18),
	ORPHANED(19);
	TcpPDUType(int type){
		this.type = type;
	}
	private int type;
	static Map<Integer, TcpPDUType> typeMap = new HashMap<Integer, TcpPDUType>();
	static{
		for(TcpPDUType type : TcpPDUType.values()){
			typeMap.put(type.getType(), type);
		}
	}
	public int getType(){
		return type;
	}
	public static TcpPDUType parse(int type){
		return typeMap.get(type);
	}
}
