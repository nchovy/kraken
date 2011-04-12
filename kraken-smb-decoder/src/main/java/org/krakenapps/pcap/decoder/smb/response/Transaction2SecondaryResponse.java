package org.krakenapps.pcap.decoder.smb.response;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
//0x33
public class Transaction2SecondaryResponse implements SmbData{

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
	// there is no response , no error code;
	@Override
	public String toString(){
		return String.format("First Level : Transaction 2 Secondary Response\n" +
				"isMalformed = %s\n",
				this.malformed);
	}
}
