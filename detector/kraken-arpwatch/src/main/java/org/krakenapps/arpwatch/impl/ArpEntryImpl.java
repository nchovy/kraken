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
import java.text.SimpleDateFormat;
import java.util.Date;

import org.krakenapps.arpwatch.ArpEntry;
import org.krakenapps.pcap.decoder.ethernet.MacAddress;

public class ArpEntryImpl implements ArpEntry {
	private MacAddress mac;
	private InetAddress ip;
	private Date firstSeen;
	private Date lastSeen;

	public ArpEntryImpl(MacAddress mac, InetAddress ip, Date firstSeen, Date lastSeen) {
		this.mac = mac;
		this.ip = ip;
		this.firstSeen = firstSeen;
		this.lastSeen = lastSeen;
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
	public Date getFirstSeen() {
		return firstSeen;
	}

	@Override
	public Date getLastSeen() {
		return lastSeen;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((firstSeen == null) ? 0 : firstSeen.hashCode());
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + ((lastSeen == null) ? 0 : lastSeen.hashCode());
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
		ArpEntryImpl other = (ArpEntryImpl) obj;
		if (firstSeen == null) {
			if (other.firstSeen != null)
				return false;
		} else if (!firstSeen.equals(other.firstSeen))
			return false;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		if (lastSeen == null) {
			if (other.lastSeen != null)
				return false;
		} else if (!lastSeen.equals(other.lastSeen))
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
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		return String.format("ip=%s, mac=%s, first seen=%s, last seen=%s", ip.getHostAddress(), mac, dateFormat
				.format(firstSeen), dateFormat.format(lastSeen));
	}

}
