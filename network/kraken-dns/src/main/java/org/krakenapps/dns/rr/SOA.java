/*
 * Copyright 2012 Future Systems
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.dns.rr;

import java.nio.ByteBuffer;

import org.krakenapps.dns.DnsLabelCodec;
import org.krakenapps.dns.DnsResourceRecord;

public class SOA extends DnsResourceRecord {
	/**
	 * The <domain-name> of the name server that was the original or primary
	 * source of data for this zone.
	 */
	private String nameServer;

	/**
	 * A <domain-name> which specifies the mailbox of the person responsible for
	 * this zone.
	 */
	private String email;

	/**
	 * The unsigned 32 bit version number of the original copy of the zone. Zone
	 * transfers preserve this value. This value wraps and should be compared
	 * using sequence space arithmetic.
	 */
	private long serial;

	/**
	 * A 32 bit time interval before the zone should be refreshed.
	 */
	private int refresh;

	/**
	 * A 32 bit time interval that should elapse before a failed refresh should
	 * be retried.
	 */
	private int retry;

	/**
	 * A 32 bit time value that specifies the upper limit on the time interval
	 * that can elapse before the zone is no longer authoritative.
	 */
	private int expire;

	/**
	 * The unsigned 32 bit minimum TTL field that should be exported with any RR
	 * from this zone.
	 */
	private long minimum;

	public static DnsResourceRecord decode(ByteBuffer bb, boolean isQuestion) {
		SOA soa = new SOA();
		if (isQuestion)
			return soa;

		// skip rdlen
		bb.getShort();

		soa.nameServer = DnsLabelCodec.decode(bb);
		soa.email = DnsLabelCodec.decode(bb);
		soa.serial = bb.getInt() & 0xffffffff;
		soa.refresh = bb.getInt();
		soa.retry = bb.getInt();
		soa.expire = bb.getInt();
		soa.minimum = bb.getInt() & 0xffffffff;

		return soa;
	}

	@Override
	public void encode(ByteBuffer bb) {
		ByteBuffer rdata = ByteBuffer.allocate(65535);
		DnsLabelCodec.encode(rdata, nameServer);
		DnsLabelCodec.encode(rdata, email);
		rdata.putInt((int) serial);
		rdata.putInt(refresh);
		rdata.putInt(retry);
		rdata.putInt(expire);
		rdata.putInt((int) minimum);
		rdata.flip();

		bb.putShort((short) rdata.limit());
		bb.put(rdata);
	}

	@Override
	public String toString() {
		return "SOA " + nameServer + " " + email + " " + serial + " " + refresh + " " + retry + " " + expire + " " + minimum;
	}

}
