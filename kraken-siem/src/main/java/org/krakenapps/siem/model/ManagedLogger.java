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

import java.util.HashMap;
import java.util.Map;

import org.krakenapps.msgbus.Marshalable;

public class ManagedLogger implements Marshalable {
	private int orgId;

	private String fullName;

	private String parserFactoryName;

	/**
	 * string to string map
	 */
	private Map<String, Object> logParserOptions = new HashMap<String, Object>();

	private boolean isEnabled = true;

	public int getOrgId() {
		return orgId;
	}

	public void setOrgId(int orgId) {
		this.orgId = orgId;
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

	public Map<String, Object> getLogParserOptions() {
		return logParserOptions;
	}

	public void setLogParserOptions(Map<String, Object> logParserOptions) {
		this.logParserOptions = logParserOptions;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	@Override
	public Map<String, Object> marshal() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("fullname", fullName);
		m.put("parser_factory_name", parserFactoryName);
		m.put("is_enabled", isEnabled);
		return m;
	}

	@Override
	public String toString() {
		return "fullname=" + fullName + ", parsername=" + parserFactoryName + ", enabled=" + isEnabled;
	}
}
