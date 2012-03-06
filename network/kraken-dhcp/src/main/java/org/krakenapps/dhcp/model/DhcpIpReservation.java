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
package org.krakenapps.dhcp.model;

import java.net.InetAddress;

import org.krakenapps.dhcp.MacAddress;

public class DhcpIpReservation {
	private String groupName;
	private String hostName;
	private InetAddress ip;
	private MacAddress mac;

	public DhcpIpReservation(String groupName, InetAddress ip, MacAddress mac, String hostName) {
		this.groupName = groupName;
		this.ip = ip;
		this.mac = mac;
		this.hostName = hostName;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
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

	@Override
	public String toString() {
		return String.format("ip=%s, mac=%s, host=%s", ip.getHostAddress(), mac, hostName);
	}

}
