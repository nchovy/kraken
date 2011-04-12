package org.krakenapps.pcap.decoder.smb.response;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
// 0x7E
public class SecurityPackageANDXResponse implements SmbData{

	boolean malformed = false;
	//retrun STATUS NOT IMPLEMENTED
	@Override
	public boolean isMalformed() {
		// TODO Auto-generated method stub
		return malformed;
	}
	@Override
	public void setMalformed(boolean malformed) {
		this.malformed = malformed;
	}
	@Override
	public String toString(){
		return String.format("First Level : Security package andx Response\n" +
				"isMalformed = %s\n",
				this.malformed);
	}
}
