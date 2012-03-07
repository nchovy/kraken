package org.krakenapps.pcap.decoder.netbios.rr;

import org.krakenapps.pcap.util.Buffer;

public class QuestionResourceRecord extends ResourceRecord {

	public QuestionResourceRecord(String name) {
		super(name);
	}

	public QuestionResourceRecord(String name, byte domainType) {
		super(name);
		this.setDomainType(domainType);
	}

	
	@Override
	public void parse(Buffer b, int types) {
		this.setType(Type.parse(b.getShort()));
		this.setCls(b.getShort());
	}

	@Override
	public String toString() {
		return String.format("ResrouceRecord(QuetionResourceRecord)\n"
				+ "type = %s , class(cls) = 0x%x , name = %s , domainType = 0x%s\n", this.type,
				Integer.toHexString(this.cls), this.name, Integer.toHexString(this.domainType));
	}
}
