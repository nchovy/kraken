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
package org.krakenapps.pcap.routing;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.List;

import org.krakenapps.pcap.util.IpConverter;

public class RoutingTable {
	static {
		System.loadLibrary("kpcap");
	}
	
	public static List<RoutingEntry> getRoutingEntries() {
		return getNativeRoutingEntries();
	}

	private static native List<RoutingEntry> getNativeRoutingEntries();

	public static RoutingEntry findRoute(InetAddress ip) {
		int target = IpConverter.toInt((Inet4Address) ip);

		for (RoutingEntry entry : RoutingTable.getRoutingEntries()) {
			int dst = IpConverter.toInt((Inet4Address) entry.getDestination());
			int mask = IpConverter.toInt((Inet4Address) entry.getMask());

			if (dst == (target & mask)) {
				return entry;
			}
		}

		return null;
	}
}
