package org.krakenapps.pcap.decoder.smb.ntreq;

public class NtTransactQueryQuotaRequest {

	//parameters
	// it specified in MS-DTYP 2.4.2.
	
	// nt transparameter
	short fid;
	boolean returnSingleEntry;
	boolean restartScan;
	int sidListlength;
	int startSidLength;
	int startsidoffset;
	// nt trans data
	byte []sidList; // variable;
	
	public short getFid() {
		return fid;
	}
	public void setFid(short fid) {
		this.fid = fid;
	}
	public boolean isReturnSingleEntry() {
		return returnSingleEntry;
	}
	public void setReturnSingleEntry(boolean returnSingleEntry) {
		this.returnSingleEntry = returnSingleEntry;
	}
	public boolean isRestartScan() {
		return restartScan;
	}
	public void setRestartScan(boolean restartScan) {
		this.restartScan = restartScan;
	}
	public int getSidListlength() {
		return sidListlength;
	}
	public void setSidListlength(int sidListlength) {
		this.sidListlength = sidListlength;
	}
	public int getStartSidLength() {
		return startSidLength;
	}
	public void setStartSidLength(int startSidLength) {
		this.startSidLength = startSidLength;
	}
	public int getStartsidoffset() {
		return startsidoffset;
	}
	public void setStartsidoffset(int startsidoffset) {
		this.startsidoffset = startsidoffset;
	}
	public byte[] getSidList() {
		return sidList;
	}
	public void setSidList(byte[] sidList) {
		this.sidList = sidList;
	}
	public String toString(){
		return String.format("Second Level : Nt Transact Query Quota Request\n"+
				"fid = 0x%s , returnSingleEntry = %s  , restartScan = %s  , sidListLength = 0x%s\n"+
				"startSidLength = 0x%s , startsidoffset = 0x%s\n",
				Integer.toHexString(this.fid) , this.returnSingleEntry ,this.restartScan , Integer.toHexString(this.sidListlength),
				Integer.toHexString(this.startSidLength) , Integer.toHexString(this.startsidoffset));
	}
}
