package org.krakenapps.dns.rr;

import java.nio.ByteBuffer;

import org.krakenapps.dns.DnsLabelCodec;
import org.krakenapps.dns.DnsResourceRecord;

public class SRV extends DnsResourceRecord {
	private int priority;

	private int weight;

	private int port;

	private String target;

	public static DnsResourceRecord decode(ByteBuffer bb, boolean isQuestion) {
		SRV srv = new SRV();
		if (isQuestion)
			return srv;

		// skip rdlen
		bb.getShort();

		srv.priority = bb.getShort() & 0xffff;
		srv.weight = bb.getShort() & 0xffff;
		srv.port = bb.getShort() & 0xffff;
		srv.target = DnsLabelCodec.decode(bb);
		return srv;
	}

	@Override
	public void encode(ByteBuffer bb) {
		ByteBuffer rdata = ByteBuffer.allocate(65535);
		rdata.putShort((short) priority);
		rdata.putShort((short) weight);
		rdata.putShort((short) port);
		DnsLabelCodec.encode(rdata, target);
		rdata.flip();

		bb.putShort((short) rdata.limit());
		bb.put(rdata);
	}

	@Override
	public String toString() {
		return "SRV " + priority + " " + weight + " " + port + " " + target;
	}
}
