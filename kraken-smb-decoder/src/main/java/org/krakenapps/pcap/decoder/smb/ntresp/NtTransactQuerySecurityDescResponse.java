package org.krakenapps.pcap.decoder.smb.ntresp;

import org.krakenapps.pcap.decoder.smb.TransData;

public class NtTransactQuerySecurityDescResponse implements TransData{

	//param
	int lengthNeeded;
	//data
	byte []securityDescriptor;
	public int getLengthNeeded() {
		return lengthNeeded;
	}
	public void setLengthNeeded(int lengthNeeded) {
		this.lengthNeeded = lengthNeeded;
	}
	public byte[] getSecurityDescriptor() {
		return securityDescriptor;
	}
	public void setSecurityDescriptor(byte[] securityDescriptor) {
		this.securityDescriptor = securityDescriptor;
	}
	@Override
	public String toString(){
		return String.format("Second Level : Nt Transact Query Security Desc Response\n"+
				"lengthNeeded = 0x%s , securityDescriptor = %s\n",
				Integer.toHexString(this.lengthNeeded) , this.securityDescriptor.toString());
	}
}
