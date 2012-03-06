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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.dhcp.DhcpMessage;
import org.krakenapps.dhcp.DhcpMessageListener;
import org.krakenapps.dhcp.DhcpOption;
import org.krakenapps.dhcp.DhcpOptionCode;
import org.krakenapps.dhcp.DhcpServer;
import org.krakenapps.dhcp.MacAddress;
import org.krakenapps.dhcp.model.DhcpFilter;
import org.krakenapps.dhcp.model.DhcpIpGroup;
import org.krakenapps.dhcp.model.DhcpIpLease;
import org.krakenapps.dhcp.model.DhcpIpReservation;
import org.krakenapps.dhcp.model.DhcpOptionConfig;
import org.krakenapps.dhcp.options.ByteConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "dhcp-server")
@Provides(specifications = { DhcpServer.class })
public class DhcpServerImpl implements DhcpServer, Runnable {

	private final Logger logger = LoggerFactory.getLogger(DhcpServerImpl.class.getName());
	private Thread t;

	private DhcpServerContext c;
	private DhcpDiscoverHandler discoverHandler;
	private DhcpRequestHandler requestHandler;
	private DhcpDeclineHandler declineHandler;
	private DhcpInformHandler informHandler;
	private DhcpReleaseHandler releaseHandler;

	/**
	 * listener thread stop indicator
	 */
	private volatile boolean doStop = false;

	public static void main(String[] args) throws Exception {
		DhcpServerImpl server = new DhcpServerImpl();
		server.start();
	}

	@Validate
	public void start() throws Exception {
		initHandlers();
		loadDatabase();

		t = new Thread(this, "DHCP Server");
		t.start();

		logger.info("kraken dhcp: server started");
	}

	private void initHandlers() throws Exception {
		c = new DhcpServerContext();
		discoverHandler = new DhcpDiscoverHandler(this, c);
		requestHandler = new DhcpRequestHandler(this, c);
		declineHandler = new DhcpDeclineHandler(c);
		informHandler = new DhcpInformHandler(this, c);
		releaseHandler = new DhcpReleaseHandler(c);
	}

	private void loadDatabase() {
		DhcpDatabase.checkSchema(c.conn);

		for (DhcpIpGroup group : DhcpDatabase.getIpGroups(c.conn)) {
			for (DhcpIpLease lease : DhcpDatabase.getIpLeases(group.getName()))
				c.leaseMap.put(lease.getIp(), lease);

			for (DhcpIpReservation r : DhcpDatabase.getIpReservations(group.getName()))
				c.reserveMap.put(r.getMac(), r);
		}

		for (DhcpFilter f : DhcpDatabase.getAllowFilters())
			c.allowFilters.put(f.getMac(), f);

		for (DhcpFilter f : DhcpDatabase.getBlockFilters())
			c.blockFilters.put(f.getMac(), f);
	}

	@Invalidate
	public void stop() {
		// thread will stop automatically when socket is closed
		c.close();

		logger.info("kraken dhcp: server stopped");
	}

	@Override
	public void addListener(DhcpMessageListener callback) {
		c.callbacks.add(callback);
	}

	@Override
	public void removeListener(DhcpMessageListener callback) {
		c.callbacks.remove(callback);
	}

	@Override
	public void run() {
		try {
			while (doStop == false) {
				DhcpMessage msg = c.receive();
				if (msg == null)
					continue;

				logger.debug("kraken dhcp: received {}", msg);
				handle(msg);
			}
		} catch (IOException e) {
			if (!e.getMessage().contains("socket closed"))
				logger.error("kraken dhcp: server socket error", e);
		} finally {
			logger.info("kraken dhcp: server stopped");
		}
	}

	private void handle(DhcpMessage msg) {
		try {
			DhcpOption option = msg.getOption(DhcpOptionCode.DhcpMessageType.code());
			int type = option.getValue()[0] & 0xff;

			switch (type) {
			case 1:
				discoverHandler.handle(msg);
				break;
			case 3:
				requestHandler.handle(msg);
				break;
			case 4:
				declineHandler.handle(msg);
				break;
			case 7:
				releaseHandler.handle(msg);
				break;
			case 8:
				informHandler.handle(msg);
				break;
			default:
				logger.warn("kraken dhcp: maybe other server's message", msg);
				break;
			}

			clearTimeout();

		} catch (IOException e) {
			logger.error("kraken dhcp: cannot handle msg - " + msg, e);
		} catch (RuntimeException e) {
			logger.error("kraken dhcp: cannot handle msg - " + msg, e);
		}
	}

	private void clearTimeout() {
		for (InetAddress ip : c.offerMap.keySet()) {
			DhcpIpLease offer = c.offerMap.get(ip);
			long gap = new Date().getTime() - offer.getCreated().getTime();

			// remind only under 60sec offers
			if (gap > 60 * 1000)
				c.offerMap.remove(ip);
		}
	}

	@Override
	public DhcpIpGroup getIpGroup(InetAddress ip) {
		for (DhcpIpGroup g : getIpGroups()) {
			long from = ByteConverter.toUnsignedInteger(g.getFrom().getAddress());
			long to = ByteConverter.toUnsignedInteger(g.getTo().getAddress());
			long n = ByteConverter.toUnsignedInteger(ip.getAddress());

			if (from <= n && n <= to)
				return g;
		}

		return null;
	}

	@Override
	public List<DhcpOptionConfig> getGroupOptions(String groupName) {
		return DhcpDatabase.getGroupConfigs(groupName);
	}

	@Override
	public void createGroupOption(DhcpOptionConfig config) {
		DhcpDatabase.createGroupConfig(config);
	}

	@Override
	public void removeGroupOption(int id) {
		DhcpDatabase.removeGroupConfig(id);
	}

	@Override
	public List<DhcpFilter> getAllowFilters() {
		return DhcpDatabase.getAllowFilters();
	}

	@Override
	public List<DhcpFilter> getBlockFilters() {
		return DhcpDatabase.getBlockFilters();
	}

	@Override
	public List<DhcpIpGroup> getIpGroups() {
		return DhcpDatabase.getIpGroups();
	}

	@Override
	public List<DhcpIpLease> getIpOffers() {
		return new ArrayList<DhcpIpLease>(c.offerMap.values());
	}

	@Override
	public List<DhcpIpLease> getIpLeases(String groupName) {
		return DhcpDatabase.getIpLeases(groupName);
	}

	@Override
	public List<DhcpIpReservation> getIpReservations(String groupName) {
		return DhcpDatabase.getIpReservations(groupName);
	}

	@Override
	public void purgeIpLease() {
		DhcpDatabase.purgeIpLease();
		c.leaseMap.clear();
	}

	@Override
	public void purgeIpLease(InetAddress ip) {
		DhcpDatabase.purgeIpLease(ip);
		c.leaseMap.remove(ip);
	}

	@Override
	public void createIpGroup(DhcpIpGroup group) {
		DhcpDatabase.createIpGroup(group);
	}

	@Override
	public void updateIpGroup(DhcpIpGroup group) {
		DhcpDatabase.updateIpGroup(group);
	}

	@Override
	public void removeIpGroup(String name) {
		// TODO: check existence
		DhcpDatabase.removeIpGroup(name);
	}

	@Override
	public void reserve(DhcpIpReservation entry) {
		// in memory
		c.reserveMap.put(entry.getMac(), entry);

		// persist
		DhcpDatabase.createIpReservation(entry);
	}

	@Override
	public void unreserve(DhcpIpReservation entry) {
		// persist
		DhcpDatabase.removeIpReservation(entry);

		// in memory
		c.reserveMap.remove(entry.getMac());
	}

	@Override
	public void createFilter(DhcpFilter filter) {
		DhcpDatabase.createFilter(filter);

		if (filter.isAllow())
			c.allowFilters.put(filter.getMac(), filter);
		else
			c.blockFilters.put(filter.getMac(), filter);
	}

	@Override
	public void removeFilter(MacAddress mac) {
		DhcpDatabase.removeFilter(mac);

		// only one side has this mac
		c.allowFilters.remove(mac);
		c.blockFilters.remove(mac);
	}

}
