package org.krakenapps.pcap.decoder.smb;

import java.util.HashMap;
import java.util.Map;

import org.krakenapps.pcap.decoder.smb.rr.QueryInformationLevel;
import org.krakenapps.pcap.decoder.smb.trans2struct.queryinfostruct.SmbInfoQueryAllEas;
import org.krakenapps.pcap.decoder.smb.trans2struct.queryinfostruct.SmbInfoQueryEaSize;
import org.krakenapps.pcap.decoder.smb.trans2struct.queryinfostruct.SmbInfoQueryEasFromList;
import org.krakenapps.pcap.decoder.smb.trans2struct.queryinfostruct.SmbInfoStandard;
import org.krakenapps.pcap.decoder.smb.trans2struct.queryinfostruct.SmbQueryFileAllInfo;
import org.krakenapps.pcap.decoder.smb.trans2struct.queryinfostruct.SmbQueryFileAltnameInfo;
import org.krakenapps.pcap.decoder.smb.trans2struct.queryinfostruct.SmbQueryFileBasicInfo;
import org.krakenapps.pcap.decoder.smb.trans2struct.queryinfostruct.SmbQueryFileEaInfo;
import org.krakenapps.pcap.decoder.smb.trans2struct.queryinfostruct.SmbQueryFileNameInfo;
import org.krakenapps.pcap.decoder.smb.trans2struct.queryinfostruct.SmbQueryFileStandardInfo;
import org.krakenapps.pcap.decoder.smb.trans2struct.queryinfostruct.SmbQueryFileStreamInfo;
import org.krakenapps.pcap.decoder.smb.trans2struct.queryinfostruct.SmbQueryFilecomressionInfo;

public class QueryInfoLevelMapper {

	private Map<QueryInformationLevel, TransStruct> queryInfoMap = new HashMap<QueryInformationLevel, TransStruct>();
	public QueryInfoLevelMapper() {
		queryInfoMap.put(QueryInformationLevel.SmbInfoStandard, new SmbInfoStandard());
		queryInfoMap.put(QueryInformationLevel.SmbInfoQueryEaSize , new SmbInfoQueryEaSize());
		queryInfoMap.put(QueryInformationLevel.SmbInfoQueryEasFromList , new SmbInfoQueryEasFromList());
		queryInfoMap.put(QueryInformationLevel.SmbInfoQueryAllEas , new SmbInfoQueryAllEas());
		queryInfoMap.put(QueryInformationLevel.SmbQueryFileBasicInfo , new SmbQueryFileBasicInfo());
		queryInfoMap.put(QueryInformationLevel.SmbQueryFileStandardInfo , new SmbQueryFileStandardInfo());
		queryInfoMap.put(QueryInformationLevel.SmbQueryFileEaInfo, new SmbQueryFileEaInfo());
		queryInfoMap.put(QueryInformationLevel.SmbQueryFileNameInfo, new SmbQueryFileNameInfo());
		queryInfoMap.put(QueryInformationLevel.SmbQueryFileAllInfo, new SmbQueryFileAllInfo());
		queryInfoMap.put(QueryInformationLevel.SmbQueryFileAltNameInfo, new SmbQueryFileAltnameInfo());
		queryInfoMap.put(QueryInformationLevel.SmbQueryFileStreamInfo, new SmbQueryFileStreamInfo());
		queryInfoMap.put(QueryInformationLevel.SmbQueryFileCompressionInfo , new SmbQueryFilecomressionInfo());
	}
	public TransStruct parse(QueryInformationLevel level){
		return queryInfoMap.get(level);
	}
}
