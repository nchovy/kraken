package org.krakenapps.sleepproxy;

import java.util.Collection;
import java.util.Date;

import org.krakenapps.sleepproxy.model.Agent;
import org.krakenapps.sleepproxy.model.AgentGroup;
import org.krakenapps.sleepproxy.model.SleepLog;
import org.krakenapps.sleepproxy.model.SleepPolicy;

public interface SleepProxyApi {
	AgentGroup getRootAgentGroup();

	AgentGroup getAgentGroup(int id);

	Agent getAgent(String guid);

	Agent getAgent(int id);

	void removeAgent(int id);

	Collection<AgentGroup> getAgentGroups(Integer id, boolean descendants);

	Collection<Agent> getAgents(Integer groupId, boolean descendants);

	Collection<SleepLog> getLogs(Date from, Date to);

	Collection<SleepLog> getLogs(Integer groupId, Date from, Date to);

	Collection<SleepLog> getLogs(String guid, Date from, Date to);

	AgentGroup createAgentGroup(Integer parentId, String name, String description, int policyId);

	void updateAgentGroup(int id, String name, String description, int policyId);

	void removeAgentGroup(int id);

	Collection<SleepPolicy> getSleepPolicies();

	SleepPolicy getSleepPolicy(int id);

	SleepPolicy createSleepPolicy(String name, String description, int awayCriteria, boolean forceHibernate);

	void updateSleepPolicy(int id, String name, String description, int awayCriteria, boolean forceHibernate);

	void removeSleepPolicy(int id);

	void wakeAgent(int id);
	
	Collection<PowerStat> getGroupPowerGraph(Integer groupId, Date from, Date to, boolean descendants);
	
	Collection<PowerStat> getAgentPowerGraph(int id, Date from, Date to);
}
