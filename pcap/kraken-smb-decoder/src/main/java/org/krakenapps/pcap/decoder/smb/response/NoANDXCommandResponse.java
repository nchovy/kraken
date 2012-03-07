package org.krakenapps.pcap.decoder.smb.response;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
//0xFF
public class NoANDXCommandResponse implements SmbData{

	boolean malformed = false;
	//return STATUS_SMB_BAD_COMMAD;
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
		return String.format("First Level : No Andx Command Response\n" +
				"isMalformed = %s\n",
				this.malformed);
	}
}
