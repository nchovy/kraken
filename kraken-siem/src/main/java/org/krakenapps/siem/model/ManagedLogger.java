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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.krakenapps.msgbus.Marshalable;

@Entity
@Table(name = "siem_managed_loggers")
public class ManagedLogger implements Marshalable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@Column(name = "org_id", nullable = false)
	private int organizationId;

	@Column(name = "fullname")
	private String fullName;

	@Column(name = "parser")
	private String parserFactoryName;

	@Column(name = "is_enabled")
	private boolean isEnabled;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "managedLogger")
	private List<LogParserOption> logParserOptions = new ArrayList<LogParserOption>();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(int organizationId) {
		this.organizationId = organizationId;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getParserFactoryName() {
		return parserFactoryName;
	}

	public void setParserFactoryName(String parserFactoryName) {
		this.parserFactoryName = parserFactoryName;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public List<LogParserOption> getLogParserOptions() {
		return logParserOptions;
	}

	public void setLogParserOptions(List<LogParserOption> logParserOptions) {
		this.logParserOptions = logParserOptions;
	}

	@Override
	public Map<String, Object> marshal() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("id", id);
		m.put("fullname", fullName);
		m.put("parser_factory_name", parserFactoryName);
		m.put("is_enabled", isEnabled);
		return m;
	}

	@Override
	public String toString() {
		return "id=" + id + ", fullname=" + fullName + ", parsername=" + parserFactoryName + ", enabled=" + isEnabled;
	}
}
