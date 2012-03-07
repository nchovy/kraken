package org.krakenapps.pcap.decoder.smb;

import java.util.HashMap;
import java.util.Map;

import org.krakenapps.pcap.decoder.smb.rr.QueryFsInformationLevel;
import org.krakenapps.pcap.decoder.smb.trans2struct.queryfsstruct.SmbInfoVolume;
import org.krakenapps.pcap.decoder.smb.trans2struct.queryfsstruct.SmbQueryFsAttributeInfo;
import org.krakenapps.pcap.decoder.smb.trans2struct.queryfsstruct.SmbQueryFsDeviceInfo;
import org.krakenapps.pcap.decoder.smb.trans2struct.queryfsstruct.SmbQueryFsSizeInfo;
import org.krakenapps.pcap.decoder.smb.trans2struct.queryfsstruct.SmbQueryFsVolumeInfo;
import org.krakenapps.pcap.decoder.smb.trans2struct.queryfsstruct.SmbinfoAllocation;

public class QueryFsInfoLevelMapper {

	private Map<QueryFsInformationLevel, TransStruct> queryFsMapper = new HashMap<QueryFsInformationLevel, TransStruct>();
	
	public QueryFsInfoLevelMapper(){
		queryFsMapper.put(QueryFsInformationLevel.SmbInfoAllocation , new SmbinfoAllocation());
		queryFsMapper.put(QueryFsInformationLevel.SmbInfoVloume , new SmbInfoVolume());
		queryFsMapper.put(QueryFsInformationLevel.SmbQueryFsAttributeInfo , new SmbQueryFsAttributeInfo());
		queryFsMapper.put(QueryFsInformationLevel.SmbQueryFsDeviceInfo , new SmbQueryFsDeviceInfo());
		queryFsMapper.put(QueryFsInformationLevel.SmbQueryFsSizeInfo , new SmbQueryFsSizeInfo());
		queryFsMapper.put(QueryFsInformationLevel.SmbQueryFsVolumeInfo , new SmbQueryFsVolumeInfo());
	}
	
	public TransStruct getStruct(QueryFsInformationLevel level){
		return queryFsMapper.get(level);
	}
}
