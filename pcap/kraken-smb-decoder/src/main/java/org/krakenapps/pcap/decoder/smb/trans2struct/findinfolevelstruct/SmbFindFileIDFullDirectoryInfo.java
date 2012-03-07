package org.krakenapps.pcap.decoder.smb.trans2struct.findinfolevelstruct;

import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransStruct;
import org.krakenapps.pcap.util.Buffer;

public class SmbFindFileIDFullDirectoryInfo implements TransStruct{

	@Override
	public TransStruct parse(Buffer b, SmbSession session) {
		return null;
	}
	@Override
	public String toString(){
		return String.format("Third Level Structure : Smb Find File Id Full Directory Info\n" +
				"this structure not implement");
	}
}
