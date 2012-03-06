package org.krakenapps.pcap.decoder.smb.response;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class WriteMPXSecondaryResponse implements SmbData{

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
	//not use
	@Override
	public String toString(){
		return String.format("First Level : Wrute MPX Secondary Response\n" +
				"isMalformed = %s\n",
				this.malformed);
	}
}
