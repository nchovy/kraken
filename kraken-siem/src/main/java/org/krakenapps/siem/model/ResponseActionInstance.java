/*
 * Copyright 2011 NCHOVY
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
package org.krakenapps.siem.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "siem_response_actions", uniqueConstraints = { @UniqueConstraint(columnNames = { "manager", "namespace",
		"name" }) })
public class ResponseActionInstance {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	private String manager;
	private String namespace;
	private String name;
	private String description;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "instance")
	private List<ResponseActionConfig> configs = new ArrayList<ResponseActionConfig>();

	@ManyToMany
	@JoinTable(name = "siem_events_to_responses", joinColumns = @JoinColumn(name = "mapping_id"), inverseJoinColumns = @JoinColumn(name = "response_id"))
	private Set<EventResponseMapping> eventMappings = new HashSet<EventResponseMapping>();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getManager() {
		return manager;
	}

	public void setManager(String manager) {
		this.manager = manager;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
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

	public List<ResponseActionConfig> getConfigs() {
		return configs;
	}

	public void setConfigs(List<ResponseActionConfig> configs) {
		this.configs = configs;
	}

	public Set<EventResponseMapping> getEventMappings() {
		return eventMappings;
	}

	public void setEventMappings(Set<EventResponseMapping> eventMappings) {
		this.eventMappings = eventMappings;
	}

}
