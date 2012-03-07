package org.krakenapps.pcap.decoder.smb.trans2resp;

import org.krakenapps.pcap.decoder.smb.TransData;

public class SetFileInformationResponse implements TransData{
	short eaErrorOffset;

	public short getEaErrorOffset() {
		return eaErrorOffset;
	}

	public void setEaErrorOffset(short eaErrorOffset) {
		this.eaErrorOffset = eaErrorOffset;
	}
	@Override
	public String toString(){
		return String.format("Trans2 Seconde Level : Set File Information Response\n" +
				"eaErrorOffset = %s\n",
				Integer.toHexString(this.eaErrorOffset));
	}
}
