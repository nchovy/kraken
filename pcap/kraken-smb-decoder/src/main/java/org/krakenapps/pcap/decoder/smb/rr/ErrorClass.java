package org.krakenapps.pcap.decoder.smb.rr;

import java.util.HashMap;
import java.util.Map;


public enum ErrorClass {
	SUCCESS(0x00),
	ERRDOS(0x01),
	ERRSRV(0x02),
	ERRHRD(0x03),
	ERRCMD(0xff);
	private static Map<Integer, ErrorClass> codeMap = new HashMap<Integer, ErrorClass>();
	static {
		for (ErrorClass code : ErrorClass.values()) {
			codeMap.put(code.getCode(), code);
		}
	}
	public int getCode() {
		return code;
	}
	public static ErrorClass parse(int code) {
		return codeMap.get(code);
	}
	
	ErrorClass(int code){
		this.code = code;
	}
	private int code;
}
