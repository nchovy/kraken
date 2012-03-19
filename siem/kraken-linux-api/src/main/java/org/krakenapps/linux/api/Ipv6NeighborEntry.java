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
 */package org.krakenapps.linux.api;

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