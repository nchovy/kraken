package org.krakenapps.sleepproxy.impl;

import java.net.InetAddress;

public class NicInfo {
	private String mac;
	private InetAddress ip;
	private String description;

	public NicInfo() {
	}

	public NicInfo(String mac, InetAddress ip, String description) {
		this.mac = mac;
		this.ip = ip;
		this.description = description;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public InetAddress getIp() {
		return ip;
	}

	public void setIp(InetAddress ip) {
		this.ip = ip;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
