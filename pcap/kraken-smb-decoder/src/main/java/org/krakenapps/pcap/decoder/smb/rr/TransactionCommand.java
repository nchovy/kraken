package org.krakenapps.pcap.decoder.smb.rr;

import java.util.HashMap;
import java.util.Map;

public enum TransactionCommand {

	//TRANS_MAILSLOT_WRITE(0x0001),
	TRANS_SET_NMPIPE_STATE(0x0001),
	TRANS_RAW_READ_NMPIPE(0x0011),
	TRANS_QUERY_NMPIPE_INFO(0x0021),
	TRANS_PEEK_NMPIPE(0x0022),
	TRANS_TRANSACT_NMPIPE(0x0023),
	TRANS_RAW_WRITE_NMPIPE(0x0026),
	TRANS_READ_WRITE_NMPIPE(0x0031),
	TRANS_READ_NMPIPE(0x0036),
	TRANS_WRITE_NMPIPE(0x0037),
	TRANS_WAIT_NMPIPE(0x0053),
	TRANS_CALL_NMPIPE(0x0054);
	TransactionCommand(int code){
		this.code = code;
	}
	private static Map<Integer, TransactionCommand> codeMap = new HashMap<Integer, TransactionCommand>();
	
	static {
		for (TransactionCommand code : TransactionCommand.values()) {
			codeMap.put(code.getCode(), code);
		}
	}
	public int getCode() {
		return code;
	}
	public static TransactionCommand parse(int code) {
		return codeMap.get(code);
	}
	private int code;
}
