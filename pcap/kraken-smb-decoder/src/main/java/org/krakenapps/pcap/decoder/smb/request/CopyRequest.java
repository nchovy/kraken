package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class CopyRequest implements SmbData{
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
	// no longer used
	// should return Status NOt Implemented
	@Override
	public String toString(){
		return String.format("First Level : copy Request\n"+
				"isMalFormed = %s\n", this.malformed);
	}
}
