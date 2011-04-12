/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.iptables;

import java.net.InetAddress;

public class NetworkAddress {
	private InetAddress address;
	private int rawAddress;
	private int mask;
	private int len;
	private boolean inverted;

	public NetworkAddress(String cidr) {
		this(cidr, false);
	}

	public NetworkAddress(String cidr, boolean inverted) {
		this.inverted = inverted;

		try {
			int d = cidr.lastIndexOf('/');

			String addr = cidr;
			if (d > 0)
				addr = cidr.substring(0, d);

			address = InetAddress.getByName(addr);
			rawAddress = convert(address);

			if (d > 0)
				len = Integer.valueOf(cidr.substring(d + 1));
			else
				len = 32;

			int bit = 1 << 31;
			for (int i = 0; i < len; i++) {
				mask |= bit;
				bit >>= 1;
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + (inverted ? 1231 : 1237);
		result = prime * result + mask;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NetworkAddress other = (NetworkAddress) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (inverted != other.inverted)
			return false;
		if (mask != other.mask)
			return false;
		return true;
	}

	public InetAddress getNetworkAddress() {
		return address;
	}

	public int getPrefixLength() {
		return len;
	}

	public boolean isInverted() {
		return inverted;
	}

	public boolean match(InetAddress ip) {
		int addr = convert(ip);
		return (addr & mask) == rawAddress;
	}

	private int convert(InetAddress ip) {
		byte[] b = ip.getAddress();
		if (b.length != 4)
			throw new IllegalArgumentException("only ipv4 address is supported");

		int l = 0;
		for (int i = 0; i < 4; i++) {
			l <<= 8;
			l |= b[i] & 0xff;
		}

		return l;
	}

	@Override
	public String toString() {
		if (len == 32)
			return address.getHostAddress();

		return address.getHostAddress() + "/" + len;
	}
}
