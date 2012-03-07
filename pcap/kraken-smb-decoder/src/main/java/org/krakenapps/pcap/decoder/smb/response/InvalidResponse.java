package org.krakenapps.pcap.decoder.smb.response;

import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class InvalidResponse implements SmbData{
// not implements
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
		return String.format("First Level : Invalid Response\n" +
				"isMalformed = %s\n",
				this.malformed);
	}
}
