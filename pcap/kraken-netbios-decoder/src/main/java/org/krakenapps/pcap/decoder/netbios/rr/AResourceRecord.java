/*
 * Copyright 2011 Future Systems, Inc
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
package org.krakenapps.pcap.decoder.netbios.rr;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.krakenapps.pcap.util.Buffer;

public class AResourceRecord extends ResourceRecord {
	private int ttl;
	private short rdLength;
	private InetAddress[] addresses;

	public AResourceRecord(String name) {
		super(name);
	}

	public int getTtl() {
		return ttl;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}

	public short getRdLength() {
		return rdLength;
	}

	public void setRdLength(short rdLength) {
		this.rdLength = rdLength;
	}

	public InetAddress[] getRData() {
		return addresses;
	}

	public void setIpAddresses(InetAddress[] addresses) {
		this.addresses = addresses;
	}

	@Override
	public void parse(Buffer b, int type) {
		this.setType(Type.parse(type));
		this.setCls(b.getShort());
		this.setTtl(b.getInt());
		this.setRdLength(b.getShort());
		this.setIpAddresses(parseRdata(b));
	}

	private InetAddress[] parseRdata(Buffer b) {
		int length = this.getRdLength();
		int count = 0;
		count = length / 4;

		InetAddress[] addresses = new InetAddress[count];
		byte[] ipBuffer = new byte[4];
		for (int i = 0; i < count; i++) {
			b.gets(ipBuffer);
			try {
				addresses[i] = InetAddress.getByAddress(ipBuffer);
			} catch (UnknownHostException e) {
			}
		}
		return addresses;
	}

}
