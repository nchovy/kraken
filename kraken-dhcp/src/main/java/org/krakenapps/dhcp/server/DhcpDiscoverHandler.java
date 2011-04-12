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
package org.krakenapps.dhcp.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

import org.krakenapps.dhcp.DhcpMessage;
import org.krakenapps.dhcp.DhcpServer;
import org.krakenapps.dhcp.MacAddress;
import org.krakenapps.dhcp.model.DhcpIpGroup;
import org.krakenapps.dhcp.model.DhcpIpLease;
import org.krakenapps.dhcp.model.DhcpIpReservation;
import org.krakenapps.dhcp.model.DhcpOptionConfig;
import org.krakenapps.dhcp.options.ByteConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DhcpDiscoverHandler {
	private final Logger logger = LoggerFactory.getLogger(DhcpDiscoverHandler.class.getName());

	private DhcpServer server;
	private DhcpServerContext c;

	public DhcpDiscoverHandler(DhcpServer server, DhcpServerContext c) {
		this.server = server;
		this.c = c;
	}

	public void handle(DhcpMessage msg) throws IOException {
		// check if blocked
		if (c.blockFilters.containsKey(msg.getClientMac())) {
			logger.warn("kraken dhcp: client [{}] discover ignored by block list", msg.getClientMac());
			return;
		}

		// determine ip address
		String hostName = DhcpOptions.getHostName(msg);
		InetAddress yourAddress = determineIp(msg.getClientMac(), hostName);
		if (yourAddress == null) {
			logger.warn("kraken dhcp: address cannot be allocated");
			return;
		}

		// send offer
		DhcpIpGroup group = server.getIpGroup(yourAddress);
		List<DhcpOptionConfig> configs = server.getGroupOptions(group.getName());
		DhcpMessage m = DhcpMessageBuilder.newOffer(msg, configs, yourAddress);
		c.send(m);
	}

	private InetAddress determineIp(MacAddress mac, String hostName) {
		DhcpIpReservation r = c.reserveMap.get(mac);
		if (r != null)
			return r.getIp();

		List<DhcpIpGroup> groups = DhcpDatabase.getIpGroups(c.conn);

		for (DhcpIpGroup group : groups) {
			long from = ByteConverter.toInteger(group.getFrom().getAddress()) & 0xffffffffl;
			long to = ByteConverter.toInteger(group.getTo().getAddress()) & 0xffffffffl;

			for (long target = from + 1; from <= to; target++) {
				try {
					InetAddress t = InetAddress.getByAddress(ByteConverter.convert((int) target));
					if (c.offerMap.containsKey(t))
						continue;

					
					List<DhcpOptionConfig> options = DhcpDatabase.getGroupConfigs(group.getName());
					int leaseDuration = DhcpDatabase.getLeaseDuration(options);

					DhcpIpLease lease = new DhcpIpLease(group.getName(), t, mac, hostName, leaseDuration);
					DhcpIpLease old = c.leaseMap.get(t);

					if (old == null) {
						c.offerMap.put(t, lease);
						return t;
					} else {
						// exists but expired
						if (old.getExpire().before(new Date())) {
							c.offerMap.put(t, lease);
							return t;
						}
					}

				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}

}
