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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.krakenapps.dhcp.MacAddress;
import org.krakenapps.dhcp.model.DhcpFilter;
import org.krakenapps.dhcp.model.DhcpIpGroup;
import org.krakenapps.dhcp.model.DhcpIpLease;
import org.krakenapps.dhcp.model.DhcpIpReservation;
import org.krakenapps.dhcp.model.DhcpOptionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DhcpDatabase {
	private DhcpDatabase() {
	}

	public static Connection newConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:derby:data/kraken-dhcp/dhcpdb;create=true");
	}

	public static void checkSchema(Connection conn) {
		trace("kraken dhcp: checking database schema");

		createTable(conn, "dhcp_ip_groups", "CREATE TABLE dhcp_ip_groups (\n" +
				"name VARCHAR(60) NOT NULL,\n" +
				"description VARCHAR(2000),\n" +
				"from_addr VARCHAR(60) NOT NULL,\n" +
				"to_addr VARCHAR(60) NOT NULL,\n" +
				"PRIMARY KEY (name))");

		createTable(conn, "dhcp_group_options", "CREATE TABLE dhcp_group_options (\n" +
				"id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT By 1),\n" +
				"group_name VARCHAR(60) NOT NULL,\n" +
				"type INTEGER NOT NULL,\n" +
				"value VARCHAR(2000) NOT NULL,\n" +
				"ordinal INTEGER NOT NULL,\n" +
				"PRIMARY KEY (id))");

		createTable(conn, "dhcp_ip_leases", "CREATE TABLE dhcp_ip_leases (\n" +
				"ip VARCHAR(60) NOT NULL,\n" +
				"mac VARCHAR(20) NOT NULL,\n" +
				"group_name VARCHAR(60) NOT NULL,\n" +
				"host_name VARCHAR(60),\n" +
				"created_at TIMESTAMP NOT NULL,\n" +
				"updated_at TIMESTAMP NOT NULL,\n" +
				"expired_at TIMESTAMP NOT NULL,\n" +
				"PRIMARY KEY (ip))");

		createTable(conn, "dhcp_ip_reservations", "CREATE TABLE dhcp_ip_reservations (\n" +
				"ip VARCHAR(60) NOT NULL,\n" +
				"mac VARCHAR(20) NOT NULL,\n" +
				"group_name VARCHAR(60) NOT NULL,\n" +
				"host_name VARCHAR(60),\n" +
				"PRIMARY KEY (ip))");

		createTable(conn, "dhcp_filters", "CREATE TABLE dhcp_filters (\n" +
				"mac VARCHAR(60) NOT NULL,\n" +
				"description VARCHAR(2000),\n" +
				"is_allowed INTEGER NOT NULL,\n" +
				"PRIMARY KEY (mac))");
	}

	private static void createTable(Connection conn, String tableName, String query) {
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(query);
			conn.commit();

			trace("kraken dhcp: table {} created", tableName);
		} catch (SQLException e) {
			try {
				conn.rollback();

				if (!e.getMessage().contains("already exists in Schema"))
					error(e);
				else {
					trace("kraken dhcp: table [{}] already exists", tableName);
					return;
				}

			} catch (SQLException e1) {
				error(e1);
			}
			throw new RuntimeException(e);
		} finally {

		}
	}

	public static List<DhcpIpGroup> getIpGroups() {
		Connection c = null;
		try {
			return getIpGroups(c);
		} finally {
			disconnect(c);
		}
	}

	public static List<DhcpIpGroup> getIpGroups(Connection c) {
		List<DhcpIpGroup> groups = new ArrayList<DhcpIpGroup>();
		try {
			c = newConnection();
			Statement stmt = c.createStatement();
			String q = "select name, description, from_addr, to_addr from dhcp_ip_groups";
			ResultSet rs = stmt.executeQuery(q);

			while (rs.next()) {
				try {
					DhcpIpGroup group = new DhcpIpGroup();
					group.setName(rs.getString(1));
					group.setDescription(rs.getString(2));
					group.setFrom(InetAddress.getByName(rs.getString(3)));
					group.setTo(InetAddress.getByName(rs.getString(4)));
					groups.add(group);
				} catch (UnknownHostException e1) {
					error(e1);
				}
			}

		} catch (SQLException e) {
			error(e);
			throw new RuntimeException(e);
		}

		return groups;
	}

	public static void purgeIpLease() {
		Connection c = null;
		try {
			c = newConnection();
			Statement stmt = c.createStatement();
			stmt.executeUpdate("delete from dhcp_ip_leases");
			c.commit();
		} catch (SQLException e) {
			error(e);
			throw new RuntimeException(e);
		} finally {
			disconnect(c);
		}
	}

	public static void purgeIpLease(InetAddress ip) {
		Connection c = null;
		try {
			c = newConnection();
			String q = "delete from dhcp_ip_leases where ip=?";
			PreparedStatement stmt = c.prepareStatement(q);
			stmt.setString(1, ip.getHostAddress());
			stmt.executeUpdate();
			c.commit();
		} catch (SQLException e) {
			error(e);
			rollback(c);
			throw new RuntimeException(e);
		} finally {
			disconnect(c);
		}
	}

	public static void createIpGroup(DhcpIpGroup group) {
		Connection c = null;
		try {
			c = newConnection();
			String q = "insert into dhcp_ip_groups (name, description, from_addr, to_addr) values (?,?,?,?)";
			PreparedStatement stmt = c.prepareStatement(q);
			stmt.setString(1, group.getName());
			stmt.setString(2, group.getDescription());
			stmt.setString(3, group.getFrom().getHostAddress());
			stmt.setString(4, group.getTo().getHostAddress());
			stmt.execute();

			c.commit();
		} catch (SQLException e) {
			error(e);
			rollback(c);
			throw new RuntimeException(e);
		} finally {
			disconnect(c);
		}
	}

	public static void updateIpGroup(DhcpIpGroup group) {
		Connection c = null;
		try {
			c = newConnection();
			String q = "update dhcp_ip_groups set description=?, from_addr=?, to_addr=? where name=?";
			PreparedStatement stmt = c.prepareStatement(q);
			stmt.setString(1, group.getDescription());
			stmt.setString(2, group.getFrom().getHostAddress());
			stmt.setString(3, group.getTo().getHostAddress());
			stmt.setString(4, group.getName());

			stmt.executeUpdate();

		} catch (SQLException e) {
			error(e);
			rollback(c);
			throw new RuntimeException(e);
		} finally {
			disconnect(c);
		}
	}

	public static void removeIpGroup(String name) {
		Connection c = null;
		try {
			c = newConnection();

			String q = "delete from dhcp_ip_reservations where group_name=?";
			PreparedStatement stmt = c.prepareStatement(q);
			stmt.setString(1, name);
			stmt.executeUpdate();
			stmt.close();
			
			q = "delete from dhcp_group_options where group_name=?";
			stmt = c.prepareStatement(q);
			stmt.setString(1, name);
			stmt.executeUpdate();
			stmt.close();
			
			q = "delete from dhcp_ip_leases where group_name=?";
			stmt = c.prepareStatement(q);
			stmt.setString(1, name);
			stmt.executeUpdate();
			stmt.close();

			q = "delete from dhcp_ip_groups where name = ?";
			stmt = c.prepareStatement(q);
			stmt.setString(1, name);
			stmt.executeUpdate();
			stmt.close();
			
			c.commit();
		} catch (SQLException e) {
			rollback(c);
			throw new RuntimeException(e);
		} finally {
			disconnect(c);
		}
	}

	public static int getLeaseDuration(List<DhcpOptionConfig> configs) {
		String option = getDhcpOption(configs, 51);
		return option != null ? Integer.valueOf(option) : 0;
	}

	public static InetAddress getServerIdentifier(List<DhcpOptionConfig> configs) {
		try {
			String option = getDhcpOption(configs, 54);
			if (option != null)
				return InetAddress.getByName(option);
		} catch (UnknownHostException e) {
		}

		return null;
	}

	public static String getDhcpOption(List<DhcpOptionConfig> configs, int type) {
		for (DhcpOptionConfig c : configs)
			if (c.getType() == type)
				return c.getValue();

		return null;
	}

	public static List<DhcpOptionConfig> getGroupConfigs(String groupName) {
		Connection c = null;
		try {
			c = newConnection();
			return getGroupConfigs(c, groupName);
		} catch (SQLException e) {
			rollback(c);
			throw new RuntimeException(e);
		} finally {
			disconnect(c);
		}
	}

	public static List<DhcpOptionConfig> getGroupConfigs(Connection c, String groupName) {
		List<DhcpOptionConfig> configs = new ArrayList<DhcpOptionConfig>();

		try {
			String q = "select id, type, value, ordinal from dhcp_group_options where group_name = ? order by ordinal, id";
			PreparedStatement stmt = c.prepareStatement(q);
			stmt.setString(1, groupName);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				DhcpOptionConfig config = new DhcpOptionConfig();
				config.setId(rs.getInt(1));
				config.setType(rs.getInt(2));
				config.setValue(rs.getString(3));
				config.setOrdinal(rs.getInt(4));
				configs.add(config);
			}

		} catch (SQLException e) {
			error(e);
			throw new RuntimeException(e);
		}

		return configs;
	}

	public static void createGroupConfig(DhcpOptionConfig config) {
		Connection c = null;
		try {
			c = newConnection();
			String q = "insert into dhcp_group_options (group_name, type, value, ordinal) values (?,?,?,?)";
			PreparedStatement stmt = c.prepareStatement(q);
			stmt.setString(1, config.getGroupName());
			stmt.setInt(2, config.getType());
			stmt.setString(3, config.getValue());
			stmt.setInt(4, config.getOrdinal());
			stmt.executeUpdate();
			c.commit();
		} catch (SQLException e) {
			error(e);
			rollback(c);
			throw new RuntimeException(e);
		} finally {
			disconnect(c);
		}
	}

	public static void removeGroupConfig(int id) {
		Connection c = null;
		try {
			c = newConnection();
			String q = "delete from dhcp_group_options where id=?";
			PreparedStatement stmt = c.prepareStatement(q);
			stmt.setInt(1, id);
			stmt.executeUpdate();
			c.commit();
		} catch (SQLException e) {
			error(e);
			rollback(c);
			throw new RuntimeException(e);
		} finally {
			disconnect(c);
		}
	}

	public static List<DhcpIpReservation> getIpReservations(String groupName) {
		Connection c = null;
		try {
			c = newConnection();
			return getIpReservations(c, groupName);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			disconnect(c);
		}
	}

	public static List<DhcpIpReservation> getIpReservations(Connection c, String groupName) {
		List<DhcpIpReservation> reservations = new LinkedList<DhcpIpReservation>();

		try {
			String q = "SELECT ip, mac, host_name FROM dhcp_ip_reservations WHERE group_name = ?";
			PreparedStatement stmt = c.prepareStatement(q);
			stmt.setString(1, groupName);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				try {
					InetAddress ip = InetAddress.getByName(rs.getString(1));
					MacAddress mac = new MacAddress(rs.getString(2));
					String hostName = rs.getString(3);

					DhcpIpReservation reservation = new DhcpIpReservation(groupName, ip, mac, hostName);

					reservations.add(reservation);
				} catch (Exception e) {
					error(e);
				}
			}

		} catch (SQLException e) {
			error(e);
			throw new RuntimeException(e);
		}

		return reservations;
	}

	public static void createIpReservation(DhcpIpReservation r) {
		Connection c = null;
		try {
			c = newConnection();

			String q = "insert into dhcp_ip_reservations (ip, mac, group_name, host_name) values (?,?,?,?)";
			PreparedStatement stmt = c.prepareStatement(q);
			stmt.setString(1, r.getIp().getHostAddress());
			stmt.setString(2, r.getMac().toString());
			stmt.setString(3, r.getGroupName());
			stmt.setString(4, r.getHostName());

			stmt.execute();

			c.commit();
		} catch (SQLException e) {
			rollback(c);
			error(e);
			throw new RuntimeException(e);
		} finally {
			disconnect(c);
		}
	}

	public static void removeIpReservation(DhcpIpReservation r) {
		Connection c = null;
		try {
			c = newConnection();
			String q = "delete from dhcp_ip_reservations where group_name = ? and ip = ?";
			PreparedStatement stmt = c.prepareStatement(q);
			stmt.setString(1, r.getGroupName());
			stmt.setString(2, r.getIp().getHostAddress());
			stmt.execute();

			c.commit();
		} catch (SQLException e) {
			rollback(c);
			error(e);
			throw new RuntimeException(e);
		} finally {
			disconnect(c);
		}
	}

	public static List<DhcpIpLease> getIpLeases(String groupName) {
		Connection c = null;
		try {
			c = newConnection();
			return getIpLeases(c, groupName);
		} catch (SQLException e) {
			error(e);
			throw new RuntimeException(e);
		} finally {
			disconnect(c);
		}
	}

	public static List<DhcpIpLease> getIpLeases(Connection c, String groupName) {
		List<DhcpIpLease> leases = new LinkedList<DhcpIpLease>();

		try {
			String q = "select ip, mac, host_name, created_at, updated_at, expired_at from dhcp_ip_leases where group_name = ?";
			PreparedStatement stmt = c.prepareStatement(q);
			stmt.setString(1, groupName);

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				try {
					InetAddress ip = InetAddress.getByName(rs.getString(1));
					MacAddress mac = new MacAddress(rs.getString(2));
					String hostName = rs.getString(3);
					Date created = rs.getTimestamp(4);
					Date updated = rs.getTimestamp(5);
					Date expire = rs.getTimestamp(6);

					DhcpIpLease lease = new DhcpIpLease(groupName, ip, mac, hostName, expire);
					lease.setCreated(created);
					lease.setUpdated(updated);
					lease.setExpire(expire);

					leases.add(lease);
				} catch (Exception e1) {
					error(e1);
				}
			}

		} catch (SQLException e) {
			error(e);
		}

		return leases;
	}

	public static void createIpLease(DhcpIpLease lease) {
		Connection c = null;
		try {
			c = newConnection();
			createIpLease(c, lease);
		} catch (SQLException e) {
			error(e);
		} finally {
			disconnect(c);
		}
	}

	public static void createIpLease(Connection c, DhcpIpLease lease) {
		try {
			String q = "insert into dhcp_ip_leases (ip, mac, group_name, host_name, created_at, updated_at, expired_at) values (?,?,?,?,?,?,?)";
			PreparedStatement stmt = c.prepareStatement(q);
			stmt.setString(1, lease.getIp().getHostAddress());
			stmt.setString(2, lease.getMac().toString());
			stmt.setString(3, lease.getGroupName());
			stmt.setString(4, lease.getHostName());
			stmt.setTimestamp(5, toTimestamp(lease.getCreated()));
			stmt.setTimestamp(6, toTimestamp(lease.getUpdated()));
			stmt.setTimestamp(7, toTimestamp(lease.getExpire()));

			stmt.execute();

			System.out.println("ip lease record created");

			c.commit();
		} catch (SQLException e) {
			rollback(c);
			error(e);
			throw new RuntimeException(e);
		}

		// remove old binding
		removeOldBinding(c, lease.getMac(), lease.getIp());
	}

	public static void updateIpLease(Connection c, DhcpIpLease lease) {
		try {
			String q = "update dhcp_ip_leases set mac=?, host_name=?, created_at=?, updated_at=?, expired_at=? where group_name=? and ip=?";
			PreparedStatement stmt = c.prepareStatement(q);
			stmt.setString(1, lease.getMac().toString());
			stmt.setString(2, lease.getHostName());
			stmt.setTimestamp(3, toTimestamp(lease.getCreated()));
			stmt.setTimestamp(4, toTimestamp(lease.getUpdated()));
			stmt.setTimestamp(5, toTimestamp(lease.getExpire()));
			stmt.setString(6, lease.getGroupName());
			stmt.setString(7, lease.getIp().getHostAddress());
			stmt.executeUpdate();
			c.commit();
		} catch (SQLException e) {
			rollback(c);
			error(e);
			throw new RuntimeException(e);
		}

		// remove old binding
		removeOldBinding(c, lease.getMac(), lease.getIp());
	}

	private static java.sql.Timestamp toTimestamp(Date d) {
		return new java.sql.Timestamp(d.getTime());
	}

	public static void removeIpLease(DhcpIpLease lease) {
		Connection c = null;
		try {
			c = newConnection();
			removeIpLease(c, lease);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			disconnect(c);
		}
	}

	public static void removeIpLease(Connection c, DhcpIpLease lease) {
		try {
			String q = "delete from dhcp_ip_leases where ip = ? and mac = ?";
			PreparedStatement stmt = c.prepareCall(q);
			stmt.setString(1, lease.getIp().getHostAddress());
			stmt.setString(2, lease.getMac().toString());

			stmt.execute();

			c.commit();
		} catch (SQLException e) {
			rollback(c);
			throw new RuntimeException(e);
		}
	}

	public static List<DhcpFilter> getAllowFilters() {
		return getFilters(true);
	}

	public static List<DhcpFilter> getAllowFilters(Connection c) {
		return getFilters(c, true);
	}

	public static List<DhcpFilter> getBlockFilters() {
		return getFilters(false);
	}

	public static List<DhcpFilter> getBlockFilters(Connection c) {
		return getFilters(c, false);
	}

	private static List<DhcpFilter> getFilters(boolean allow) {
		Connection c = null;
		try {
			c = newConnection();
			return getFilters(c, allow);
		} catch (SQLException e) {
			error(e);
			throw new RuntimeException(e);
		} finally {
			disconnect(c);
		}
	}

	private static List<DhcpFilter> getFilters(Connection c, boolean allow) {
		List<DhcpFilter> filters = new LinkedList<DhcpFilter>();

		try {
			String q = "select mac, description FROM dhcp_filters WHERE is_allowed=?";
			PreparedStatement stmt = c.prepareStatement(q);
			stmt.setInt(1, allow ? 1 : 0);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				try {
					DhcpFilter f = new DhcpFilter();
					f.setMac(new MacAddress(rs.getString(1)));
					f.setDescription(rs.getString(2));
					f.setAllow(allow);

					filters.add(f);
				} catch (Exception e) {
					error(e);
				}
			}

		} catch (SQLException e) {
			error(e);
			throw new RuntimeException(e);
		}

		return filters;
	}

	public static void createFilter(DhcpFilter f) {
		Connection c = null;
		try {
			c = newConnection();
			String q = "insert into dhcp_filters (mac, description, is_allowed) values (?,?,?)";
			PreparedStatement stmt = c.prepareStatement(q);
			stmt.setString(1, f.getMac().toString());
			stmt.setString(2, f.getDescription());
			stmt.setInt(3, f.isAllow() ? 1 : 0);
			stmt.executeUpdate();
			c.commit();
		} catch (SQLException e) {
			error(e);
			rollback(c);
			throw new RuntimeException(e);
		} finally {
			disconnect(c);
		}
	}

	public static void updateIpFilter(DhcpFilter f) {
		Connection c = null;
		try {
			c = newConnection();
			String q = "update dhcp_filters set description=?, is_allowed=? where mac=?";
			PreparedStatement stmt = c.prepareStatement(q);
			stmt.setString(1, f.getDescription());
			stmt.setInt(2, f.isAllow() ? 1 : 0);
			stmt.setString(3, f.getMac().toString());
			stmt.executeUpdate();
			c.commit();
		} catch (SQLException e) {
			error(e);
			rollback(c);
			throw new RuntimeException(e);
		} finally {
			disconnect(c);
		}
	}

	public static void removeFilter(MacAddress mac) {
		Connection c = null;
		try {
			c = newConnection();
			String q = "delete from dhcp_filters where mac=?";
			PreparedStatement stmt = c.prepareStatement(q);
			stmt.setString(1, mac.toString());
			stmt.executeUpdate();
			c.commit();
		} catch (SQLException e) {
			error(e);
			rollback(c);
			throw new RuntimeException(e);
		} finally {
			disconnect(c);
		}
	}

	public static void removeOldBinding(Connection c, MacAddress clientMac, InetAddress newIp) {
		try {
			PreparedStatement stmt = c.prepareStatement("delete from dhcp_ip_leases where mac=? and ip<>?");
			stmt.setString(1, clientMac.toString());
			stmt.setString(2, newIp.getHostAddress());
			stmt.executeUpdate();
			c.commit();
		} catch (SQLException e) {
			error(e);
			rollback(c);
			throw new RuntimeException(e);
		}
	}

	private static void rollback(Connection c) {
		try {
			c.rollback();
		} catch (SQLException e) {
			error(e);
		}
	}

	private static void disconnect(Connection c) {
		try {
			if (c != null && !c.isClosed())
				c.close();
		} catch (Exception e) {
			error(e);
		}
	}

	private static void trace(String msg, Object... args) {
		Logger logger = LoggerFactory.getLogger(DhcpDatabase.class.getName());
		logger.trace(msg, args);
	}

	private static void error(Throwable t) {
		Logger logger = LoggerFactory.getLogger(DhcpDatabase.class.getName());
		logger.error("kraken dhcp: database operation failed", t);
	}
}
