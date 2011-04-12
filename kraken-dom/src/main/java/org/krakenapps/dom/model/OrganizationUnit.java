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
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.krakenapps.msgbus.Marshalable;

@Entity
@Table(name = "dom_org_units")
public class OrganizationUnit implements Marshalable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@ManyToOne
	@JoinColumn(name = "org_id", nullable = false)
	private Organization organization;

	@ManyToOne
	@JoinColumn(name = "parent")
	private OrganizationUnit parent;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "parent")
	private List<OrganizationUnit> children = new ArrayList<OrganizationUnit>();

	@Column(length = 60, nullable = false)
	private String name;

	@Column(name = "dc")
	private String domainController;

	@Column(name = "from_ldap", nullable = false)
	private boolean fromLdap;

	@Column(name = "created_at", nullable = false)
	private Date createDateTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDomainController() {
		return domainController;
	}

	public void setDomainController(String domainController) {
		this.domainController = domainController;
	}

	public boolean isFromLdap() {
		return fromLdap;
	}

	public void setFromLdap(boolean fromLdap) {
		this.fromLdap = fromLdap;
	}

	public Date getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(Date createDateTime) {
		this.createDateTime = createDateTime;
	}

	public OrganizationUnit getParent() {
		return parent;
	}

	public void setParent(OrganizationUnit parent) {
		this.parent = parent;
	}

	public List<OrganizationUnit> getChildren() {
		return children;
	}

	public void setChildren(List<OrganizationUnit> children) {
		this.children = children;
	}

	@Override
	public Map<String, Object> marshal() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("id", id);
		m.put("name", name);
		m.put("parent_id", (parent == null) ? null : parent.getId());
		m.put("org_id", organization.getId());
		m.put("dc", domainController);
		m.put("from_ldap", fromLdap);
		m.put("created_at", dateFormat.format(createDateTime));
		return m;
	}
}
