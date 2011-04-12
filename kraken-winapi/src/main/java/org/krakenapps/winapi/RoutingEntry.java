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
package org.krakenapps.winapi;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.krakenapps.winapi.RoutingTable.Protocol;
import org.krakenapps.winapi.RoutingTable.Type;

public class RoutingEntry {
	private InetAddress destination;
	private InetAddress subnet;
	private int policy;
	private int nextHop;
	private InetAddress interfaceAddress;
	private int ifIndex;
	private Type type;
	private Protocol protocol;
	private int age;
	private int metric1;
	private int metric2;
	private int metric3;
	private int metric4;
	private int metric5;

	public RoutingEntry(byte[] destination, byte[] subnet, int policy, int nextHop, byte[] interfaceAddress,
			int ifIndex, String type, String protocol, int age, int metric1, int metric2, int metric3, int metric4,
			int metric5) throws UnknownHostException {
		this.destination = InetAddress.getByAddress(destination);
		this.subnet = InetAddress.getByAddress(subnet);
		this.policy = policy;
		this.nextHop = nextHop;
		this.interfaceAddress = InetAddress.getByAddress(interfaceAddress);
		this.ifIndex = ifIndex;
		this.type = Type.valueOf(type);
		this.protocol = Protocol.valueOf(protocol);
		this.age = age;
		this.metric1 = metric1;
		this.metric2 = metric2;
		this.metric3 = metric3;
		this.metric4 = metric4;
		this.metric5 = metric5;
	}

	public InetAddress getDestination() {
		return destination;
	}

	public InetAddress getSubnet() {
		return subnet;
	}

	public int getPolicy() {
		return policy;
	}

	public int getNextHop() {
		return nextHop;
	}

	public InetAddress getInterfaceAddress() {
		return interfaceAddress;
	}

	public int getIfIndex() {
		return ifIndex;
	}

	public Type getType() {
		return type;
	}

	public Protocol getProtocol() {
		return protocol;
	}

	public int getAge() {
		return age;
	}

	public int getMetric1() {
		return metric1;
	}

	public int getMetric2() {
		return metric2;
	}

	public int getMetric3() {
		return metric3;
	}

	public int getMetric4() {
		return metric4;
	}

	public int getMetric5() {
		return metric5;
	}

	@Override
	public String toString() {
		return "destination=" + destination.getHostAddress() + ", subnet=" + subnet.getHostAddress() + ", interface="
				+ interfaceAddress.getHostAddress();
	}

}
