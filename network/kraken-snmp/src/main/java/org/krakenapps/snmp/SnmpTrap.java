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
package org.krakenapps.snmp;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class SnmpTrap {
	private InetSocketAddress remoteAddress;
	private InetSocketAddress localAddress;
	private int version;
	private String enterpriseOid;
	private int genericTrap;
	private int specificTrap;
	private Map<String, Object> variableBindings = new HashMap<String, Object>();

	public InetSocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	public void setRemoteAddress(InetSocketAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	public InetSocketAddress getLocalAddress() {
		return localAddress;
	}

	public void setLocalAddress(InetSocketAddress localAddress) {
		this.localAddress = localAddress;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getEnterpriseOid() {
		return enterpriseOid;
	}

	public void setEnterpriseOid(String enterpriseOid) {
		this.enterpriseOid = enterpriseOid;
	}

	public int getGenericTrap() {
		return genericTrap;
	}

	public void setGenericTrap(int genericTrap) {
		this.genericTrap = genericTrap;
	}

	public int getSpecificTrap() {
		return specificTrap;
	}

	public void setSpecificTrap(int specificTrap) {
		this.specificTrap = specificTrap;
	}

	public Map<String, Object> getVariableBindings() {
		return variableBindings;
	}

	public void setVariableBindings(Map<String, Object> variableBindings) {
		this.variableBindings = variableBindings;
	}

	@Override
	public String toString() {
		return "remote=" + remoteAddress + ", local=" + localAddress + ", version=" + version + ", enterprise="
				+ enterpriseOid + ", generic_trap=" + genericTrap + ", specific_trap=" + specificTrap + ", bindings="
				+ variableBindings.size();
	}

}
