package org.krakenapps.pcap.decoder.smb.rr;

import java.util.HashMap;
import java.util.Map;

public enum QueryInformationLevel {
	SmbInfoStandard(0x0001),
	SmbInfoQueryEaSize(0x0002),
	SmbInfoQueryEasFromList(0x0003),
	SmbInfoQueryAllEas(0x0004),
	SmbInfoIsNameValid(0x0006),
	SmbQueryFileBasicInfo(0x0101),
	SmbQueryFileStandardInfo(0x0102),
	SmbQueryFileEaInfo(0x0103),
	SmbQueryFileNameInfo(0x0104),
	SmbQueryFileAllInfo(0x0107),
	SmbQueryFileAltNameInfo(0x0108),
	SmbQueryFileStreamInfo(0x0109),
	SmbQueryFileCompressionInfo(0x010b);
	private static Map<Integer , QueryInformationLevel> codeMap = new HashMap<Integer , QueryInformationLevel>();
	static {
		for( QueryInformationLevel mode : QueryInformationLevel.values()){
			codeMap.put(mode.getCode() , mode);
		}
	}
	public int getCode(){
		return levelType;
	}
	QueryInformationLevel(int code){
		this.levelType = code;
	}
	public QueryInformationLevel parse(int code){
		return codeMap.get(code);
	}
	private int levelType;
}
