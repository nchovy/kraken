package org.krakenapps.pcap.decoder.smb.ntreq;

import org.krakenapps.pcap.decoder.smb.TransData;

public class NtTransactQuerySecurityDescRequest implements TransData{
	//no setup
	//param
	private short fid;
	private short reserved;
	private int securityInforFields;
	//no data
	public short getFid() {
		return fid;
	}
	public void setFid(short fid) {
		this.fid = fid;
	}
	public short getReserved() {
		return reserved;
	}
	public void setReserved(short reserved) {
		this.reserved = reserved;
	}
	public int getSecurityInforFields() {
		return securityInforFields;
	}
	public void setSecurityInforFields(int securityInforFields) {
		this.securityInforFields = securityInforFields;
	}
	public String toString(){
		return String.format("Second Level : Nt Transact Query Security DescRequest\n"+
				"fid = 0x%s , reserved = 0x%s , securityInforFields = 0x%s\n",
				Integer.toHexString(this.fid) , Integer.toHexString(this.reserved) , Integer.toHexString(this.securityInforFields));
	}
}
