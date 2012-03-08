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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.krakenapps.api.CollectionTypeHint;
import org.krakenapps.api.DateFormat;
import org.krakenapps.api.FieldOption;
import org.krakenapps.api.ReferenceKey;
import org.krakenapps.confdb.CollectionName;

@CollectionName("host")
public class Host {
	@FieldOption(nullable = false)
	private String guid = UUID.randomUUID().toString();

	@FieldOption(nullable = false)
	@ReferenceKey("guid")
	private HostType type;

	@ReferenceKey("guid")
	private Area area;

	@FieldOption(nullable = false, length = 60)
	private String name;

	private String description;

	/**
	 * os specific data
	 */
	private Map<String, Object> data = new HashMap<String, Object>();

	/**
	 * device type string key to device list
	 */
	private Map<String, Object> devices = new HashMap<String, Object>();

	@CollectionTypeHint(HostExtension.class)
	private List<HostExtension> extensions = new ArrayList<HostExtension>();

	@FieldOption(nullable = false)
	private Date created = new Date();

	@FieldOption(nullable = false)
	private Date updated = new Date();

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public HostType getType() {
		return type;
	}

	public void setType(HostType type) {
		this.type = type;
	}

	public Area getArea() {
		return area;
	}

	public void setArea(Area area) {
		this.area = area;
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

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	public Map<String, Object> getDevices() {
		return devices;
	}

	public void setDevices(Map<String, Object> devices) {
		this.devices = devices;
	}

	public List<HostExtension> getExtensions() {
		return extensions;
	}

	public void setExtensions(List<HostExtension> extensions) {
		this.extensions = extensions;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	@Override
	public String toString() {
		return "guid=" + guid + ", type=" + type + ", area=" + area + ", name=" + name + ", created="
				+ DateFormat.format("yyyy-MM-dd HH:mm:ss", created) + ", updated="
				+ DateFormat.format("yyyy-MM-dd HH:mm:ss", created);
	}

}
