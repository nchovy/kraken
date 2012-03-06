package org.krakenapps.pcap.decoder.smb.response;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
//0x26
public class TransactionSecondaryResponse implements SmbData{

	boolean malformed = false;
	//there is no response 
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
		return String.format("First Level : Transaction Secondary Response\n" +
				"isMalformed = %s\n",
				this.malformed);
	}
}
