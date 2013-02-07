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
import java.util.ArrayList;
import java.util.List;

import org.krakenapps.pcap.util.Buffer;

public class NbResourceRecord extends ResourceRecord {
	// NB_Flags definition
	public class NbAddress {
		final static short G = (short) 0x8000;
		final static short ONT = (short) 0x6000;
		private int flag; // 'NB' only
		private InetAddress addresses;

		public NbAddress() {
		}

		public int getFlag() {
			return flag;
		}

		public void setFlag(int flag) {
			this.flag = flag;
		}

		public InetAddress getAddresses() {
			return addresses;
		}

		public void setAddresses(InetAddress addresses) {
			this.addresses = addresses;
		}

		public boolean isUniqueName() {
			return ((flag & G) >> 15) == 0;
			// 0 = RR_NAME is unique name
			// 1 = RR_NAME is group name
		}

		public boolean isBnode() {
			return ((flag & ONT) >> 13) == 0;
			// 00 = B node
			// 01 = P node
			// 10 = M node
			// 11 = Reservedfor guture use
		}

		public boolean isMnode() {
			return ((flag & ONT) >> 13) == 1;
			// 00 = B node
			// 01 = P node
			// 10 = M node
			// 11 = Reservedfor guture use
		}

		public boolean isPnode() {
			return ((flag & ONT) >> 13) == 2;
			// 00 = B node
			// 01 = P node
			// 10 = M node
			// 11 = Reservedfor guture use
		}
	}

	private short represent;
	private int ttl;
	private short rdLength;
	private List<NbAddress> addressList;

	public List<NbAddress> getAddressList() {
		return addressList;
	}

	public void setAddressList(List<NbAddress> addressList) {
		this.addressList = addressList;
	}

	public NbResourceRecord(String name) {
		super(name);
	}

	public short getRepresent() {
		return represent;
	}

	public void setRepresent(short represent) {
		this.represent = represent;
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

	public void setRData(Buffer b) throws UnknownHostException {

		int length = this.getRdLength();
		int count = 0;
		count = length / 6;
		if (count == 0) {
			return; // flag only , no datas, this use header's op,r code
		}
		
		addressList = new ArrayList<NbResourceRecord.NbAddress>();
		for (int i = 0; i < count; i++) {
			byte[] ipBuffer = new byte[4];
			NbAddress address = new NbAddress();
		//	address.setFlag(b.getUnsignedShort());
			address.setFlag(b.getShort() & 0xffff);
			b.gets(ipBuffer);
			address.setAddresses(InetAddress.getByAddress(ipBuffer));
			addressList.add(address);
		}
	}

	@Override
	public void parse(Buffer b, int type) {
		this.setType(Type.parse(type));
		this.setCls(b.getShort());
		this.setTtl(b.getInt());
		this.setRdLength(b.getShort());
		try {
			this.setRData(b);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return String.format("ReousrceRecord(NbResrouceRecord)\n" +
				"Type = %s , cls(class) = 0x%s , ttl = 0x%s\n" +
				"rdLength = 0x%s\n"+"%s"+"<"+">",
				this.type, Integer.toHexString(this.cls), Integer.toHexString(this.ttl),
				Integer.toHexString(this.rdLength) , this.name , this.domainType);
	}
}
