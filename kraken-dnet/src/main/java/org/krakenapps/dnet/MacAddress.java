package org.krakenapps.dnet;

import java.util.Arrays;

public class MacAddress {
	private final byte[] addr;

	public MacAddress(byte[] addr) {
		this.addr = addr;
	}

	public byte[] getAddr() {
		return addr;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(addr);
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
		MacAddress other = (MacAddress) obj;
		if (!Arrays.equals(addr, other.addr))
			return false;
		return true;
	}

	@Override
	public String toString() {
		byte[] b = addr;
		return String.format("%02x:%02x:%02x:%02x:%02x:%02x", b[0], b[1], b[2], b[3], b[4], b[5]);
	}
}
