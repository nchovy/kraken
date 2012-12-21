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
