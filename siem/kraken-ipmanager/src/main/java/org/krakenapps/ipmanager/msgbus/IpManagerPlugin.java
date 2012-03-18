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
package org.krakenapps.ipmanager.msgbus;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.ipmanager.ArpScanner;
import org.krakenapps.ipmanager.IpEventListener;
import org.krakenapps.ipmanager.IpManager;
import org.krakenapps.ipmanager.IpQueryCondition;
import org.krakenapps.ipmanager.LogQueryCondition;
import org.krakenapps.ipmanager.model.Agent;
import org.krakenapps.ipmanager.model.AllowedMac;
import org.krakenapps.ipmanager.model.DeniedMac;
import org.krakenapps.ipmanager.model.HostEntry;
import org.krakenapps.ipmanager.model.IpEntry;
import org.krakenapps.ipmanager.model.IpEventLog;
import org.krakenapps.ipmanager.model.IpEventLog.Type;
import org.krakenapps.lookup.mac.MacLookupService;
import org.krakenapps.lookup.mac.Vendor;
import org.krakenapps.msgbus.Marshaler;
import org.krakenapps.msgbus.PushApi;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;
import org.krakenapps.pcap.decoder.ethernet.MacAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MsgbusPlugin
@Component(name = "ipm-msgbus-plugin")
public class IpManagerPlugin implements IpEventListener {
	private final Logger logger = LoggerFactory.getLogger(IpManagerPlugin.class.getName());

	@Requires
	private IpManager ipManager;

	@Requires
	private MacLookupService macLookup;

	@Requires
	private PushApi pushApi;

	@Requires
	private ArpScanner arpScanner;

	@Validate
	public void start() {
		ipManager.addListener(this);
	}

	@Invalidate
	public void stop() {
		if (ipManager != null)
			ipManager.removeListener(this);
	}

	@MsgbusMethod
	public void getAgents(Request req, Response resp) {
		List<Agent> agents = ipManager.getAgents(req.getOrgId());
		resp.put("agents", Marshaler.marshal(agents));
	}

	@MsgbusMethod
	public void getIpEntries(Request req, Response resp) {
		int orgId = req.getOrgId();
		Integer agentId = req.getInteger("agent_id");

		IpQueryCondition condition = new IpQueryCondition(orgId);
		condition.setAgentId(agentId);

		List<IpEntry> ipEntries = ipManager.getIpEntries(condition);
		List<Object> l = Marshaler.marshal(ipEntries);
		addMacVendors(l);

		resp.put("ip_entries", l);
	}

	private void addMacVendors(List<Object> l) {
		for (Object o : l) {
			@SuppressWarnings("unchecked")
			Map<String, Object> m = (Map<String, Object>) o;
			Vendor vendor = macLookup.findByMac((String) m.get("mac"));
			m.put("mac_vendor", marshalVendor(vendor));
		}
	}

	private Map<String, Object> marshalVendor(Vendor vendor) {
		if (vendor == null)
			return null;

		Map<String, Object> m = new HashMap<String, Object>();
		m.put("country", vendor.getCountry());
		m.put("name", vendor.getName());
		m.put("address", vendor.getAddress());
		return m;
	}

	@MsgbusMethod
	public void getIpAllocationRequests(Request req, Response resp) {

	}

	@MsgbusMethod
	public void getIpAllocationRequest(Request req, Response resp) {

	}

	@MsgbusMethod
	public void createIpAllocationRequest(Request req, Response resp) {

	}

	@MsgbusMethod
	public void getIpEventLogs(Request req, Response resp) {
		int orgId = req.getOrgId();
		Integer agentId = req.getInteger("agent_id");
		int page = req.getInteger("page");
		int pageSize = req.getInteger("page_size");

		LogQueryCondition condition = new LogQueryCondition(orgId, page, pageSize);
		condition.setAgentId(agentId);
		if (req.has("type"))
			condition.setType(Type.valueOf(req.getString("type")));
		if (req.has("ip_from") && req.has("ip_to"))
			condition.setIpRange(req.getString("ip_from"), req.getString("ip_to"));
		if (req.has("mac"))
			condition.setMac(req.getString("mac"));
		if (req.has("from"))
			condition.setFrom(req.getDate("from"));
		if (req.has("to"))
			condition.setTo(req.getDate("to"));

		List<IpEventLog> logs = ipManager.getLogs(condition);
		resp.put("logs", Marshaler.marshal(logs));
	}

	@MsgbusMethod
	public void getAllowMacAddresses(Request req, Response resp) {
		int orgId = req.getOrgId();
		int ipId = req.getInteger("ip_id");
		List<AllowedMac> allowed = ipManager.getAllowMacAddresses(orgId, ipId);
		resp.put("allowed", Marshaler.marshal(allowed));
	}

	@MsgbusMethod
	public void allowMacAddress(Request req, Response resp) {
		int orgId = req.getOrgId();
		int ipId = req.getInteger("ip_id");
		String mac = req.getString("mac");
		Date from = req.getDate("from");
		Date to = req.getDate("to");

		int macId = ipManager.allowMacAddress(orgId, ipId, mac, from, to);
		resp.put("mac_id", macId);
	}

	@SuppressWarnings("unchecked")
	@MsgbusMethod
	public void disallowMacAddress(Request req, Response resp) {
		int orgId = req.getOrgId();
		List<Integer> macIds = (List<Integer>) req.get("mac_id");

		for (Integer macId : macIds)
			ipManager.disallowMacAddress(orgId, macId);
	}

	@MsgbusMethod
	public void deniedMacAddresses(Request req, Response resp) {
		int orgId = req.getOrgId();
		int agentId = req.getInteger("agnet_id");
		List<DeniedMac> denied = ipManager.getDenyMacAddresses(orgId, agentId);
		resp.put("denied", Marshaler.marshal(denied));
	}

	@MsgbusMethod
	public void denyMacAddress(Request req, Response resp) {
		int orgId = req.getOrgId();
		int agentId = req.getInteger("agent_id");
		String mac = req.getString("mac");
		Date from = req.getDate("from");
		Date to = req.getDate("to");
		ipManager.denyMacAddress(orgId, agentId, mac, from, to);
	}

	@SuppressWarnings("unchecked")
	@MsgbusMethod
	public void removeDenyMacAddress(Request req, Response resp) {
		int orgId = req.getOrgId();
		List<Integer> macIds = (List<Integer>) req.get("mac_id");

		for (Integer macId : macIds)
			ipManager.removeDenyMacAddress(orgId, macId);
	}

	@MsgbusMethod
	public void getQuarantinedHosts(Request req, Response resp) {

	}

	@MsgbusMethod
	public void getHosts(Request req, Response resp) {
		int orgId = req.getOrgId();
		List<HostEntry> hosts = ipManager.getHosts(orgId);
		resp.put("hosts", Marshaler.marshal(hosts));
	}

	@MsgbusMethod
	public void arpscan(Request req, Response resp) {
		arpScanner.run();
	}

	@MsgbusMethod
	@Override
	public void onNewIpDetected(Agent agent, InetAddress ip, MacAddress mac) {
		push(Type.NewIpDetected, agent, ip, null, mac, null);
	}

	@Override
	public void onNewMacDetected(Agent agent, InetAddress ip, MacAddress mac) {
		push(Type.NewMacDetected, agent, ip, null, mac, null);
	}

	@Override
	public void onIpChanged(Agent agent, InetAddress ip1, InetAddress ip2, MacAddress mac) {
		push(Type.IpChanged, agent, ip1, ip2, mac, null);
	}

	@Override
	public void onMacChanged(Agent agent, InetAddress ip, MacAddress oldMac, MacAddress newMac) {
		push(Type.MacChanged, agent, ip, null, oldMac, newMac);
	}

	@Override
	public void onIpConflict(Agent agent, InetAddress ip, MacAddress originalMac, MacAddress conflictMac) {
		push(Type.IpConflict, agent, ip, null, originalMac, conflictMac);
	}

	private void push(Type type, Agent agent, InetAddress ip1, InetAddress ip2, MacAddress mac1, MacAddress mac2) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Map<String, Object> m = new HashMap<String, Object>();

		// TODO: localized log message using push interceptor
		m.put("type", type.getCode());
		m.put("ip1", ip1.getHostAddress());
		m.put("ip2", ip2 == null ? null : ip2.getHostAddress());
		m.put("mac1", mac1.toString());
		m.put("mac2", mac2 == null ? null : mac2.toString());
		m.put("date", dateFormat.format(new Date()));

		pushApi.push(agent.getOrgId(), "kraken-ipm-log", m);

		logger.info("kraken ipmanager: push message ip event [type={}, ip={}]", type, ip1.getHostAddress());
	}
}
