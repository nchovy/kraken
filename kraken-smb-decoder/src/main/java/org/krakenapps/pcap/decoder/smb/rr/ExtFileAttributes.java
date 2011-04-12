package org.krakenapps.pcap.decoder.smb.rr;

import java.util.HashMap;
import java.util.Map;

public enum ExtFileAttributes {
	ATTR_DREADONLY(0x00000001),
	ATTR_HIDDEN(0x00000002),
	ATTR_SYSTEM(0x00000004),
	ATTR_DIRECTORY(0x00000010),
	ATTR_ARCHIVE(0x00000020),
	ATTR_NORMAL(0x00000080),
	ATTR_TMPORARY(0x00000100),
	ATTR_COMPRESSED(0x00000800),
	POSIX_SEMANTICS(0x1000000),
	BACKUP_SEMMANTICS(0x02000000),
	DELETE_ON_CLOSE(0x04000000),
	SEQUENTIAL_SCAN(0x08000000),
	RANDOM_ACCESS(0x10000000),
	NO_BUFFERING(0x20000000),
	WRITE_THROUGH(0x80000000);
	private static Map<Integer, ExtFileAttributes> codeMap = new HashMap<Integer, ExtFileAttributes>();
	
	static {
		for (ExtFileAttributes code : ExtFileAttributes.values()) {
			codeMap.put(code.getCode(), code);
		}
	}
	public int getCode() {
		return code;
	}
	public static ExtFileAttributes parse(int code) {
		return codeMap.get(code);
	}
	
	ExtFileAttributes(int code){
		this.code = code;
	}
	private int code;

}
