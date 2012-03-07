package org.krakenapps.pcap.decoder.smb.response;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class NewFileSizeResponse implements SmbData{

	boolean malformed = false;
	//// not implemented
	//  return STATUS_NOT_IMPLEMENTED
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
		return String.format("First Level : New File Size Response \n" +
				"isMalformed = %s\n",
				this.malformed);
	}
}
