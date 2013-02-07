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
package org.krakenapps.dhcp.msgbus;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.dhcp.DhcpOptionCode;
import org.krakenapps.dhcp.DhcpServer;
import org.krakenapps.dhcp.MacAddress;
import org.krakenapps.dhcp.model.DhcpFilter;
import org.krakenapps.dhcp.model.DhcpIpGroup;
import org.krakenapps.dhcp.model.DhcpIpLease;
import org.krakenapps.dhcp.model.DhcpIpReservation;
import org.krakenapps.dhcp.model.DhcpOptionConfig;
import org.krakenapps.msgbus.MsgbusException;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "dhcp-plugin")
@MsgbusPlugin
public class DhcpPlugin {
	@Requires
	private DhcpServer server;

	@MsgbusMethod
	public void groups(Request req, Response resp) {
		List<DhcpIpGroup> groups = server.getIpGroups();
		resp.put("groups", marshal(groups));
	}

	@MsgbusMethod
	public void createGroup(Request req, Response resp) {
		try {
			DhcpIpGroup group = buildIpGroup(req);
			InetAddress subnet = InetAddress.getByName(req.getString("subnet"));
			InetAddress serverIp = InetAddress.getByName(req.getString("server_ip"));
			InetAddress router = InetAddress.getByName(req.getString("router"));
			InetAddress dns = InetAddress.getByName(req.getString("dns"));
			int leaseDuration = req.getInteger("lease_duration");

			server.createIpGroup(group);
			server.createGroupOption(new DhcpOptionConfig(group.getName(), 1, subnet.getHostAddress()));
			server.createGroupOption(new DhcpOptionConfig(group.getName(), 54, serverIp.getHostAddress()));
			server.createGroupOption(new DhcpOptionConfig(group.getName(), 3, router.getHostAddress()));
			server.createGroupOption(new DhcpOptionConfig(group.getName(), 6, dns.getHostAddress()));
			server.createGroupOption(new DhcpOptionConfig(group.getName(), 51, Integer.toString(leaseDuration)));
		} catch (UnknownHostException e) {
			throw new MsgbusException("0", e.getMessage());
		}
	}

	@MsgbusMethod
	public void updateGroup(Request req, Response resp) {
		try {
			DhcpIpGroup group = buildIpGroup(req);
			server.updateIpGroup(group);
		} catch (UnknownHostException e) {
			throw new MsgbusException("0", e.getMessage());
		}
	}

	private DhcpIpGroup buildIpGroup(Request req) throws UnknownHostException {
		DhcpIpGroup group = new DhcpIpGroup();
		group.setName(req.getString("name"));
		group.setFrom(InetAddress.getByName(req.getString("from")));
		group.setTo(InetAddress.getByName(req.getString("to")));
		return group;
	}

	@MsgbusMethod
	public void removeGroup(Request req, Response resp) {
		String name = req.getString("name");
		server.removeIpGroup(name);
	}

	@MsgbusMethod
	public void groupConfigs(Request req, Response resp) {
		String name = req.getString("name");
		List<DhcpOptionConfig> configs = server.getGroupOptions(name);
		resp.put("configs", marshal(configs));
	}

	@MsgbusMethod
	public void createGroupConfigs(Request req, Response resp) {
		String groupName = req.getString("group_name");
		int type = req.getInteger("type");
		String value = req.getString("value");
		server.createGroupOption(new DhcpOptionConfig(groupName, type, value));
	}

	@MsgbusMethod
	public void removeGroupConfigs(Request req, Response resp) {
		int id = req.getInteger("id");
		server.removeGroupOption(id);
	}

	@MsgbusMethod
	public void offers(Request req, Response resp) {
		List<DhcpIpLease> offers = server.getIpOffers();
		resp.put("offers", marshal(offers));
	}

	@MsgbusMethod
	public void leases(Request req, Response resp) {
		for (DhcpIpGroup group : server.getIpGroups()) {
			String groupName = group.getName();
			List<DhcpIpLease> leases = server.getIpLeases(groupName);
			resp.put(groupName, marshal(leases));
		}
	}

	@MsgbusMethod
	public void purgeLease(Request req, Response resp) {
		try {
			String target = req.getString("target");
			if (target.equals("all"))
				server.purgeIpLease();
			else
				server.purgeIpLease(InetAddress.getByName(target));
		} catch (UnknownHostException e) {
			throw new MsgbusException("0", e.getMessage());
		}
	}

	@MsgbusMethod
	public void reservations(Request req, Response resp) {
		String groupName = req.getString("group_name");
		List<DhcpIpReservation> reservations = server.getIpReservations(groupName);
		resp.put("reservations", marshal(reservations));
	}

	@MsgbusMethod
	public void reserve(Request req, Response resp) {
		try {
			String groupName = req.getString("group_name");
			InetAddress ip = InetAddress.getByName(req.getString("ip"));
			MacAddress mac = new MacAddress(req.getString("mac_address"));
			String hostName = req.getString("host_name");
			server.reserve(new DhcpIpReservation(groupName, ip, mac, hostName));
		} catch (UnknownHostException e) {
			throw new MsgbusException("0", e.getMessage());
		}
	}

	@MsgbusMethod
	public void unreserve(Request req, Response resp) {
		try {
			String groupName = req.getString("group_name");
			InetAddress ip = InetAddress.getByName(req.getString("ip"));
			server.unreserve(new DhcpIpReservation(groupName, ip, null, null));
		} catch (UnknownHostException e) {
			throw new MsgbusException("0", e.getMessage());
		}
	}

	@MsgbusMethod
	public void filters(Request req, Response resp) {
		List<DhcpFilter> allows = server.getAllowFilters();
		List<DhcpFilter> blocks = server.getBlockFilters();
		resp.put("allows", marshal(allows));
		resp.put("blocks", marshal(blocks));
	}

	@MsgbusMethod
	public void allow(Request req, Response resp) {
		MacAddress mac = new MacAddress(req.getString("mac_address"));
		String description = req.getString("description");
		server.createFilter(new DhcpFilter(mac, description, true));
	}

	@MsgbusMethod
	public void block(Request req, Response resp) {
		MacAddress mac = new MacAddress(req.getString("mac_address"));
		String description = req.getString("description");
		server.createFilter(new DhcpFilter(mac, description, false));
	}

	@MsgbusMethod
	public void removeFilter(Request req, Response resp) {
		String mac = req.getString("mac_address");
		server.removeFilter(new MacAddress(mac));
	}

	@MsgbusMethod
	public void options(Request req, Response resp) {
		List<DhcpOptionCode> options = Arrays.asList(DhcpOptionCode.values());
		resp.put("options", marshal(options));
	}

	private List<Object> marshal(Collection<?> objs) {
		List<Object> l = new ArrayList<Object>();
		for (Object obj : objs) {
			if (obj instanceof DhcpIpGroup)
				l.add(marshal((DhcpIpGroup) obj));
			else if (obj instanceof DhcpOptionConfig)
				l.add(marshal((DhcpOptionConfig) obj));
			else if (obj instanceof DhcpIpLease)
				l.add(marshal((DhcpIpLease) obj));
			else if (obj instanceof DhcpIpReservation)
				l.add(marshal((DhcpIpReservation) obj));
			else if (obj instanceof DhcpFilter)
				l.add(marshal((DhcpFilter) obj));
			else if (obj instanceof DhcpOptionCode)
				l.add(marshal((DhcpOptionCode) obj));
		}
		return l;
	}

	private Map<String, Object> marshal(DhcpIpGroup group) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("name", group.getName());
		m.put("description", group.getDescription());
		m.put("from", group.getFrom().getHostAddress());
		m.put("to", group.getTo().getHostAddress());
		return m;
	}

	private Map<String, Object> marshal(DhcpOptionConfig obj) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("id", obj.getId());
		m.put("group_name", obj.getGroupName());
		m.put("type", obj.getType());
		m.put("value", obj.getValue());
		m.put("ordinal", obj.getOrdinal());
		return m;
	}

	private Map<String, Object> marshal(DhcpIpLease obj) {
		SimpleDateFormat dateFormat = new SimpleDateFormat();
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("group_name", obj.getGroupName());
		m.put("ip", obj.getIp().getHostAddress());
		m.put("mac_address", obj.getMac().toString());
		m.put("host_name", obj.getHostName());
		m.put("expire", dateFormat.format(obj.getExpire()));
		m.put("created", dateFormat.format(obj.getCreated()));
		m.put("updated", dateFormat.format(obj.getUpdated()));
		return m;
	}

	private Map<String, Object> marshal(DhcpIpReservation obj) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("group_name", obj.getGroupName());
		m.put("host_name", obj.getHostName());
		m.put("ip", obj.getIp().getHostAddress());
		m.put("mac_address", obj.getMac().toString());
		return m;
	}

	private Map<String, Object> marshal(DhcpFilter obj) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("mac_address", obj.getMac().toString());
		m.put("description", obj.getDescription());
		m.put("allow", obj.isAllow());
		return m;
	}

	private Map<String, Object> marshal(DhcpOptionCode obj) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("code", obj.code());
		m.put("name", obj.name());
		m.put("class", obj.getValueType() != null ? obj.getValueType().getName() : null);
		return m;
	}
}
