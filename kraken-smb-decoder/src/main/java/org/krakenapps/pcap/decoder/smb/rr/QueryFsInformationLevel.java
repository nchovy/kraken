package org.krakenapps.pcap.decoder.smb.rr;

import java.util.HashMap;
import java.util.Map;

public enum QueryFsInformationLevel {
	SmbInfoAllocation(0x0001),
	SmbInfoVloume(0x0002),
	SmbQueryFsVolumeInfo(0x0102),
	SmbQueryFsSizeInfo(0x0103),
	SmbQueryFsDeviceInfo(0x0104),
	SmbQueryFsAttributeInfo(0x0105);
	private static Map<Integer , QueryFsInformationLevel> codeMap = new HashMap<Integer , QueryFsInformationLevel>();
	static {
		for( QueryFsInformationLevel mode : QueryFsInformationLevel.values()){
			codeMap.put(mode.getCode() , mode);
		}
	}
	public int getCode(){
		return levelType;
	}
	QueryFsInformationLevel(int code){
		this.levelType = code;
	}
	public QueryFsInformationLevel parse(int code){
		return codeMap.get(code);
	}
	private int levelType;
}
