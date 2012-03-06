package org.krakenapps.pcap.decoder.smb.trans2resp;

import org.krakenapps.pcap.decoder.smb.TransData;

public class Trans2CreateDirectoryResponse implements TransData{
	short eaErrorOffset;

	public short getEaErrorOffset() {
		return eaErrorOffset;
	}

	public void setEaErrorOffset(short eaErrorOffset) {
		this.eaErrorOffset = eaErrorOffset;
	}
	@Override
	public String toString(){
		return String.format("Trans2 Seconde Level : Trans2 Create Directory Response\n");
	}
}
