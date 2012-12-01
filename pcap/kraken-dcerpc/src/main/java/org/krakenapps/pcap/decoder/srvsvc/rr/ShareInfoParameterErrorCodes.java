/**
 * 
 */
package org.krakenapps.pcap.decoder.srvsvc.rr;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tgnice@nchovy.com
 *
 */
public enum ShareInfoParameterErrorCodes {

	;
	private int code;
	ShareInfoParameterErrorCodes(int code) {
		this.code = code;
	}
	static Map<Integer, ShareInfoParameterErrorCodes> codeMap = new HashMap<Integer, ShareInfoParameterErrorCodes>();
	static{
		for(ShareInfoParameterErrorCodes code : ShareInfoParameterErrorCodes.values()){
			codeMap.put(code.getCode(), code);
		}
	}
	public int getCode(){
		return code;
	}
	public static ShareInfoParameterErrorCodes parse(int code){
		return codeMap.get(code);
	}
}
