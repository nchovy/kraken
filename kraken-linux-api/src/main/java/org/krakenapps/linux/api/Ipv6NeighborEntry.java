package org.krakenapps.linux.api;

public class Ipv6NeighborEntry {
	private String address;
	private String device;
	private String mac;
	private String state;
	
	public Ipv6NeighborEntry(String address, String device, String mac, String state) {
		this.address = address;
		this.device = device;
		this.mac = mac;
		this.state = state;
	}
	
	public String getAddress() {
		return address;
	}
	
	public String getDevice() {
		return device;
	}
	
	public String getMac() {
		return mac;
	}
	
	public String getState() {
		return state;
	}
}