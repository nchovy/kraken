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
package org.krakenapps.ipmanager.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.krakenapps.msgbus.Marshalable;

@Entity
@Table(name = "ipm_hosts")
public class HostEntry implements Marshalable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@ManyToOne
	@JoinColumn(name = "agent_id", nullable = false)
	private Agent agent;

	@Column(length = 80)
	private String name;

	@Column(name = "workgroup", length = 80)
	private String workGroup;

	@Column(length = 60)
	private String category;

	@Column(length = 60)
	private String vendor;

	@Column(name = "first_seen", nullable = false)
	private Date firstSeen;

	@Column(name = "last_seen", nullable = false)
	private Date lastSeen;

	@ManyToMany(mappedBy = "hostEntries")
	private Set<IpEntry> ipEntries = new HashSet<IpEntry>();

	@OneToMany(mappedBy = "host", cascade = CascadeType.ALL)
	private List<HostNic> hostMacs = new ArrayList<HostNic>();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Agent getAgent() {
		return agent;
	}

	public void setAgent(Agent agent) {
		this.agent = agent;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getWorkGroup() {
		return workGroup;
	}

	public void setWorkGroup(String workGroup) {
		this.workGroup = workGroup;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public Date getFirstSeen() {
		return firstSeen;
	}

	public void setFirstSeen(Date firstSeen) {
		this.firstSeen = firstSeen;
	}

	public Date getLastSeen() {
		return lastSeen;
	}

	public void setLastSeen(Date lastSeen) {
		this.lastSeen = lastSeen;
	}

	public Set<IpEntry> getIpEntries() {
		return ipEntries;
	}

	public void setIpEntries(Set<IpEntry> ipEntries) {
		this.ipEntries = ipEntries;
	}

	public List<HostNic> getHostMacs() {
		return hostMacs;
	}

	public void setHostMacs(List<HostNic> hostMacs) {
		this.hostMacs = hostMacs;
	}

	@Override
	public Map<String, Object> marshal() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");

		Map<String, Object> m = new HashMap<String, Object>();
		m.put("id", id);
		m.put("agent_id", agent.getId());
		m.put("name", name);
		m.put("workgroup", workGroup);
		m.put("category", category);
		m.put("vendor", vendor);
		m.put("first_seen", dateFormat.format(firstSeen));
		m.put("last_seen", dateFormat.format(lastSeen));
		return m;
	}

	@Override
	public String toString() {
		return String.format("[%3d] %s, workgroup=%s, category=%s, vendor=%s", id, name, workGroup, category, vendor);
	}
}
