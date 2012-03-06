package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
// 0x7E
public class SecurityPackageANDXRequest implements SmbData{

	boolean malformed = false;
	@Override
	public boolean isMalformed() {
		// TODO Auto-generated method stub
		return malformed;
	}
	@Override
	public void setMalformed(boolean malformed) {
		this.malformed = malformed;
	}
	//retrun STATUS NOT IMPLEMENTED
	@Override
	public String toString(){
		return String.format("First Level : Security package Andx Request\n" +
				"isMalformed = %s\n",
				this.malformed);
	}
}
