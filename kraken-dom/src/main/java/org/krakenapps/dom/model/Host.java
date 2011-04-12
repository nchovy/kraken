/*
 * Copyright 2011 Future Systems, Inc.
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
package org.krakenapps.dom.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.krakenapps.msgbus.Marshalable;

@Entity
@Table(name = "dom_hosts")
@NamedQueries( {
		@NamedQuery(name = "Host.findAll", query = "FROM Host h WHERE h.organization.id = ?"),
		@NamedQuery(name = "Host.findById", query = "FROM Host h WHERE h.organization.id = ? AND h.id = ?"),
		@NamedQuery(name = "Host.findByArea", query = "FROM Host h WHERE h.organization.id = ? AND h.area.id = ?"),
		@NamedQuery(name = "Host.findByAreas", query = "FROM Host h WHERE h.organization.id = ? AND h.area.id in (:ids)") })
public class Host implements Marshalable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@Column(nullable = false, length = 60)
	private String name;

	private String description;

	@Column(nullable = false, length = 36)
	private String guid;

	@OneToOne(cascade = CascadeType.ALL, mappedBy = "host")
	private Sentry sentry;

	@ManyToOne
	@JoinColumn(name = "org_id", nullable = false)
	private Organization organization;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "type_id", nullable = false)
	private HostType hostType;

	@ManyToOne
	@JoinColumn(name = "area_id", nullable = false)
	private Area area;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "host")
	private List<NetworkInterface> networkInterfaces = new ArrayList<NetworkInterface>();

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "dom_hosts_to_exts", joinColumns = @JoinColumn(name = "host_id"), inverseJoinColumns = @JoinColumn(name = "ext_id"))
	private Set<HostExtension> extensions = new HashSet<HostExtension>();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public Sentry getSentry() {
		return sentry;
	}

	public void setSentry(Sentry sentry) {
		this.sentry = sentry;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public HostType getHostType() {
		return hostType;
	}

	public void setHostType(HostType hostType) {
		this.hostType = hostType;
	}

	public Area getArea() {
		return area;
	}

	public void setArea(Area area) {
		this.area = area;
	}

	public List<NetworkInterface> getNetworkInterfaces() {
		return networkInterfaces;
	}

	public void setNetworkInterfaces(List<NetworkInterface> networkInterfaces) {
		this.networkInterfaces = networkInterfaces;
	}

	public Set<HostExtension> getExtensions() {
		return extensions;
	}

	public void setExtensions(Set<HostExtension> extensions) {
		this.extensions = extensions;
	}

	@Override
	public Map<String, Object> marshal() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("id", id);
		m.put("name", name);
		m.put("description", description);
		m.put("guid", guid);
		m.put("type_id", hostType.getId());
		m.put("type_name", hostType.getName());
		m.put("area_id", area.getId());
		return m;
	}

}
