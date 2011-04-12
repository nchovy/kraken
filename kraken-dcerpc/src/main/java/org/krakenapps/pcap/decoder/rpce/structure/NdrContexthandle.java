package org.krakenapps.pcap.decoder.rpce.structure;

import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class NdrContexthandle {

	private int contextHandleAttribues;
	private Uuid contextHandleUuid;
	public void parse(Buffer b){
		contextHandleAttribues = ByteOrderConverter.swap(b.getInt());
		contextHandleUuid.parse(b);
	}
	public int getContextHandleAttribues() {
		return contextHandleAttribues;
	}
	public void setContextHandleAttribues(int contextHandleAttribues) {
		this.contextHandleAttribues = contextHandleAttribues;
	}
	public Uuid getContextHandleUuid() {
		return contextHandleUuid;
	}
	public void setContextHandleUuid(Uuid contextHandleUuid) {
		this.contextHandleUuid = contextHandleUuid;
	}
	
}
