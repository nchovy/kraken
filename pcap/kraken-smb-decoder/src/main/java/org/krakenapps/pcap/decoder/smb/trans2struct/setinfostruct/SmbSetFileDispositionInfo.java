package org.krakenapps.pcap.decoder.smb.trans2struct.setinfostruct;

import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransStruct;
import org.krakenapps.pcap.util.Buffer;

public class SmbSetFileDispositionInfo implements TransStruct {

	byte deletePending;

	public byte getDeletePending() {
		return deletePending;
	}

	public void setDeletePending(byte deletePending) {
		this.deletePending = deletePending;
	}

	@Override
	public TransStruct parse(Buffer b , SmbSession session) {
		deletePending = b.get();
		return this;
	}
	@Override
	public String toString(){
		return String.format("");
	}
}
