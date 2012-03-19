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
package org.krakenapps.linux.api;

public class ArpEntry {
	private String ip;
	private int hwType;
	private String hardware;
	private String flags;
	private String mac;
	private String mask;
	private String device;

	public ArpEntry(String ip, int hwType, String flags, String mac, String mask, String device) {
		this.ip = ip;
		this.hwType = hwType;
		this.flags = flags;
		this.mac = mac;
		this.mask = mask;
		this.device = device;
	}
	
	public ArpEntry(String ip, String hardware, String flags, String mac, String mask, String device) {
		this.ip = ip;
		this.hardware = hardware;
		this.flags = flags;
		this.mac = mac;
		this.mask = mask;
		this.device = device;
	}
	
	public String getIp() {
		return ip;
	}

	public int getHwType() {
		return hwType;
	}

	public String getHardware() {
		return hardware;
	}
	
	public String getFlags() {
		return flags;
	}

	public String getMac() {
		return mac;
	}

	public String getMask() {
		return mask;
	}

	public String getDevice() {
		return device;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((device == null) ? 0 : device.hashCode());
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
		if (device == null) {
			if (other.device != null)
				return false;
		} else if (!device.equals(other.device))
			return false;
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
		return String.format("device=%s, ip=%s, mac=%s", device, ip, mac);
	}
}