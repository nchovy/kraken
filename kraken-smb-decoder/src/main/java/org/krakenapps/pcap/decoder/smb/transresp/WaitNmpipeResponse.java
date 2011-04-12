package org.krakenapps.pcap.decoder.smb.transresp;

import org.krakenapps.pcap.decoder.smb.TransData;

public class WaitNmpipeResponse implements TransData{
	/// there is no response
	@Override
	public String toString(){
		return String.format("");
	}
}
