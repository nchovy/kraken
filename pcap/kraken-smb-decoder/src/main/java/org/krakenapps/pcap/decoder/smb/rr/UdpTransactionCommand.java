package org.krakenapps.pcap.decoder.smb.rr;

import java.util.HashMap;
import java.util.Map;

public enum UdpTransactionCommand {

	TRANS_MAILSLOT_WRITE(0x0001);
	UdpTransactionCommand(int code){
		this.code = code;
	}
	private static Map<Integer, UdpTransactionCommand> codeMap = new HashMap<Integer, UdpTransactionCommand>();
	
	static {
		for (UdpTransactionCommand code : UdpTransactionCommand.values()) {
			codeMap.put(code.getCode(), code);
		}
	}
	public int getCode() {
		return code;
	}
	public static UdpTransactionCommand parse(int code) {
		return codeMap.get(code);
	}
	private int code;
}
