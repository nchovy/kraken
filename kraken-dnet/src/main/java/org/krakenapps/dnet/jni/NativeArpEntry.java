package org.krakenapps.dnet.jni;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.krakenapps.dnet.Address;
import org.krakenapps.dnet.ArpEntry;
import org.krakenapps.dnet.MacAddress;

public class NativeArpEntry {
	static {
		System.loadLibrary("kdnet");
	}

	private Address protocol;
	private Address hardware;

	public NativeArpEntry(Address protocol, Address hardware) {
		this.protocol = protocol;
		this.hardware = hardware;
	}

	public Address getProtocol() {
		return protocol;
	}

	public void setProtocol(Address protocol) {
		this.protocol = protocol;
	}

	public Address getHardware() {
		return hardware;
	}

	public void setHardware(Address hardware) {
		this.hardware = hardware;
	}

	public ArpEntry getJavaEntry() {
		byte[] mac = Arrays.copyOfRange(hardware.getData(), 0, 6);
		byte[] ip = Arrays.copyOfRange(protocol.getData(), 0, 4);
		InetAddress inet = null;

		try {
			inet = InetAddress.getByAddress(ip);
		} catch (UnknownHostException e) {
		}

		return new ArpEntry(new MacAddress(mac), inet);
	}

}
