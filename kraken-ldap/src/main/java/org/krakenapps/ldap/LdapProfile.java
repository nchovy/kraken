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
package org.krakenapps.ldap;

public class LdapProfile {
	private String name;
	private String dc;
	private int port;
	private String account;
	private String password;

	public LdapProfile(String name, String dc, String account, String password) {
		this(name, dc, 389, account, password);
	}

	public LdapProfile(String name, String dc, int port, String account, String password) {
		this.name = name;
		this.dc = dc;
		this.port = port;
		this.account = account;
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public String getDc() {
		return dc;
	}

	public int getPort() {
		return port;
	}

	public String getAccount() {
		return account;
	}

	public String getPassword() {
		return password;
	}

	@Override
	public String toString() {
		return String.format("name=%s, dc=%s, port=%d, account=%s", name, dc, port, account);
	}
}
