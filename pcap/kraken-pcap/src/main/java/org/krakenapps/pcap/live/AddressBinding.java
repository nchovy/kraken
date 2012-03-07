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

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author delmitz
 */
public class AddressBinding {
	private InetAddress address;
	private InetAddress subnet;
	private InetAddress broadcast;
	private InetAddress destination;

	public AddressBinding(byte[] address, byte[] subnet, byte[] broadcast, byte[] destination)
			throws UnknownHostException {
		if (address != null)
			this.address = InetAddress.getByAddress(address);
		if (subnet != null)
			this.subnet = InetAddress.getByAddress(subnet);
		if (broadcast != null)
			this.broadcast = InetAddress.getByAddress(broadcast);
		if (destination != null)
			this.destination = InetAddress.getByAddress(destination);
	}

	public InetAddress getAddress() {
		return address;
	}

	public InetAddress getSubnet() {
		return subnet;
	}

	public InetAddress getBroadcast() {
		return broadcast;
	}

	public InetAddress getDestination() {
		return destination;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AddressBinding other = (AddressBinding) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (broadcast == null) {
			if (other.broadcast != null)
				return false;
		} else if (!broadcast.equals(other.broadcast))
			return false;
		if (destination == null) {
			if (other.destination != null)
				return false;
		} else if (!destination.equals(other.destination))
			return false;
		if (subnet == null) {
			if (other.subnet != null)
				return false;
		} else if (!subnet.equals(other.subnet))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + ((broadcast == null) ? 0 : broadcast.hashCode());
		result = prime * result + ((destination == null) ? 0 : destination.hashCode());
		result = prime * result + ((subnet == null) ? 0 : subnet.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return String.format("NetworkInterfaceAddress [address=%s, broadcast=%s, destination=%s, subnet=%s]", address,
				broadcast, destination, subnet);
	}

}
