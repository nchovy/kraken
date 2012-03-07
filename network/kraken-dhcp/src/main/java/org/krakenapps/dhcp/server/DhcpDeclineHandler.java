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

import org.krakenapps.dhcp.DhcpMessage;
import org.krakenapps.dhcp.model.DhcpIpLease;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DhcpDeclineHandler {
	private final Logger logger = LoggerFactory.getLogger(DhcpDeclineHandler.class.getName());
	private DhcpServerContext c;

	public DhcpDeclineHandler(DhcpServerContext c) {
		this.c = c;
	}

	public void handle(DhcpMessage msg) {
		DhcpIpLease lease = c.leaseMap.get(msg.getClientAddress());
		if (lease == null) {
			logger.trace("kraken dhcp: invalid decline for [{}, {}]", msg.getClientAddress(), msg.getClientMac());
			return;
		}

		if (lease.getMac().equals(msg.getClientMac())) {
			DhcpDatabase.removeIpLease(lease);
			c.leaseMap.remove(lease.getIp());

			logger.trace("kraken dhcp: client declined ip={}, mac={}", msg.getClientAddress().getHostAddress(), msg
					.getClientMac());
		}
	}
}
