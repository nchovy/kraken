package org.krakenapps.dns.rr;

import java.nio.ByteBuffer;

import org.krakenapps.dns.DnsLabelCodec;
import org.krakenapps.dns.DnsResourceRecord;

public class NS extends DnsResourceRecord {

	private String nsDomain;

	public static DnsResourceRecord decode(ByteBuffer bb, boolean isQuestion) {
		NS ns = new NS();

		if (isQuestion)
			return ns;

		// skip rdlen
		bb.getShort();

		ns.nsDomain = DnsLabelCodec.decode(bb);
		return ns;
	}

	public String getNsDomain() {
		return nsDomain;
	}

	@Override
	public void encode(ByteBuffer bb) {
		ByteBuffer rdata = ByteBuffer.allocate(nsDomain.length() * 3);
		DnsLabelCodec.encode(rdata, nsDomain);
		rdata.flip();

		bb.putShort((short) rdata.limit());
		bb.put(rdata);
	}

	@Override
	public String toString() {
		return "NS " + nsDomain;
	}
}
