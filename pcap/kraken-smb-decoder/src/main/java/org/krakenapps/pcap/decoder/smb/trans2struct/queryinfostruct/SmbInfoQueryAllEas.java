package org.krakenapps.pcap.decoder.smb.trans2struct.queryinfostruct;

import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransStruct;
import org.krakenapps.pcap.decoder.smb.structure.SmbFeaList;
import org.krakenapps.pcap.util.Buffer;

public class SmbInfoQueryAllEas implements TransStruct{

	SmbFeaList extendedAttributesList;

	@Override
	public TransStruct parse(Buffer b , SmbSession session) {
		extendedAttributesList = new SmbFeaList();
		extendedAttributesList.parse(b);
		return this;
	}
	@Override
	public String toString(){
		return String.format("Third Level Structure : Smb Info Query All Eas\n" +
				"extendedAttributesList = %s\n",
				this.extendedAttributesList);
	}
}
