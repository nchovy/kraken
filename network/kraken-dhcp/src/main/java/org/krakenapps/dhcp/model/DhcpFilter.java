/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.dhcp.model;

import org.krakenapps.dhcp.MacAddress;

public class DhcpFilter {
	private MacAddress mac;
	private String description;
	private boolean allow;
	
	public DhcpFilter() {
	}
	
	public DhcpFilter(MacAddress mac, String description, boolean allow) {
		this.mac = mac;
		this.description = description;
		this.allow = allow;
	}

	public MacAddress getMac() {
		return mac;
	}

	public void setMac(MacAddress mac) {
		this.mac = mac;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isAllow() {
		return allow;
	}

	public void setAllow(boolean allow) {
		this.allow = allow;
	}

	@Override
	public String toString() {
		return String.format("mac=%s, description=%s, allowed=%s", mac, description, allow);
	}
}
