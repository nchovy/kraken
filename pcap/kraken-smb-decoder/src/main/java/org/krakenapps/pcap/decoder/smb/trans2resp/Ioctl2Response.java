package org.krakenapps.pcap.decoder.smb.trans2resp;

import org.krakenapps.pcap.decoder.smb.TransData;

public class Ioctl2Response implements TransData{
	@Override
	public String toString(){
		return String.format("Trans2 Seconde Level : Ioctl2 Response\n");
	}
}
