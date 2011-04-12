package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
// 0x31
public class CloseAndTreeDiscRequest implements SmbData{
	boolean malformed;
	//no use
	// return STATUS_NOT_IMPLEMETED
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
		return String.format("First Level : Clone and Tree Disc Request\n" +
				"isMalformed = %s ", this.malformed);
	}
}
