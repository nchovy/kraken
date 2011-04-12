package org.krakenapps.dnet;

import java.net.InetAddress;

import org.krakenapps.dnet.jni.NativeArpEntry;

public class ArpEntry {
	private InetAddress ip;
	private MacAddress mac;

	public ArpEntry(MacAddress mac, InetAddress ip) {
		this.ip = ip;
		this.mac = mac;
	}

	public InetAddress getIp() {
		return ip;
	}

	public void setIp(InetAddress ip) {
		this.ip = ip;
	}

	public MacAddress getMac() {
		return mac;
	}

	public void setMac(MacAddress mac) {
		this.mac = mac;
	}

	public NativeArpEntry getNativeEntry() {
		Address protocol = new Address(Address.Type.IP, 32, ip.getAddress());
		Address hardware = new Address(Address.Type.Ethernet, 48, mac.getAddr());

		return new NativeArpEntry(protocol, hardware);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + ((mac == null) ? 0 : mac.hashCode());
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
		ArpEntry other = (ArpEntry) obj;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		if (mac == null) {
			if (other.mac != null)
				return false;
		} else if (!mac.equals(other.mac))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("ip=%s, mac=%s", ip.getHostAddress(), mac);
	}

}
