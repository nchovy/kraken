package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class IOCTLSecondaryRequest implements SmbData{
	private boolean malformed = false;
	@Override
	public boolean isMalformed() {
		// TODO Auto-generated method stub
		return malformed;
	}
	@Override
	public void setMalformed(boolean malformed) {
		this.malformed = malformed;
	}
	
	// reserved;
	// return Status NOT Implemented
	@Override
	public String toString(){
		return String.format("First Level : IOCTL secondary Request\n"+
				"isMalformed = %s\n",
				this.malformed);
	}
}
