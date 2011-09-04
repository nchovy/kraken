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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.krakenapps.msgbus.Marshalable;

@Entity
@Table(name = "dom_users", uniqueConstraints = { @UniqueConstraint(columnNames = { "login_name" }) })
public class User implements Marshalable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@ManyToOne
	@JoinColumn(name = "org_id", nullable = false)
	private Organization organization;

	@ManyToOne
	@JoinColumn(name = "ou_id")
	private OrganizationUnit organizationUnit;

	@Column(name = "login_name", length = 60, nullable = false)
	private String loginName;

	@Column(length = 60, nullable = false)
	private String name;

	@Column(length = 250)
	private String description;

	@Column(name = "pwd", length = 60)
	private String password;

	@Column(name = "salt", length = 20)
	private String salt;

	@Column(length = 60)
	private String title;

	@Column(length = 60)
	private String department;

	@Column(length = 60)
	private String email;

	@Column(length = 60)
	private String phone;

	@Column(name = "external", nullable = false)
	private boolean external;

	@Column(name = "dc", length = 60)
	private String domainController;

	@Column(name = "created_at", nullable = false)
	private Date createDateTime;

	@Column(name = "updated_at", nullable = false)
	private Date updateDateTime;

	@OneToOne(mappedBy = "user", cascade = CascadeType.REMOVE)
	private Admin admin;

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

	public OrganizationUnit getOrganizationUnit() {
		return organizationUnit;
	}

	public void setOrganizationUnit(OrganizationUnit organizationUnit) {
		this.organizationUnit = organizationUnit;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public boolean isExternal() {
		return external;
	}

	public void setExternal(boolean external) {
		this.external = external;
	}

	public String getDomainController() {
		return domainController;
	}

	public void setDomainController(String domainController) {
		this.domainController = domainController;
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

	public Admin getAdmin() {
		return admin;
	}

	public void setAdmin(Admin admin) {
		this.admin = admin;
	}

	@Override
	public Map<String, Object> marshal() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("id", id);
		m.put("ou_id", organizationUnit == null ? null : organizationUnit.getId());
		m.put("name", name);
		m.put("login_name", loginName);
		m.put("description", description);
		m.put("external", external);
		m.put("dc", domainController);
		m.put("email", email);
		m.put("phone", phone);
		m.put("title", title);
		m.put("department", department);
		m.put("created_at", dateFormat.format(createDateTime));
		m.put("updated_at", dateFormat.format(updateDateTime));
		return m;
	}
}
