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
import java.util.List;
import java.util.UUID;

import org.krakenapps.api.CollectionTypeHint;
import org.krakenapps.api.FieldOption;
import org.krakenapps.api.ReferenceKey;

public class OrganizationUnit {
	@FieldOption(nullable = false)
	private String guid = UUID.randomUUID().toString();

	@FieldOption(length = 60, nullable = false)
	private String name;

	private String domainController;

	@FieldOption(nullable = false)
	private Date createDateTime = new Date();

	@ReferenceKey("guid")
	@CollectionTypeHint(OrganizationUnit.class)
	private List<OrganizationUnit> children = new ArrayList<OrganizationUnit>();

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
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

	public Date getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(Date createDateTime) {
		this.createDateTime = createDateTime;
	}

	public List<OrganizationUnit> getChildren() {
		return children;
	}

	public void setChildren(List<OrganizationUnit> children) {
		this.children = children;
	}
}
