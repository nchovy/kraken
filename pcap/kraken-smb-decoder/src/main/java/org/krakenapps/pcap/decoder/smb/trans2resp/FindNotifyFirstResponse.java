package org.krakenapps.pcap.decoder.smb.trans2resp;

import org.krakenapps.pcap.decoder.smb.TransData;

public class FindNotifyFirstResponse implements TransData{
	@Override
	public String toString(){
		return String.format("Trans2 Seconde Level : Find Notify First Response\n");
	}
}
