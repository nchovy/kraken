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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.krakenapps.msgbus.Marshalable;

@Entity
@Table(name = "ipm_ip_entries", uniqueConstraints = @UniqueConstraint(columnNames = { "agent_id", "ip" }))
public class IpEntry implements Marshalable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@ManyToOne
	@JoinColumn(name = "agent_id", nullable = false)
	private Agent agent;

	@Column(length = 60, nullable = false)
	private String ip;

	@Column(name = "mac", length = 20, nullable = false)
	private String currentMac;

	@Column(name = "first_seen", nullable = false)
	private Date firstSeen;

	@Column(name = "last_seen", nullable = false)
	private Date lastSeen;

	@Column(name = "protected", nullable = false)
	private boolean isProtected;

	@Column(name = "is_block", nullable = false)
	private boolean isBlock;

	@OneToMany(mappedBy = "ip", cascade = CascadeType.ALL)
	private List<DetectedMac> detectedMacs = new ArrayList<DetectedMac>();

	@OneToMany(mappedBy = "ip", cascade = CascadeType.ALL)
	private List<AllowedMac> allowedMacs = new ArrayList<AllowedMac>();

	@ManyToMany
	@JoinTable(name = "ipm_ip_to_hosts", joinColumns = @JoinColumn(name = "ip_id"), inverseJoinColumns = @JoinColumn(name = "host_id"))
	private Set<HostEntry> hostEntries = new HashSet<HostEntry>();

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

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getCurrentMac() {
		return currentMac;
	}

	public void setCurrentMac(String currentMac) {
		this.currentMac = currentMac;
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

	public boolean isProtected() {
		return isProtected;
	}

	public void setProtected(boolean isProtected) {
		this.isProtected = isProtected;
	}

	public boolean isBlock() {
		return isBlock;
	}

	public void setBlock(boolean isBlock) {
		this.isBlock = isBlock;
	}

	public List<DetectedMac> getDetectedMacs() {
		return detectedMacs;
	}

	public void setDetectedMacs(List<DetectedMac> detectedMacs) {
		this.detectedMacs = detectedMacs;
	}

	public List<AllowedMac> getAllowedMacs() {
		return allowedMacs;
	}

	public void setAllowedMacs(List<AllowedMac> allowedMacs) {
		this.allowedMacs = allowedMacs;
	}

	public Set<HostEntry> getHostEntries() {
		return hostEntries;
	}

	public void setHostEntries(Set<HostEntry> hostEntries) {
		this.hostEntries = hostEntries;
	}

	@Override
	public Map<String, Object> marshal() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("id", id);
		m.put("agent_id", agent.getId());
		m.put("ip", ip);
		m.put("mac", currentMac);
		m.put("is_protected", isProtected);
		m.put("is_block", isBlock);
		m.put("first_seen", dateFormat.format(firstSeen));
		m.put("last_seen", dateFormat.format(lastSeen));
		return m;
	}

	@Override
	public String toString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return String.format("%-15s first seen=%s, last seen=%s", ip, dateFormat.format(firstSeen),
				dateFormat.format(lastSeen));
	}

}
