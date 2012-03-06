package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class NewFileSizeRequest implements SmbData{
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
	//// not implemented
	//  return STATUS_NOT_IMPLEMENTED
	public String toString(){
		return String.format("First Level : New File Size Request\n"+
				"isMalforemd = %s\n",
				this.malformed);
	}
}
