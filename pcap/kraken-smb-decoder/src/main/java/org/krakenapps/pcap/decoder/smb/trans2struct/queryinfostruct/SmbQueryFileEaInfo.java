package org.krakenapps.pcap.decoder.smb.trans2struct.queryinfostruct;

import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransStruct;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class SmbQueryFileEaInfo implements TransStruct{

	int eaSize;

	public int getEaSize() {
		return eaSize;
	}

	public void setEaSize(int eaSize) {
		this.eaSize = eaSize;
	}

	@Override
	public TransStruct parse(Buffer b , SmbSession session) {
		eaSize = ByteOrderConverter.swap(b.getInt());
		return this;
	}
	@Override
	public String toString(){
		return String.format("");
	}
}
