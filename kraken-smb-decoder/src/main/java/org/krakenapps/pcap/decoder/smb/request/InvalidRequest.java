package org.krakenapps.pcap.decoder.smb.request;

import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class InvalidRequest implements SmbData{
	private boolean malformed = false;
	@Override
	public boolean isMalformed() {
		// TODO Auto-generated method stub
		return malformed;
	}
	@Override
	public void setMalformed(boolean malformed) {
		this.malformed = malformed;
	}//not implements
	@Override
	public String toString(){
		return String.format("First Level : Invalid Request\n"+
				"isMalformed = %s\n",
				this.malformed);
	}
}
