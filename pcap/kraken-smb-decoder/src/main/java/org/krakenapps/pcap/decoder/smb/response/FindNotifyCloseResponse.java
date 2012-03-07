package org.krakenapps.pcap.decoder.smb.response;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
//0x30
public class FindNotifyCloseResponse implements SmbData{
	boolean malformed = false;
	// this code have no use
	// if receive this code , must return STATUS_NOT_IMPLEMETED 
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
		return String.format("First Level : Find Notify Close Response\n" +
				"isMalformed = %s\n",
				this.malformed);
	}
}
