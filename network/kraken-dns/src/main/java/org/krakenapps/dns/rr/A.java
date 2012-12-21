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

public class A extends DnsResourceRecord {
	private InetAddress address;

	public A() {
	}

	public A(String name) {
		this.name = name;
		this.type = 1;
		this.clazz = 1;
	}

	public A(String name, InetAddress address, int ttl) {
		this(name);
		this.address = address;
		this.ttl = ttl;
	}

	public static DnsResourceRecord decode(ByteBuffer bb, boolean isQuestion) {
		A a = new A();

		if (isQuestion)
			return a;

		int rdlen = bb.getShort();
		if (rdlen != 4)
			throw new IllegalStateException("type A record's rdlen (" + rdlen + ") should be 4");

		byte[] b = new byte[4];
		bb.get(b);
		try {
			a.address = InetAddress.getByAddress(b);
		} catch (UnknownHostException e) {
		}

		return a;
	}

	@Override
	public void encode(ByteBuffer bb) {
		bb.putShort((short) 4);
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
			return "A " + name;
		else
			return "A " + address.getHostAddress();
	}

}
