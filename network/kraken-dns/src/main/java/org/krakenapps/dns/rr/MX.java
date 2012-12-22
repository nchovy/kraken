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

/**
 * {@link http://tools.ietf.org/html/rfc1035#page-17}
 * 
 * @author xeraph
 * 
 */
public class MX extends DnsResourceRecord {
	private int preference;

	/**
	 * {@link http://tools.ietf.org/html/rfc974}
	 */
	private String exchange;

	public static DnsResourceRecord decode(ByteBuffer bb, boolean isQuestion) {
		MX mx = new MX();

		if (isQuestion)
			return mx;

		// skip rdlen
		bb.getShort();

		mx.preference = bb.getShort() & 0xffff;
		mx.exchange = DnsLabelCodec.decode(bb);
		return mx;
	}

	@Override
	public void encode(ByteBuffer bb) {
		ByteBuffer rdata = ByteBuffer.allocate(65535);
		rdata.putShort((short) preference);
		DnsLabelCodec.encode(rdata, exchange);
		rdata.flip();

		bb.putShort((short) rdata.limit());
		bb.put(rdata);
	}

	public int getPreference() {
		return preference;
	}

	public void setPreference(int preference) {
		this.preference = preference;
	}

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	@Override
	public String toString() {
		return "MX " + preference + " " + exchange;
	}
}
