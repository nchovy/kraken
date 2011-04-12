package org.krakenapps.pcap.decoder.smb.transresp;

import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.rr.NamedPipeStatus;

public class QueryNmpipeStateResponse implements TransData{
	
	NamedPipeStatus status;

	public NamedPipeStatus getStatus() {
		return status;
	}

	public void setStatus(NamedPipeStatus status) {
		this.status = status;
	}
	@Override
	public String toString(){
		return String.format("");
	}
}
