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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.krakenapps.msgbus.Marshalable;

@Entity
@Table(name = "ipm_agents")
public class Agent implements Marshalable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@Column(name = "org_id", nullable = false)
	private int orgId;

	@Column(name = "area_id")
	private Integer areaId;

	@Column(length = 60)
	private String name;

	@Column(name = "guid", length = 36)
	private String guid;

	@Column(length = 60, nullable = false)
	private String ip;

	@Column(length = 60, nullable = false)
	private String netmask;

	@Column(name = "prevent_new_ip", nullable = false)
	private boolean preventNewIp;

	@Column(name = "prevent_new_mac", nullable = false)
	private boolean preventNewMac;

	@Column(name = "protect_mode", nullable = false)
	private boolean protectMode;

	@Column(name = "protect_all", nullable = false)
	private boolean protectAll;

	@Column(name = "created_at", nullable = false)
	private Date createDateTime;

	@OneToMany(mappedBy = "agent", cascade = CascadeType.ALL)
	private List<DeniedMac> deniedMac = new ArrayList<DeniedMac>();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getOrgId() {
		return orgId;
	}

	public void setOrgId(int orgId) {
		this.orgId = orgId;
	}

	public Integer getAreaId() {
		return areaId;
	}

	public void setAreaId(Integer areaId) {
		this.areaId = areaId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getNetmask() {
		return netmask;
	}

	public void setNetmask(String netmask) {
		this.netmask = netmask;
	}

	public boolean isPreventNewIp() {
		return preventNewIp;
	}

	public void setPreventNewIp(boolean preventNewIp) {
		this.preventNewIp = preventNewIp;
	}

	public boolean isPreventNewMac() {
		return preventNewMac;
	}

	public void setPreventNewMac(boolean preventNewMac) {
		this.preventNewMac = preventNewMac;
	}

	public boolean isProtectMode() {
		return protectMode;
	}

	public void setProtectMode(boolean protectMode) {
		this.protectMode = protectMode;
	}

	public boolean isProtectAll() {
		return protectAll;
	}

	public void setProtectAll(boolean protectAll) {
		this.protectAll = protectAll;
	}

	public Date getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(Date createDateTime) {
		this.createDateTime = createDateTime;
	}

	public List<DeniedMac> getDeniedMac() {
		return deniedMac;
	}

	public void setDeniedMac(List<DeniedMac> deniedMac) {
		this.deniedMac = deniedMac;
	}

	@Override
	public Map<String, Object> marshal() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("id", id);
		m.put("area_id", areaId);
		m.put("guid", guid);
		m.put("name", name);
		m.put("ip", ip);
		m.put("netmask", netmask);
		m.put("prevent_new_ip", preventNewIp);
		m.put("prevent_new_mac", preventNewMac);
		m.put("protect_mode", protectMode);
		m.put("protect_all", protectAll);
		return m;
	}

	@Override
	public String toString() {
		return String.format("id=%d, name=%s, guid=%s, ip=%s, netmask=%s", id, name, guid, ip, netmask);
	}
}
