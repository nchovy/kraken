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
@Table(name = "dom_apps", uniqueConstraints = { @UniqueConstraint(columnNames = { "vendor_id", "name" }) })
public class Application implements Marshalable {
	@Id
	@Column(length = 36)
	private String guid;

	@ManyToOne
	@JoinColumn(name = "vendor_id", nullable = false)
	private Vendor vendor;

	@Column(nullable = false, length = 60)
	private String name;

	@Column(name = "created_at", nullable = false)
	private Date createDateTime;

	@Column(name = "updated_at", nullable = false)
	private Date updateDateTime;

	@OneToMany(mappedBy = "key.application", cascade = CascadeType.ALL)
	private List<ApplicationMetadata> metadatas = new ArrayList<ApplicationMetadata>();

	@OneToMany(mappedBy = "application", cascade = CascadeType.ALL)
	private List<ApplicationVersion> applicationVersions = new ArrayList<ApplicationVersion>();

	@ManyToMany
	@JoinTable(name = "dom_apps_to_groups", joinColumns = @JoinColumn(name = "app_id"), inverseJoinColumns = @JoinColumn(name = "group_id"))
	private Set<ApplicationGroup> applicationGroups = new HashSet<ApplicationGroup>();

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public Vendor getVendor() {
		return vendor;
	}

	public void setVendor(Vendor vendor) {
		this.vendor = vendor;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(Date createDateTime) {
		this.createDateTime = createDateTime;
	}

	public Date getUpdateDateTime() {
		return updateDateTime;
	}

	public void setUpdateDateTime(Date updateDateTime) {
		this.updateDateTime = updateDateTime;
	}

	public Set<ApplicationGroup> getApplicationGroups() {
		return applicationGroups;
	}

	public void setApplicationGroups(Set<ApplicationGroup> applicationGroups) {
		this.applicationGroups = applicationGroups;
	}

	public List<ApplicationMetadata> getMetadatas() {
		return metadatas;
	}

	public void setMetadatas(List<ApplicationMetadata> metadatas) {
		this.metadatas = metadatas;
	}

	public List<ApplicationVersion> getVersions() {
		return applicationVersions;
	}

	public void setVersions(List<ApplicationVersion> applicationVersions) {
		this.applicationVersions = applicationVersions;
	}

	@Override
	public Map<String, Object> marshal() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("guid", guid);
		m.put("vendor", vendor.getName());
		m.put("name", name);
		m.put("created_at", dateFormat.format(createDateTime));
		m.put("updated_at", dateFormat.format(updateDateTime));
		return m;
	}
}
