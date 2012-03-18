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
package org.krakenapps.ipmanager;

import java.util.Date;
import java.util.List;

import org.krakenapps.ipmanager.model.Agent;
import org.krakenapps.ipmanager.model.AllowedMac;
import org.krakenapps.ipmanager.model.DeniedMac;
import org.krakenapps.ipmanager.model.HostEntry;
import org.krakenapps.ipmanager.model.IpEntry;
import org.krakenapps.ipmanager.model.IpEventLog;

public interface IpManager {
	List<Agent> getAgents(int orgId);

	List<HostEntry> getHosts(int orgId);

	List<IpEntry> getIpEntries(IpQueryCondition condition);

	List<AllowedMac> getAllowMacAddresses(int orgId, int ipId);

	int allowMacAddress(int orgId, int ipId, String mac, Date from, Date to);

	void disallowMacAddress(int orgId, int macId);

	List<DeniedMac> getDenyMacAddresses(int orgId, int agentId);

	int denyMacAddress(int orgId, int agentId, String mac, Date from, Date to);

	void removeDenyMacAddress(int orgId, int macId);

	void updateIpEntry(IpDetection detection);

	List<IpEventLog> getLogs(LogQueryCondition condition);

	void addListener(IpEventListener callback);

	void removeListener(IpEventListener callback);
}
