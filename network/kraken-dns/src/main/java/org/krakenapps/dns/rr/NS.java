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
