package org.krakenapps.dns.rr;

import java.nio.ByteBuffer;

import org.krakenapps.dns.DnsLabelCodec;
import org.krakenapps.dns.DnsResourceRecord;

public class PTR extends DnsResourceRecord {
	private String domain;

	public static DnsResourceRecord decode(ByteBuffer bb, boolean isQuestion) {
		PTR ptr = new PTR();

		if (isQuestion)
			return ptr;

		// skip rdlen
		bb.getShort();

		ptr.domain = DnsLabelCodec.decode(bb);
		return ptr;
	}

	@Override
	public void encode(ByteBuffer bb) {
		ByteBuffer rdata = ByteBuffer.allocate(domain.length() * 3);
		DnsLabelCodec.encode(rdata, domain);
		rdata.flip();

		bb.putShort((short) rdata.limit());
		bb.put(rdata);
	}

	@Override
	public String toString() {
		if (domain != null)
			return "PTR " + domain;
		return "PTR " + name;
	}
}
