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
package org.krakenapps.siem;

import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NormalizedLog {
	private String orgDomain;
	private Map<String, Object> params;

	public NormalizedLog() {
		params = new HashMap<String, Object>();
	}

	public NormalizedLog(String orgDomain, Map<String, Object> params) {
		this.orgDomain = orgDomain;
		this.params = params;
	}

	public String getOrgDomain() {
		return orgDomain;
	}

	public void setOrgDomain(String orgDomain) {
		this.orgDomain = orgDomain;
	}

	public void set(String key, Object value) {
		params.put(key, value);
	}

	public Object get(String key) {
		return params.get(key);
	}

	public String getString(String key) {
		return (String) params.get(key);
	}

	public Integer getInteger(String key) {
		return (Integer) params.get(key);
	}

	public Date getDate(String key) {
		return (Date) params.get(key);
	}

	public InetAddress getIp(String key) {
		return (InetAddress) params.get(key);
	}

	@Override
	public String toString() {
		return "org=" + orgDomain + ", params=" + params;
	}
}
