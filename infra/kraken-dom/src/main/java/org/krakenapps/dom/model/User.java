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

import org.krakenapps.api.FieldOption;
import org.krakenapps.api.MapTypeHint;
import org.krakenapps.api.ReferenceKey;
import org.krakenapps.confdb.CollectionName;

@CollectionName("user")
public class User {
	@FieldOption(nullable = false, length = 60)
	private String loginName;

	@ReferenceKey("guid")
	private OrganizationUnit orgUnit;

	@FieldOption(nullable = false, length = 60)
	private String name;

	@FieldOption(length = 250)
	private String description;

	@FieldOption(length = 60)
	private String password;

	@FieldOption(length = 20)
	private String salt;

	@FieldOption(length = 60)
	private String title;

	@FieldOption(length = 60)
	private String email;

	@FieldOption(length = 60)
	private String phone;

	@MapTypeHint({ String.class, Object.class })
	private Map<String, Object> ext = new HashMap<String, Object>();

	@FieldOption(nullable = false)
	private Date created = new Date();

	@FieldOption(nullable = false)
	private Date updated = new Date();

	@FieldOption(nullable = true)
	private Date lastPasswordChange;

	@FieldOption(nullable = true)
	private String sourceType;	
	
	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public OrganizationUnit getOrgUnit() {
		return orgUnit;
	}

	public void setOrgUnit(OrganizationUnit orgUnit) {
		this.orgUnit = orgUnit;
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
		this.lastPasswordChange = new Date();
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

	public Map<String, Object> getExt() {
		return ext;
	}

	public void setExt(Map<String, Object> ext) {
		this.ext = ext;
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

	public Date getLastPasswordChange() {
		return lastPasswordChange;
	}

	public void setLastPasswordChange(Date lastPasswordChange) {
		this.lastPasswordChange = lastPasswordChange;
	}

	@Override
	public String toString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String orgUnitName = null;
		if (orgUnit != null)
			orgUnitName = orgUnit.getName();

		return "login_name=" + loginName + ", org unit=" + orgUnitName + ", name=" + name + ", updated="
				+ dateFormat.format(updated);
	}

}
