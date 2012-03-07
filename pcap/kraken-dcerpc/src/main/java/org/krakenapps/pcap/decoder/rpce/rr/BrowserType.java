package org.krakenapps.pcap.decoder.rpce.rr;

import java.util.HashMap;
import java.util.Map;

public enum BrowserType {

	HostAnnouncement(0x01),
	AnnouncementRequest(0x02),
	RequestElection(0x08),
	GetBackupListRequest(0x09),
	GetBackupListResponse(0x0A),
	BecomeBackup(0x0B),
	DomainAnnouncement(0x0C),
	MasterAnnouncement(0x0D),
	ResetStateRequest(0x0E),
	LocalMasterAnnouncement(0x0F);

	BrowserType(int type) {
		this.type = type;
	}
	int type;
	
	private static Map<Integer, BrowserType> typeMap = new HashMap<Integer, BrowserType>();
	static{
		for(BrowserType type : BrowserType.values()){
			typeMap.put(type.getType(), type);
		}
	}
	public int getType(){
		return type;
	}
	public static BrowserType parse(int type){
		return typeMap.get(type);
	}
}
