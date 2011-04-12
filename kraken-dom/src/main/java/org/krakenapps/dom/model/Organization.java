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
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.krakenapps.msgbus.Marshalable;

@Entity
@Table(name = "dom_organizations")
public class Organization implements Marshalable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@Column(nullable = false, length = 60)
	private String name;

	@Column(length = 60)
	private String phone;

	@Column(length = 255)
	private String address;

	@Column(length = 255)
	private String description;

	@Column(name = "created_at", nullable = false)
	private Date createDateTime;

	@Column(name = "is_enabled", nullable = false)
	private boolean isEnabled;

	@Column(name = "dc")
	private String domainController;

	@Column(name = "bdc")
	private String backupDomainController;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "organization")
	private List<Area> areas = new ArrayList<Area>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "organization")
	private List<Sentry> sentries = new ArrayList<Sentry>();

	@ManyToMany
	@JoinTable(name = "dom_orgs_to_packs", joinColumns = @JoinColumn(name = "org_id"), inverseJoinColumns = @JoinColumn(name = "pack_id"))
	private Set<ProgramPack> programPacks = new HashSet<ProgramPack>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "organization")
	private List<ProgramProfile> programProfiles = new ArrayList<ProgramProfile>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "organization")
	private List<FileSpace> fileSpaces = new ArrayList<FileSpace>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "organization")
	private List<Timetable> timetables = new ArrayList<Timetable>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "organization")
	private List<NetworkAddress> networkAddresses = new ArrayList<NetworkAddress>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "organization")
	private List<OrganizationUnit> organizationUnits = new ArrayList<OrganizationUnit>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "organization")
	private List<User> users = new ArrayList<User>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "organization")
	private List<OrganizationParameter> parameters = new ArrayList<OrganizationParameter>();

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

	public List<Area> getAreas() {
		return areas;
	}

	public void setAreas(List<Area> areas) {
		this.areas = areas;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(Date createDateTime) {
		this.createDateTime = createDateTime;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public String getDomainController() {
		return domainController;
	}

	public void setDomainController(String domainController) {
		this.domainController = domainController;
	}

	public String getBackupDomainController() {
		return backupDomainController;
	}

	public void setBackupDomainController(String backupDomainController) {
		this.backupDomainController = backupDomainController;
	}

	public Set<ProgramPack> getProgramPacks() {
		return programPacks;
	}

	public void setProgramPacks(Set<ProgramPack> programPacks) {
		this.programPacks = programPacks;
	}

	public List<ProgramProfile> getProgramProfiles() {
		return programProfiles;
	}

	public void setProgramProfiles(List<ProgramProfile> programProfiles) {
		this.programProfiles = programProfiles;
	}

	public List<FileSpace> getFileSpaces() {
		return fileSpaces;
	}

	public void setFileSpaces(List<FileSpace> fileSpaces) {
		this.fileSpaces = fileSpaces;
	}

	public List<Sentry> getSentries() {
		return sentries;
	}

	public void setSentries(List<Sentry> sentries) {
		this.sentries = sentries;
	}

	public List<Timetable> getTimetables() {
		return timetables;
	}

	public void setTimetables(List<Timetable> timetables) {
		this.timetables = timetables;
	}

	public List<NetworkAddress> getNetworkAddresses() {
		return networkAddresses;
	}

	public void setNetworkAddresses(List<NetworkAddress> networkAddresses) {
		this.networkAddresses = networkAddresses;
	}

	public List<OrganizationUnit> getOrganizationUnits() {
		return organizationUnits;
	}

	public void setOrganizationUnits(List<OrganizationUnit> organizationUnits) {
		this.organizationUnits = organizationUnits;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public List<OrganizationParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<OrganizationParameter> parameters) {
		this.parameters = parameters;
	}

	@Override
	public Map<String, Object> marshal() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", id);
		map.put("name", name);
		map.put("address", address);
		map.put("phone", phone);
		map.put("description", description);
		map.put("is_enabled", isEnabled);
		return map;
	}

}
