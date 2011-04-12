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
package org.krakenapps.dhcp;

import java.net.InetAddress;
import java.util.List;

import org.krakenapps.dhcp.model.DhcpFilter;
import org.krakenapps.dhcp.model.DhcpIpGroup;
import org.krakenapps.dhcp.model.DhcpIpLease;
import org.krakenapps.dhcp.model.DhcpIpReservation;
import org.krakenapps.dhcp.model.DhcpOptionConfig;

public interface DhcpServer {
	List<DhcpIpLease> getIpOffers();

	List<DhcpIpGroup> getIpGroups();

	DhcpIpGroup getIpGroup(InetAddress ip);

	List<DhcpOptionConfig> getGroupOptions(String groupName);

	List<DhcpIpLease> getIpLeases(String groupName);

	List<DhcpIpReservation> getIpReservations(String groupName);

	List<DhcpFilter> getAllowFilters();

	List<DhcpFilter> getBlockFilters();

	void purgeIpLease();

	void purgeIpLease(InetAddress ip);

	void createIpGroup(DhcpIpGroup group);

	void updateIpGroup(DhcpIpGroup group);

	void removeIpGroup(String name);

	void createGroupOption(DhcpOptionConfig config);

	void removeGroupOption(int id);

	void reserve(DhcpIpReservation entry);

	void unreserve(DhcpIpReservation entry);

	void createFilter(DhcpFilter filter);

	void removeFilter(MacAddress mac);

	void addListener(DhcpMessageListener callback);

	void removeListener(DhcpMessageListener callback);

}
