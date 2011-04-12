package org.krakenapps.pcap.decoder.smb.trans2resp;

import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.TransStruct;

public class QueryFileInformationResponse implements TransData{

	short eaErrorOffset;
	TransStruct struct;
	// response each information level

	public short getEaErrorOffset() {
		return eaErrorOffset;
	}

	public void setEaErrorOffset(short eaErrorOffset) {
		this.eaErrorOffset = eaErrorOffset;
	}
	@Override
	public String toString(){
		return String.format("Trans2 Seconde Level : Query File Information\n" +
				"eaErrorOffset = 0x%s\n" +
				"struct = %s\n",
				Integer.toHexString(this.eaErrorOffset),
				this.struct);
	}
}
