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
package org.krakenapps.pcap.live;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.krakenapps.pcap.decoder.ethernet.MacAddress;
import org.krakenapps.pcap.util.IpConverter;

public class PcapDeviceMetadata {
	private final String name;
	private final String description;
	private final boolean loopback;
	private final String datalinkName;
	private final String datalinkDescription;
	private final MacAddress macAddress;
	private final AddressBinding[] bindings;
	private final InetAddress subnet;
	private final InetAddress mask;
	private final int networkPrefixLength;

	private PcapDeviceMetadata(String name, String description, boolean loopback, String datalinkName,
			String datalinkDescription, byte[] macAddress, AddressBinding[] bindings, byte[] subnet,
			int networkPrefixLength) throws UnknownHostException {
		this.name = name;
		this.description = description;
		this.loopback = loopback;
		this.datalinkName = datalinkName;
		this.datalinkDescription = datalinkDescription;
		this.macAddress = new MacAddress(macAddress);
		this.bindings = bindings;
		this.subnet = InetAddress.getByAddress(subnet);
		this.networkPrefixLength = networkPrefixLength;

		int m = 0;
		int bit = 1;
		for (int i = 31; i >= 32 - networkPrefixLength; i--) {
			m |= (bit << i);
		}

		mask = IpConverter.toInetAddress(m);
	}

	public boolean isIntranet(InetAddress ip) {
		if (ip instanceof Inet6Address || mask instanceof Inet6Address) {
			throw new UnsupportedOperationException();
		}

		int target = IpConverter.toInt((Inet4Address) ip);
		int m = IpConverter.toInt((Inet4Address) mask);
		int net = IpConverter.toInt((Inet4Address) subnet);
		return (target & m) == net;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public boolean isLoopback() {
		return loopback;
	}

	public String getDatalinkName() {
		return datalinkName;
	}

	public String getDatalinkDescription() {
		return datalinkDescription;
	}

	public MacAddress getMacAddress() {
		return macAddress;
	}

	public AddressBinding[] getBindings() {
		return bindings;
	}

	public InetAddress getSubnet() {
		return subnet;
	}

	public InetAddress getNetmask() {
		return mask;
	}

	public int getNetworkPrefixLength() {
		return networkPrefixLength;
	}

	public InetAddress getInet4Address() {
		for (AddressBinding binding : getBindings())
			if (binding.getAddress() instanceof Inet4Address)
				return binding.getAddress();

		return null;
	}

	@Override
	public String toString() {
		return String.format("name=%s, description=%s, mac=%s", name, description, macAddress);
	}
}
