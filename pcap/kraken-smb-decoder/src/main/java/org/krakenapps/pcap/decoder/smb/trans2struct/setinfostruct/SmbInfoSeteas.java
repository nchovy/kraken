package org.krakenapps.pcap.decoder.smb.trans2struct.setinfostruct;

import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransStruct;
import org.krakenapps.pcap.decoder.smb.structure.SmbFeaList;
import org.krakenapps.pcap.util.Buffer;

public class SmbInfoSeteas implements TransStruct {

	SmbFeaList extendedAttributeList;

	public SmbFeaList getExtendedAttributeList() {
		return extendedAttributeList;
	}

	public void setExtendedAttributeList(SmbFeaList extendedAttributeList) {
		this.extendedAttributeList = extendedAttributeList;
	}

	@Override
	public TransStruct parse(Buffer b , SmbSession session) {
		extendedAttributeList = new SmbFeaList();
		extendedAttributeList.parse(b);
		return this;
	}
	@Override
	public String toString(){
		return String.format("");
	}
}
