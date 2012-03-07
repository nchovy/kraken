package org.krakenapps.pcap.decoder.smb.trans2resp;

import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.TransStruct;

public class QueryFsInformationResponse implements TransData{
	TransStruct struct;

	public TransStruct getStruct() {
		return struct;
	}

	public void setStruct(TransStruct struct) {
		this.struct = struct;
	}
	
	// each Attribute Type need structure
	@Override
	public String toString(){
		return String.format("Trans2 Seconde Level : Query Fs Information Response\n" +
				"struct = %s\n",
				this.struct);
	}
}
