package org.krakenapps.pcap.decoder.smb.rr;

import java.util.HashMap;
import java.util.Map;

public enum FileAttributes {
	SMB_FILE_ATTRIBUTE_NORMAL(0x0000),
	SMB_FILE_ATTRIBUTE_READONLY(0x0001),
	SMB_FILE_ATTRIBUTE_HIDDEN(0x0002),
	SMB_FILE_ATTRIBUTE_SYSTEM(0x0004),
	SMB_FILE_ATTRIBUTE_VOLUME(0x0008),
	SMB_FILE_ATTRIBUTE_DIRECTORY(0x0010),
	SMB_FILE_ATTRIBUTE_ARCHIVE(0x0020),
	SMB_SEARCH_ATTRIBUTE_READONLY(0x0100),
	SMB_SEARCH_ATTRIBUTE_HIDDEN(0x0200),
	SMB_SEARCH_ATTRIBUTE_SYSTEM(0x0400),
	SMB_SEARCH_ATTRIBUTE_DIRECTORY(0x1000),
	SMB_SEARCH_ATTRIBUTE_ARCHIVE(0x2000),
	OTHER(0xC8C0);
	private static Map<Integer, FileAttributes> codeMap = new HashMap<Integer, FileAttributes>();
	static {
		for (FileAttributes code : FileAttributes.values()) {
			codeMap.put(code.getCode(), code);
		}
	}
	public int getCode() {
		return code;
	}
	public static FileAttributes parse(int code) {
		return codeMap.get(code);
	}
	
	FileAttributes(int code){
		this.code = code;
	}
	private int code;
}
