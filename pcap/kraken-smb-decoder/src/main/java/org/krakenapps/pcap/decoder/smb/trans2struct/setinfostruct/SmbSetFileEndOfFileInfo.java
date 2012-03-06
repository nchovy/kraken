package org.krakenapps.pcap.decoder.smb.trans2struct.setinfostruct;

import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransStruct;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class SmbSetFileEndOfFileInfo implements TransStruct {
	long endOfFile;

	public long getEndOfFile() {
		return endOfFile;
	}

	public void setEndOfFile(long endOfFile) {
		this.endOfFile = endOfFile;
	}

	@Override
	public TransStruct parse(Buffer b , SmbSession session) {
		endOfFile = ByteOrderConverter.swap(b.getLong());
		return this;
	}
	@Override
	public String toString(){
		return String.format("");
	}
}
