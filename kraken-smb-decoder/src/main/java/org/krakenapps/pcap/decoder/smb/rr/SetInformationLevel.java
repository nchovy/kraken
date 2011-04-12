package org.krakenapps.pcap.decoder.smb.rr;

import java.util.HashMap;
import java.util.Map;

public enum SetInformationLevel {

	SmbInfoStandard(0x0001),
	SmbInfoSetEas(0x0002),
	SmbSetFileBasicInfo(0x0101),
	SmbSetFileDspositionInfo(0x0102),
	SmbSetFileAllocationInfo(0x0103),
	SmbSetFileEndOfFileInfo(0x0104);
	private static Map<Integer , SetInformationLevel> codeMap = new HashMap<Integer , SetInformationLevel>();
	static {
		for( SetInformationLevel mode : SetInformationLevel.values()){
			codeMap.put(mode.getCode() , mode);
		}
	}
	public int getCode(){
		return levelType;
	}
	SetInformationLevel(int code){
		this.levelType = code;
	}
	public SetInformationLevel parse(int code){
		return codeMap.get(code);
	}
	private int levelType;
}
