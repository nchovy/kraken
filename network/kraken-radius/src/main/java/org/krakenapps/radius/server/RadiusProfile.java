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
package org.krakenapps.radius.server;

import java.util.ArrayList;
import java.util.List;

import org.krakenapps.api.CollectionTypeHint;
import org.krakenapps.confdb.CollectionName;

@CollectionName("profiles")
public class RadiusProfile {
	private String name;
	private String sharedSecret;

	@CollectionTypeHint(String.class)
	private List<String> authenticators;

	@CollectionTypeHint(String.class)
	private List<String> userDatabases;

	public RadiusProfile() {
		this.authenticators = new ArrayList<String>();
		this.userDatabases = new ArrayList<String>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSharedSecret() {
		return sharedSecret;
	}

	public void setSharedSecret(String sharedSecret) {
		this.sharedSecret = sharedSecret;
	}

	public List<String> getAuthenticators() {
		return authenticators;
	}

	public void setAuthenticators(List<String> authenticators) {
		this.authenticators = authenticators;
	}

	public List<String> getUserDatabases() {
		return userDatabases;
	}

	public void setUserDatabases(List<String> userDatabases) {
		this.userDatabases = userDatabases;
	}

	@Override
	public String toString() {
		return String.format("name=%s, shared secret=%s, auth=%s, userdb=%s", name, sharedSecret,
				generateCsv(authenticators), generateCsv(userDatabases));
	}

	private String generateCsv(List<String> l) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (String s : l) {
			if (i != 0)
				sb.append(", ");

			sb.append(s);
			i++;
		}

		return sb.toString();
	}
}
