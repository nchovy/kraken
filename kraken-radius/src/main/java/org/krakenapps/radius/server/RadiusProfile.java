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

import java.util.List;

public class RadiusProfile {
	private String name;
	private String sharedSecret;
	private List<String> authenticators;
	private List<String> userDatabases;

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
}
