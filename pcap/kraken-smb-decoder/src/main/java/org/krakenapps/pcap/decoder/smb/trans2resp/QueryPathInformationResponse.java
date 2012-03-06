package org.krakenapps.pcap.decoder.smb.trans2resp;

import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.TransStruct;

public class QueryPathInformationResponse implements TransData{
	short eaErrorOffset;
	TransStruct struct;

	public TransStruct getStruct() {
		return struct;
	}

	public void setStruct(TransStruct struct) {
		this.struct = struct;
	}

	public short getEaErrorOffset() {
		return eaErrorOffset;
	}

	public void setEaErrorOffset(short eaErrorOffset) {
		this.eaErrorOffset = eaErrorOffset;
	}
	
	// data different from InformationLevel on time
	@Override
	public String toString(){
		return String.format("Trans2 Seconde Level : Query Path Information Response \n" +
				"eaErrorOffset = 0x%s\n" +
				"struct = %s\n",
				Integer.toHexString(this.eaErrorOffset),
				this.struct);
	}
}
