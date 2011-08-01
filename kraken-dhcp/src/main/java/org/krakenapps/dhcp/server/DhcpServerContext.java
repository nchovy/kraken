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

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.krakenapps.dhcp.DhcpMessage;
import org.krakenapps.dhcp.DhcpMessageListener;
import org.krakenapps.dhcp.MacAddress;
import org.krakenapps.dhcp.model.DhcpFilter;
import org.krakenapps.dhcp.model.DhcpIpLease;
import org.krakenapps.dhcp.model.DhcpIpReservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DhcpServerContext {
	public static InetAddress ZERO_ADDRESS;

	private final Logger logger = LoggerFactory.getLogger(DhcpServerContext.class.getName());

	public Connection conn;

	public CopyOnWriteArraySet<DhcpMessageListener> callbacks;

	/**
	 * offered ip address (can be timeout'd)
	 */
	public ConcurrentMap<InetAddress, DhcpIpLease> offerMap;

	/**
	 * leased ip address map
	 */
	public ConcurrentMap<InetAddress, DhcpIpLease> leaseMap;

	/**
	 * reserved mac address mappings
	 */
	public ConcurrentMap<MacAddress, DhcpIpReservation> reserveMap;

	public ConcurrentMap<MacAddress, DhcpFilter> blockFilters;

	public ConcurrentMap<MacAddress, DhcpFilter> allowFilters;

	private byte[] buf = new byte[4096];
	private DatagramSocket socket;

	static {
		try {
			ZERO_ADDRESS = InetAddress.getByName("0.0.0.0");
		} catch (UnknownHostException e) {
			// not reachable
		}
	}

	public DhcpServerContext() throws Exception {
		offerMap = new ConcurrentHashMap<InetAddress, DhcpIpLease>();
		leaseMap = new ConcurrentHashMap<InetAddress, DhcpIpLease>();
		reserveMap = new ConcurrentHashMap<MacAddress, DhcpIpReservation>();
		blockFilters = new ConcurrentHashMap<MacAddress, DhcpFilter>();
		allowFilters = new ConcurrentHashMap<MacAddress, DhcpFilter>();
		socket = new DatagramSocket(67);

		System.setProperty("derby.system.home", System.getProperty("user.dir"));
		new File(System.getProperty("kraken.data.dir"), "kraken-dhcp").mkdirs();
		conn = DhcpDatabase.newConnection();
	}

	public DhcpMessage receive() throws IOException {
		DatagramPacket p = new DatagramPacket(buf, buf.length);
		socket.receive(p);

		ByteBuffer bb = ByteBuffer.wrap(p.getData(), 0, p.getLength());
		return DhcpMessageParser.parse(bb);
	}

	public void send(DhcpMessage msg) throws IOException {
		send(InetAddress.getByName("255.255.255.255"), msg);
	}

	public void send(InetAddress destination, DhcpMessage msg) throws IOException {
		byte[] buf = DhcpMessageBuilder.encode(msg);
		DatagramPacket p = new DatagramPacket(buf, buf.length, destination, 68);
		socket.send(p);
	}

	public void close() {
		if (socket != null)
			socket.close();

		try {
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
			logger.error("kraken dhcp: cannot close database", e);
		}
	}

}
