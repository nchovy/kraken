package org.krakenapps.pcap.decoder.smb.rr;

import java.util.HashMap;
import java.util.Map;

public enum Transaction2Command {

	TRANS2_OPEN2(0x0000),
	TRANS2_FIND_FIRST2(0x0001),
	TRANS2_FIND_NEXT2(0x0002),
	TRANS_QUERY_FS_INFORMATION(0x0003),
	TRANS2_SET_FS_INFORMATION(0x0004),
	TRANS2_QUERY_PATH_INFORMATION(0x0005),
	TRANS2_SET_PATH_INFORMATION(0x0006),
	TRANS2_QUERY_FILE_INFORMATION(0x0007),
	TRANS2_SET_FILE_INFORMATION(0x0008),
	TRANS2_FSCTL(0x0009),
	TRANS2_IOCTL2(0x000a),
	TRANS2_FIND_NOTIFY_FIRST(0x000b),
	TRANS2_FIND_NOTIFY_NEXT(0x000c),
	TRANS2_CREATE_DIRECTORY(0x000d),
	TRANS2_SESSION_SETUP(0x000e),
	TRANS2_GET_DFS_REFERRAL(0x0010),
	TRANS2_REPORT_DFS_INCONSITENCY(0x11);
private static Map<Integer, Transaction2Command> codeMap = new HashMap<Integer, Transaction2Command>();
	
	static {
		for (Transaction2Command code : Transaction2Command.values()) {
			codeMap.put(code.getCode(), code);
		}
	}
	public int getCode() {
		return code;
	}
	public static Transaction2Command parse(int code) {
		return codeMap.get(code);
	}
	Transaction2Command(int code){
		this.code = code;
	}
	private int code;
}
