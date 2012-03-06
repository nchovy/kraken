package org.krakenapps.pcap.decoder.smb.rr;

import java.util.HashMap;
import java.util.Map;

public enum NtTransactCommand {
	NT_TRANSACT_CREATE(0x0001),
	NT_TRANSACT_IOCTL(0x0002),
	NT_TRANSACT_SET_SECURITY_DESC(0x0003),
	NT_TRANSACT_NOTIFY_CHANGE(0x0004),
	NT_TRANSACT_RENAME(0x0005),
	NT_TRANSACT_QUERY_SECURITY_DESC(0x0006),
	// upper CIFS
	// under SMB
	NT_TRANSACT_QUERY_QUOTA(0x0007),
	NT_TRANSACT_SET_QUOTA(0x0008),
	NT_TRANSACT_CREATE2(0x0009);
	private static Map<Integer, NtTransactCommand> codeMap = new HashMap<Integer, NtTransactCommand>();
	
	static {
		for (NtTransactCommand code : NtTransactCommand.values()) {
			codeMap.put(code.getCode(), code);
		}
	}
	public int getCode() {
		return code;
	}
	public static NtTransactCommand parse(int code) {
		return codeMap.get(code);
	}
	NtTransactCommand(int code)	{
		this.code = code;
	}
	private int code;
}
