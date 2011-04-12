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
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.krakenapps.dhcp.DhcpMessage;
import org.krakenapps.dhcp.DhcpOption;
import org.krakenapps.dhcp.DhcpOptionCode;
import org.krakenapps.dhcp.DhcpServer;
import org.krakenapps.dhcp.model.DhcpIpGroup;
import org.krakenapps.dhcp.model.DhcpIpLease;
import org.krakenapps.dhcp.model.DhcpIpReservation;
import org.krakenapps.dhcp.model.DhcpOptionConfig;
import org.krakenapps.dhcp.options.ByteConverter;
import org.krakenapps.dhcp.options.RequestedIpAddressOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DhcpRequestHandler {
	private final Logger logger = LoggerFactory.getLogger(DhcpRequestHandler.class.getName());
	private DhcpServer server;
	private DhcpServerContext c;

	public DhcpRequestHandler(DhcpServer server, DhcpServerContext c) {
		this.server = server;
		this.c = c;
	}

	public void handle(DhcpMessage msg) throws IOException {

		// check if blocked
		if (c.blockFilters.containsKey(msg.getClientMac())) {
			logger.info("kraken dhcp: client [{}] discover ignored by block list", msg.getClientMac());
			return;
		}

		// request ip
		InetAddress requestedIp = getRequestedIp(msg);
		if (requestedIp == null) {
			logger.trace("kraken dhcp: invalid dhcp request, null request ip from {}", msg.getClientMac());
			return;
		}

		DhcpIpGroup group = server.getIpGroup(requestedIp);
		if (group == null) {
			logger.trace("kraken dhcp: ip group not found for {}", requestedIp.getHostAddress());
			return;
		}

		List<DhcpOptionConfig> configs = server.getGroupOptions(group.getName());
		InetAddress serverIp = DhcpDatabase.getServerIdentifier(configs);

		// check server identifier, and ignore if not matched
		DhcpOption serverOption = msg.getOption(DhcpOptionCode.ServerIdentifier.code());

		if (serverOption != null) {
			InetAddress ip = ByteConverter.toInetAddress(serverOption.getValue());
			if (!ip.equals(serverIp)) {
				logger.info("kraken dhcp: ignored request to other dhcp server [{}]", ip.getHostAddress());
				return;
			}
		}

		// check ip reservation
		DhcpIpReservation r = c.reserveMap.get(msg.getClientMac());
		if (r != null && !r.getIp().equals(requestedIp)) {
			// reject and induce dhcp discover, then device will use
			// reserved ip
			c.send(DhcpMessageBuilder.newNak(msg, serverIp));
			return;
		}

		// check if already used
		DhcpIpLease lease = c.leaseMap.get(requestedIp);
		if (lease != null) {
			handleWithOldLease(msg, requestedIp, lease);
			return;
		}

		// check if offered to this client
		DhcpIpLease offer = c.offerMap.get(requestedIp);
		if (offer != null && !offer.getMac().equals(msg.getClientMac())) {
			handleIpConflict(msg, requestedIp, offer, serverIp);
			return;
		}

		lease(group, configs, msg, requestedIp, offer);
	}

	private InetAddress getRequestedIp(DhcpMessage msg) {
		InetAddress requestedIp = null;
		RequestedIpAddressOption requestedIpOption = (RequestedIpAddressOption) msg
				.getOption(DhcpOptionCode.RequestedIpAddress.code());
		if (requestedIpOption == null) {
			if (!msg.getClientAddress().equals(DhcpServerContext.ZERO_ADDRESS))
				requestedIp = msg.getClientAddress();
		} else {
			requestedIp = requestedIpOption.getRequestedIp();
		}
		return requestedIp;
	}

	private void lease(DhcpIpGroup group, List<DhcpOptionConfig> configs, DhcpMessage msg, InetAddress requestedIp,
			DhcpIpLease offer) throws IOException {
		String hostName = DhcpOptions.getHostName(msg);
		InetAddress serverIp = DhcpDatabase.getServerIdentifier(configs);
		int leaseDuration = DhcpDatabase.getLeaseDuration(configs);

		// no previous offer found
		if (offer == null)
			offer = new DhcpIpLease(group.getName(), requestedIp, msg.getClientMac(), hostName, leaseDuration);

		DhcpIpLease lease = offer;

		try {
			// Success!
			DhcpDatabase.createIpLease(lease);

			c.leaseMap.put(requestedIp, lease);

			DhcpMessage m = DhcpMessageBuilder.newAck(msg, configs, requestedIp);
			logger.info("kraken dhcp: sending ack [{}]", m);
			c.send(m);
		} catch (RuntimeException e) {
			// duplicated entry
			if (e.getCause() instanceof SQLException) {
				DhcpMessage m = DhcpMessageBuilder.newNak(msg, serverIp);
				logger.info("kraken dhcp: ip conflict, sending nack [{}]", m);
				c.send(m);
			}
		}
	}

	private void handleIpConflict(DhcpMessage msg, InetAddress requestedIp, DhcpIpLease offer, InetAddress serverIp)
			throws IOException {
		logger.trace("kraken dhcp: request ip [{}] from [{}] is already offered to other client [{}]",
				new Object[] { requestedIp, msg.getClientMac(), offer.getMac() });

		DhcpMessage m = DhcpMessageBuilder.newNak(msg, serverIp);
		logger.trace("kraken dhcp: already offered to others, sending nack [{}]", m);
		c.send(m);
	}

	private void handleWithOldLease(DhcpMessage msg, InetAddress requestedIp, DhcpIpLease lease) throws IOException {
		DhcpIpGroup group = server.getIpGroup(requestedIp);
		List<DhcpOptionConfig> configs = server.getGroupOptions(group.getName());
		int leaseDuration = DhcpDatabase.getLeaseDuration(configs);
		InetAddress serverIp = DhcpDatabase.getServerIdentifier(configs);

		if (lease.getExpire().before(new Date())) {
			// replace expired lease
			lease.setMac(msg.getClientMac());
			lease.setHostName(DhcpOptions.getHostName(msg));
			lease.setCreated(new Date());
			lease.setUpdated(new Date());
			lease.setNewExpire(leaseDuration);
			DhcpDatabase.updateIpLease(c.conn, lease);

			c.send(DhcpMessageBuilder.newAck(msg, configs, requestedIp));

			logger.trace("kraken dhcp: replaced expired ip [{}] with [{}]", requestedIp.getHostAddress(), msg
					.getClientMac());
		} else if (lease.getMac().equals(msg.getClientMac())) {
			// extend expire
			lease.setUpdated(new Date());
			lease.setNewExpire(leaseDuration);
			DhcpDatabase.updateIpLease(c.conn, lease);

			c.send(DhcpMessageBuilder.newAck(msg, configs, requestedIp));

			logger.trace("kraken dhcp: extend ip lease [{}] from owner [{}]", requestedIp, msg.getClientMac());
		} else {
			logger.trace("kraken dhcp: request ip [{}] from [{}] is already leased by other client [{}]",
					new Object[] { requestedIp, msg.getClientMac(), lease.getMac() });

			c.send(DhcpMessageBuilder.newNak(msg, serverIp));
		}
	}
}
