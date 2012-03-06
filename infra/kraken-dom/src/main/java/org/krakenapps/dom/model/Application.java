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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.krakenapps.api.FieldOption;
import org.krakenapps.api.MapTypeHint;
import org.krakenapps.api.ReferenceKey;
import org.krakenapps.confdb.CollectionName;

@CollectionName("application")
public class Application {
	@FieldOption(nullable = false)
	private String guid = UUID.randomUUID().toString();

	@FieldOption(nullable = false)
	@ReferenceKey("guid")
	private Vendor vendor;

	@FieldOption(length = 20)
	private String platform;

	@FieldOption(nullable = false, length = 60)
	private String name;

	@FieldOption(nullable = false)
	private Date created = new Date();

	@FieldOption(nullable = false)
	private Date updated = new Date();

	@MapTypeHint({ String.class, Object.class })
	private Map<String, Object> metadatas = new HashMap<String, Object>();

	@FieldOption(skip = true)
	private List<ApplicationVersion> versions;

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

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public Map<String, Object> getMetadatas() {
		return metadatas;
	}

	public void setMetadatas(Map<String, Object> metadatas) {
		this.metadatas = metadatas;
	}

	public List<ApplicationVersion> getVersions() {
		return versions;
	}

	public void setVersions(List<ApplicationVersion> versions) {
		this.versions = versions;
	}
}
