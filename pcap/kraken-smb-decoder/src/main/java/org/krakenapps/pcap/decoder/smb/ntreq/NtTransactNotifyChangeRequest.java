package org.krakenapps.pcap.decoder.smb.ntreq;

import org.krakenapps.pcap.decoder.smb.TransData;

public class NtTransactNotifyChangeRequest implements TransData{
	//setup
	int completionFiler;
	short fid;
	byte watchTree;
	byte reserved;
	//no param,data
	public int getCompletionFiler() {
		return completionFiler;
	}
	public void setCompletionFiler(int completionFiler) {
		this.completionFiler = completionFiler;
	}
	public short getFid() {
		return fid;
	}
	public void setFid(short fid) {
		this.fid = fid;
	}
	public byte getWatchTree() {
		return watchTree;
	}
	public void setWatchTree(byte watchTree) {
		this.watchTree = watchTree;
	}
	public byte getReserved() {
		return reserved;
	}
	public void setReserved(byte reserved) {
		this.reserved = reserved;
	}
	public String toString(){
		return String.format("Second Level : Nt Transact Notify Change Request\n"+
				"completionFilter = 0x%s , fid = 0x%s , watchTree = 0x%s , reserved = 0x%s\n",
				Integer.toHexString(this.completionFiler), Integer.toHexString(this.fid) , Integer.toHexString(this.watchTree) , Integer.toHexString(this.reserved));
	}
}
