package org.krakenapps.sleepproxy;

import java.util.Collection;
import java.util.Date;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.msgbus.Marshaler;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;
import org.krakenapps.sleepproxy.exception.SleepPolicyNotFoundException;
import org.krakenapps.sleepproxy.model.Agent;
import org.krakenapps.sleepproxy.model.AgentGroup;
import org.krakenapps.sleepproxy.model.SleepLog;
import org.krakenapps.sleepproxy.model.SleepPolicy;

@Component(name = "sleep-proxy-plugin")
@MsgbusPlugin
public class SleepProxyPlugin {
	@Requires
	private SleepProxyApi sleepProxyApi;

	@MsgbusMethod
	public void getLogs(Request req, Response resp) {
		Date from = req.getDate("from");
		Date to = req.getDate("to");
		Integer groupId = req.getInteger("group_id");

		Collection<SleepLog> logs = sleepProxyApi.getLogs(groupId, from, to);
		resp.put("logs", Marshaler.marshal(logs));
	}

	//
	// Agent Management
	//

	@MsgbusMethod
	public void getAgents(Request req, Response resp) {
		int groupId = 0;
		if (req.has("group_id"))
			groupId = req.getInteger("group_id");

		Collection<Agent> agents = sleepProxyApi.getAgents(groupId, true);
		resp.put("agents", Marshaler.marshal(agents));
	}

	@MsgbusMethod
	public void removeAgent(Request req, Response resp) {
		int agentId = req.getInteger("agent_id");
		sleepProxyApi.removeAgent(agentId);
	}

	@MsgbusMethod
	public void getAgent(Request req, Response resp) {
		if (!req.has("agent_id"))
			throw new IllegalArgumentException("agent id is required");

		int agentId = req.getInteger("agent_id");

		Agent agent = sleepProxyApi.getAgent(agentId);
		resp.put("agent", agent.marshal());
	}

	@MsgbusMethod
	public void getAgentGroups(Request req, Response resp) {
		int groupId = 0;
		if (req.has("group_id") && req.getInteger("group_id") != null)
			groupId = req.getInteger("group_id");
		else
			groupId = sleepProxyApi.getRootAgentGroup().getId();

		Collection<AgentGroup> groups = sleepProxyApi.getAgentGroups(groupId, true);
		resp.put("agent_groups", Marshaler.marshal(groups));
	}

	@MsgbusMethod
	public void getAgentGroup(Request req, Response resp) {
		int groupId = 0;
		if (req.has("group_id") && req.getInteger("group_id") != null)
			groupId = req.getInteger("group_id");
		else
			groupId = sleepProxyApi.getRootAgentGroup().getId();

		AgentGroup group = sleepProxyApi.getAgentGroup(groupId);
		resp.put("agent_group", group != null ? group.marshal() : null);
	}

	@MsgbusMethod
	public void createAgentGroup(Request req, Response resp) {
		int parentId = req.getInteger("parent_id");
		String name = req.getString("name");
		String description = req.getString("description");
		int policyId = req.getInteger("policy_id");

		SleepPolicy policy = sleepProxyApi.getSleepPolicy(policyId);
		if (policy == null)
			throw new IllegalArgumentException("policy " + policyId + " not found");

		AgentGroup group = sleepProxyApi.createAgentGroup(parentId, name, description, policyId);
		resp.put("created", group.getId());
	}

	@MsgbusMethod
	public void updateAgentGroup(Request req, Response resp) {
		int id = req.getInteger("group_id");
		String name = req.getString("name");
		String description = req.getString("description");
		int policyId = req.getInteger("policy_id");

		SleepPolicy policy = sleepProxyApi.getSleepPolicy(policyId);
		if (policy == null)
			throw new IllegalArgumentException("policy " + policyId + " not found");

		sleepProxyApi.updateAgentGroup(id, name, description, policyId);
	}

	@MsgbusMethod
	public void removeAgentGroup(Request req, Response resp) {
		int id = req.getInteger("group_id");
		sleepProxyApi.removeAgentGroup(id);
	}

	@MsgbusMethod
	public void wake(Request req, Response resp) {
		if (!req.has("agent_id")) {
			throw new IllegalArgumentException("agent id is required");
		}

		int id = req.getInteger("agent_id");
		sleepProxyApi.wakeAgent(id);
	}

	//
	// Sleep Policy
	//

	@MsgbusMethod
	public void getSleepPolicies(Request req, Response resp) {
		Collection<SleepPolicy> policies = sleepProxyApi.getSleepPolicies();
		resp.put("policies", Marshaler.marshal(policies));
	}

	@MsgbusMethod
	public void getSleepPolicy(Request req, Response resp) {
		int policyId = req.getInteger("policy_id");
		SleepPolicy policy = sleepProxyApi.getSleepPolicy(policyId);
		if (policy == null)
			throw new SleepPolicyNotFoundException();

		resp.put("policy", policy.marshal());
	}

	@MsgbusMethod
	public void createSleepPolicy(Request req, Response resp) {
		String name = req.getString("name");
		String description = req.getString("description");
		int awayCriteria = req.getInteger("away");
		boolean forceHibernate = req.getBoolean("force_hibernate");

		SleepPolicy policy = sleepProxyApi.createSleepPolicy(name, description, awayCriteria, forceHibernate);
		resp.put("created", policy != null ? policy.marshal() : null);
	}

	@MsgbusMethod
	public void updateSleepPolicy(Request req, Response resp) {
		int id = req.getInteger("policy_id");
		String name = req.getString("name");
		String description = req.getString("description");
		int awayCriteria = req.getInteger("away");
		boolean forceHibernate = req.getBoolean("force_hibernate");

		sleepProxyApi.updateSleepPolicy(id, name, description, awayCriteria, forceHibernate);
	}

	@MsgbusMethod
	public void removeSleepPolicy(Request req, Response resp) {
		int policyId = req.getInteger("policy_id");
		sleepProxyApi.removeSleepPolicy(policyId);
	}

	//
	// Statistics
	//

	@MsgbusMethod
	public void getGroupPowerGraph(Request req, Response resp) {
		Date from = req.getDate("from");
		Date to = req.getDate("to");
		Integer groupId = req.getInteger("group_id");

		Collection<PowerStat> graph = sleepProxyApi.getGroupPowerGraph(groupId, from, to, true);
		resp.put("graph", Marshaler.marshal(graph));
	}
	
	@MsgbusMethod
	public void getAgentPowerGraph(Request req, Response resp) {
		Date from = req.getDate("from");
		Date to = req.getDate("to");
		int agentId = req.getInteger("agent_id");
		
		Collection<PowerStat> graph = sleepProxyApi.getAgentPowerGraph(agentId, from, to);
		resp.put("graph", Marshaler.marshal(graph));
	}
}
