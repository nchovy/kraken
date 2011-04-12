package org.krakenapps.pcap.decoder.smb.trans2resp;

import org.krakenapps.pcap.decoder.smb.TransData;

public class SetFsInformationResponse implements TransData{
//not implement err
	@Override
	public String toString(){
		return String.format("Trans2 Seconde Level : Set Fs Information Response\n");
	}
}
