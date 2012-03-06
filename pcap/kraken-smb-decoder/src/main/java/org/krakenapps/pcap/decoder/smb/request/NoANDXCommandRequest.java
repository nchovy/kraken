package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
//0xFF
public class NoANDXCommandRequest implements SmbData{

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
	//return STATUS_SMB_BAD_COMMAD;
	public String toString(){
		return String.format("First Level : No Andx Command Request\n"+
				"isMalforemd = %s\n",
				this.malformed);
	}
}
