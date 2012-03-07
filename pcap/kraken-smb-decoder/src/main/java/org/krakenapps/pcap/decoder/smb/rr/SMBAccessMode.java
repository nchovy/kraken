package org.krakenapps.pcap.decoder.smb.rr;

import java.util.HashMap;
import java.util.Map;

public enum SMBAccessMode {
	AccessMode(0x0007),
	Reserved1(0x0008),
	SharingMode(0x0070),
	Reserved2(0x0080),
	ReferenceLocality(0x0700),
	Reserved3(0x0800),
	CacheMode(0x1000),
	Reserved4(0x2000),
	WritethroughMode(0x4000),
	Reserved5(0x8000);
	private static Map<Integer, SMBAccessMode> codeMap = new HashMap<Integer, SMBAccessMode>();
	static {
		for (SMBAccessMode code : SMBAccessMode.values()) {
			codeMap.put(code.getCode(), code);
		}
	}
	public int getCode() {
		return code;
	}
	public static SMBAccessMode parse(int code) {
		return codeMap.get(code);
	}
	
	SMBAccessMode(int code){
		this.code = code;
	}
	private int code;
}
