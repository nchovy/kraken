package org.krakenapps.pcap.decoder.smb.trans2struct.setinfostruct;

import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransStruct;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class SmbSetFileAllocationInfo implements TransStruct{

	long allocationSize;

	public long getAllocationSize() {
		return allocationSize;
	}

	public void setAllocationSize(long allocationSize) {
		this.allocationSize = allocationSize;
	}

	@Override
	public TransStruct parse(Buffer b , SmbSession session) {
		allocationSize = ByteOrderConverter.swap(b.getLong()); 
		return this;
	}
	@Override
	public String toString(){
		return String.format("");
	}
}
