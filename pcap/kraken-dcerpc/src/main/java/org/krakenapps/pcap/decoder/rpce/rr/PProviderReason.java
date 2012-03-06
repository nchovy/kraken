package org.krakenapps.pcap.decoder.rpce.rr;

import java.util.HashMap;
import java.util.Map;

public enum PProviderReason {

	REASON_NOT_SPECIFIED(0x00),
	ABSTRACT_SYNTAX_NOT_SUPPORTED(0x01),
	PROPOSED_TRANSFER_SYNTAXES_NOT_SUPPORTED(0x02),
	LOCAL_LIMIT_EXCEEDED(0x03);
	private PProviderReason(int code) {
		this.code = code;
	}
	static Map<Integer, PProviderReason> codeMap = new HashMap<Integer, PProviderReason>();
	static{
		for(PProviderReason code : PProviderReason.values()){
			codeMap.put(code.getCode(), code);
		}
	}
	public int getCode(){
		return code;
	}
	public static PProviderReason parse(int code){
		return codeMap.get(code);
	}
	private int code;
}
