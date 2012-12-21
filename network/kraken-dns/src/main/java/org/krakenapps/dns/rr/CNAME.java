package org.krakenapps.dns.rr;

import java.nio.ByteBuffer;

import org.krakenapps.dns.DnsLabelCodec;
import org.krakenapps.dns.DnsResourceRecord;

public class CNAME extends DnsResourceRecord {
	private String canonicalName;

	public static DnsResourceRecord decode(ByteBuffer bb, boolean isQuestion) {
		CNAME cname = new CNAME();

		if (isQuestion)
			return cname;

		// skip rdlen
		bb.getShort();

		cname.canonicalName = DnsLabelCodec.decode(bb);
		return cname;
	}

	@Override
	public void encode(ByteBuffer bb) {
		ByteBuffer rdata = ByteBuffer.allocate(canonicalName.length() * 3);
		DnsLabelCodec.encode(rdata, canonicalName);
		rdata.flip();

		bb.putShort((short) rdata.limit());
		bb.put(rdata);
	}

	public String getCanonicalName() {
		return canonicalName;
	}

	public void setCanonicalName(String canonicalName) {
		this.canonicalName = canonicalName;
	}

	@Override
	public String toString() {
		return "CNAME " + canonicalName;
	}
}
