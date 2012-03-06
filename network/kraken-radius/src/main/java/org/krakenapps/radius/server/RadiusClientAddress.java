/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.radius.server;

import java.net.Inet4Address;
import java.net.InetAddress;

public class RadiusClientAddress {
	private InetAddress address;
	private int cidr;
	private int mask;
	private boolean isNetworkAddress;

	public RadiusClientAddress(InetAddress address) {
		this.address = address;
		this.isNetworkAddress = false;
	}

	public RadiusClientAddress(InetAddress address, int cidr) {
		if (address instanceof Inet4Address)
			throw new IllegalArgumentException("IPv4 network address only supported");

		this.address = address;
		this.cidr = cidr;

		int bit = 1;
		for (int i = 31; i >= 32 - cidr; i--) {
			this.mask |= (bit << i);
		}
	}

	public int getCidr() {
		return cidr;
	}

	public boolean match(InetAddress target) {
		if (isNetworkAddress) {
			if (!(target instanceof Inet4Address))
				throw new IllegalArgumentException("cannot match ipv6 address with ipv4 network address");

			int t = toInt((Inet4Address) target);
			int n = toInt((Inet4Address) address);
			return (t & mask) == n;
		} else {
			return target.equals(address);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + cidr;
		result = prime * result + (isNetworkAddress ? 1231 : 1237);
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
		RadiusClientAddress other = (RadiusClientAddress) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (cidr != other.cidr)
			return false;
		if (isNetworkAddress != other.isNetworkAddress)
			return false;
		return true;
	}

	public static int toInt(Inet4Address addr) {
		byte[] b = addr.getAddress();
		int l = 0;
		for (int i = 0; i < 4; i++) {
			l <<= 8;
			l |= b[i] & 0xff;
		}
		return l;
	}

	@Override
	public String toString() {
		if (isNetworkAddress)
			return address.getHostAddress() + "/" + cidr;
		return address.getHostAddress();
	}

}
