package org.krakenapps.pcap.decoder.smb.rr;

import java.util.HashMap;
import java.util.Map;

public enum NamedPipeStatus {
	ICount(0x00FF),
	ReadModeByte(0x0300),
	ReadModeMessage(0x0300),
	NamedPipeTypeByte(0x0c00),
	NamedPipeTypeMessage(0x0c00),
	EndPointClientSide(0x4000),
	EndPointServerSide(0x4000),
	Nonblocking1(0x8000),
	Nonblocking2(0x8000);
	private static Map<Integer, NamedPipeStatus> codeMap = new HashMap<Integer, NamedPipeStatus>();
	
	static {
		for (NamedPipeStatus code : NamedPipeStatus.values()) {
			codeMap.put(code.getCode(), code);
		}
	}
	public int getCode() {
		return code;
	}
	public static NamedPipeStatus parse(int code) {
		return codeMap.get(code);
	}
	
	NamedPipeStatus(int code)
	{
		this.code = code;
	}
	private int code;
}
