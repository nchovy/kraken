package org.krakenapps.pcap.decoder.smb.ntreq;

public class NtTransactSetQuotaRequest {

	//parameter
	short fid;
	//trans data 
	// quotaInformation specifiedMS-FSCC 2.4.33
	byte [] quotaInformation;
	public short getFid() {
		return fid;
	}
	public void setFid(short fid) {
		this.fid = fid;
	}
	public byte[] getQuotaInformation() {
		return quotaInformation;
	}
	public void setQuotaInformation(byte[] quotaInformation) {
		this.quotaInformation = quotaInformation;
	}
	public String toString(){
		return String.format("Second Level : Nt Transact Query Security DescRequest\n"+
				"fid = 0x%s\n",
				Integer.toHexString(this.fid));
	}
}
