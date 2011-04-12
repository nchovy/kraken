package org.krakenapps.pcap.decoder.rpce.structure;

import org.krakenapps.pcap.util.Buffer;

public class AuthValueLevelConnect implements AuthValue {

	private byte subType;

	public byte getSubType() {
		return subType;
	}

	public void setSubType(byte subType) {
		this.subType = subType;
	}

	@Override
	public void parse(Buffer b) {
		subType = b.get();
	}

}
