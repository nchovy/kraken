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
import java.util.Map;

import org.krakenapps.api.FieldOption;
import org.krakenapps.api.MapTypeHint;
import org.krakenapps.confdb.CollectionName;

@CollectionName("organization")
public class Organization {
	@FieldOption(nullable = false)
	private String domain;

	@FieldOption(nullable = false, length = 60)
	private String name;

	@FieldOption(length = 60)
	private String phone;

	@FieldOption(length = 255)
	private String address;

	@FieldOption(length = 255)
	private String description;

	private boolean isEnabled;
	private String domainController;
	private String backupDomainController;

	@MapTypeHint({ String.class, Object.class })
	private Map<String, Object> parameters = new HashMap<String, Object>();

	@FieldOption(nullable = false)
	private Date created = new Date();

	@FieldOption(nullable = false)
	private Date updated = new Date();

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
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
}
