package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
//0x30
public class FindNotifyCloseRequest implements SmbData{
	private boolean malformed = false;
	@Override
	public boolean isMalformed() {
		return malformed;
	}
	@Override
	public void setMalformed(boolean malformed) {
		this.malformed = malformed;
	}
	// this code have no use
	// if receive this code , must return STATUS_NOT_IMPLEMETED 
	@Override
	public String toString(){
		return String.format("First Level : Find Notify Close Request\n"+
				"isMalformed = %s\n", this.malformed);
	}
}
