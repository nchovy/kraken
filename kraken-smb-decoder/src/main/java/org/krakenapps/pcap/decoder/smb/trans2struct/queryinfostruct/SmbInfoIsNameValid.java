package org.krakenapps.pcap.decoder.smb.trans2struct.queryinfostruct;

import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransStruct;
import org.krakenapps.pcap.util.Buffer;

public class SmbInfoIsNameValid implements TransStruct{

	@Override
	public TransStruct parse(Buffer b , SmbSession session) {
		return null;
	}
	@Override
	public String toString(){
		return String.format("");
	}
	// no parameter return
}
