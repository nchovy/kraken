package org.krakenapps.pcap.decoder.smb.ntreq;

import org.krakenapps.pcap.decoder.smb.TransData;

public class NtTransactSetSecurityDescRequest implements TransData{

	private short fid;
	private short reserved;
	private int securityinformation;
	private byte []securityDescriptor;
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
	public int getSecurityinformation() {
		return securityinformation;
	}
	public void setSecurityinformation(int securityinformation) {
		this.securityinformation = securityinformation;
	}
	public byte[] getSecurityDescriptor() {
		return securityDescriptor;
	}
	public void setSecurityDescriptor(byte[] securityDescriptor) {
		this.securityDescriptor = securityDescriptor;
	}
	public String toString(){
		return String.format("Second Level : Nt Transact Set Security DescRequest\n"+
				"fid = 0x%s , reserved = 0x%s , securityinformation = 0x%s , securityDescriptor = %s\n"
				, Integer.toHexString(this.fid) , Integer.toHexString(this.reserved) , Integer.toHexString(this.securityinformation) , this.securityDescriptor.toString());
	}
}
