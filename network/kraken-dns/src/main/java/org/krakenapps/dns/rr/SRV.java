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
