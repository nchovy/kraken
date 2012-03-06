package org.krakenapps.pcap.decoder.smb.rr;

import java.util.HashMap;
import java.util.Map;

public enum FindInformationLevel {
		SmbInfoStandard(0x0001),
		SmbInfoQueryEaSize(0x0002),
		SmbInfoQueryEasFromList(0x0003),
		SmbFindFileDirectoryInfo(0x0101),
		SmbFindFileFullDirectoryInfo(0x0102),
		SmbFindFileNamesInfo(0x0103),
		SmbFindFileBothDirectoryInfo(0x0104),
		SmbFindFileIDFullDirectoryInfo(0x0105),
		SmbFindFileIDBothDirectoryInfo(0x0106);
		private static Map<Integer , FindInformationLevel> codeMap = new HashMap<Integer , FindInformationLevel>();
		static {
			for( FindInformationLevel mode : FindInformationLevel.values()){
				codeMap.put(mode.getCode() , mode);
			}
		}
		public int getCode(){
			return levelType;
		}
		FindInformationLevel(int code){
			this.levelType = code;
		}
		public static FindInformationLevel parse(int code){
			return codeMap.get(code);
		}
		private int levelType;
}
