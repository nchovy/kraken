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
import java.util.List;

import org.krakenapps.api.CollectionTypeHint;

public class ResponseActionInstance {
	private int id;
	private String manager;
	private String namespace;
	private String name;
	private String description;

	@CollectionTypeHint(ResponseActionConfig.class)
	private List<ResponseActionConfig> configs = new ArrayList<ResponseActionConfig>();

	@CollectionTypeHint(EventResponseMapping.class)
	private List<EventResponseMapping> eventMappings = new ArrayList<EventResponseMapping>();

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

	public List<EventResponseMapping> getEventMappings() {
		return eventMappings;
	}

	public void setEventMappings(List<EventResponseMapping> eventMappings) {
		this.eventMappings = eventMappings;
	}

}
