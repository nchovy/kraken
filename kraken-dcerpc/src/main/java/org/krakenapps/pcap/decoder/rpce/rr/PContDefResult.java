package org.krakenapps.pcap.decoder.rpce.rr;

import java.util.HashMap;
import java.util.Map;

public enum PContDefResult {

	ACCEPTANCE(0x00),
	USER_REJECTION(0x01),
	PROVIDER_REJECTION(0x02),
	NEGOTIATE_ACK(0x03);
	PContDefResult(int code) {
		this.code = code;
	}
	static Map<Integer, PContDefResult> codeMap = new HashMap<Integer, PContDefResult>();
	static{
		for(PContDefResult code : PContDefResult.values()){
			codeMap.put(code.getCode(), code);
		}
	}
	public int getCode(){
		return code;
	}
	public static PContDefResult parse(int code){
		return codeMap.get(code);
	}
	private int code;
}
