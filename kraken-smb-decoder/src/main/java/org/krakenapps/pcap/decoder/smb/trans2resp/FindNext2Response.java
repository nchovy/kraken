package org.krakenapps.pcap.decoder.smb.trans2resp;

import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.TransStruct;

public class FindNext2Response implements TransData{
	
	short searchCount;
	short endOfSearch;
	short eaErrorOffset;
	short lastNameOffset;
	TransStruct []infoStruct;
	
	
	public TransStruct[] getInfoStruct() {
		return infoStruct;
	}
	public void setInfoStruct(TransStruct[] infoStruct) {
		this.infoStruct = infoStruct;
	}
	public short getSearchCount() {
		return searchCount;
	}
	public void setSearchCount(short searchCount) {
		this.searchCount = searchCount;
	}
	public short getEndOfSearch() {
		return endOfSearch;
	}
	public void setEndOfSearch(short endOfSearch) {
		this.endOfSearch = endOfSearch;
	}
	public short getEaErrorOffset() {
		return eaErrorOffset;
	}
	public void setEaErrorOffset(short eaErrorOffset) {
		this.eaErrorOffset = eaErrorOffset;
	}
	public short getLastNameOffset() {
		return lastNameOffset;
	}
	public void setLastNameOffset(short lastNameOffset) {
		this.lastNameOffset = lastNameOffset;
	}
	@Override
	public String toString(){
		return String.format("Trans2 Seconde Level : Find Next2 Response\n" +
				"searchCount = 0x%s , endOfSearch = 0x%s , eaErrorOffset = 0x%s\n" +
				"lastNameOffset = 0x%s\n" +
				"infoStruct = %s\n",
				Integer.toHexString(this.searchCount), Integer.toHexString(this.endOfSearch) , Integer.toHexString(this.eaErrorOffset),
				Integer.toHexString(this.lastNameOffset),
				this.infoStruct);
	}
}
