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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.krakenapps.dns.DnsResourceRecord;

/**
 * DNS Extensions to Support IP Version 6 {@link http
 * ://tools.ietf.org/html/rfc3596}
 * 
 * @author xeraph
 * 
 */
public class AAAA extends DnsResourceRecord {
	private InetAddress address;

	public static DnsResourceRecord decode(ByteBuffer bb, boolean isQuestion) {
		AAAA aaaa = new AAAA();
		if (isQuestion)
			return aaaa;

		int rdlen = bb.getShort();
		if (rdlen != 16)
			throw new IllegalStateException("type AAAA record's rdlen (" + rdlen + ") should be 16 (128bit)");

		byte[] b = new byte[16];
		bb.get(b);
		try {
			aaaa.address = InetAddress.getByAddress(b);
		} catch (UnknownHostException e) {
		}

		return aaaa;
	}

	@Override
	public void encode(ByteBuffer bb) {
		bb.putShort((short) 16);
		bb.put(address.getAddress());
	}

	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	@Override
	public String toString() {
		if (address == null)
			return "AAAA " + name;
		else
			return "AAAA " + address.getHostAddress();
	}
}
