package org.krakenapps.pcap.decoder.smb.response;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
// 0x31
public class CloseAndTreeDiscResponse implements SmbData{
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
	@Override
	public String toString(){
		return String.format("First Level : Close And Tree Disc Response\n" +
				"isMalformed = %s\n",
				this.malformed);
	}
	
	//no use
	// return STATUS_NOT_IMPLEMETED
}
