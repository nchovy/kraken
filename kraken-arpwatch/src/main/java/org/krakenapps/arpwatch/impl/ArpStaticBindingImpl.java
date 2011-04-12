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
package org.krakenapps.arpwatch.impl;

import java.net.InetAddress;

import org.krakenapps.arpwatch.ArpStaticBinding;
import org.krakenapps.pcap.decoder.ethernet.MacAddress;

public class ArpStaticBindingImpl implements ArpStaticBinding {
	private MacAddress mac;
	private InetAddress ip;

	public ArpStaticBindingImpl(MacAddress mac, InetAddress ip) {
		this.mac = mac;
		this.ip = ip;
	}

	@Override
	public InetAddress getIpAddress() {
		return ip;
	}

	@Override
	public MacAddress getMacAddress() {
		return mac;
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
		ArpStaticBindingImpl other = (ArpStaticBindingImpl) obj;
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
		return String.format("mac=%s, ip=%s", mac, ip.getHostAddress());
	}
}
