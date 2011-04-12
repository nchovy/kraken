package org.krakenapps.pcap.decoder.netbios.rr;

import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.util.Buffer;

public class NsResourceRecord extends ResourceRecord {
	private int ttl;
	private short rdLength;
	private String nsdName;

	public NsResourceRecord(String name) {
		super(name);
	}

	public int getTtl() {
		return ttl;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}

	public short getRdLength() {
		return rdLength;
	}

	public void setRdLength(short rdLength) {
		this.rdLength = rdLength;
	}

	public String getNsdName() {
		return nsdName;
	}

	public void setNsdName(String nsdName) {
		this.nsdName = nsdName;
	}

	@Override
	public void parse(Buffer b, int type) {
		this.setType(Type.parse(type));
		this.setCls(b.getShort());
		this.setTtl(b.getInt());
		this.setRdLength(b.getShort());

		// parse name
		byte[] nsdBuffer = new byte[this.getRdLength()];
		b.get();
		b.gets(nsdBuffer);
		b.get();
		this.setNsdName(NetBiosNameCodec.decodeResourceName(nsdBuffer));
	}
	@Override
	public String toString(){
		return String.format("ResoruceRecord(NsResourceRecord)\n"+
				"type = %s , cls(class) = 0x%s , ttl = 0x%s\n"+
				"Rdlength = 0x%s\n"
				, this.type , Integer.toHexString(this.cls) , Integer.toHexString(this.ttl),
				Integer.toHexString(this.rdLength));
	}
}
