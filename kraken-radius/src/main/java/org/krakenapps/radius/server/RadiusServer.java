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

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

public interface RadiusServer {
	//
	// Virtual server management
	//

	List<RadiusVirtualServer> getVirtualServers();

	RadiusVirtualServer getVirtualServer(String name);

	RadiusVirtualServer createVirtualServer(String name, InetSocketAddress bindAddress, RadiusPortType portType,
			String profileName);

	void removeVirtualServer(String name);

	//
	// Profile management
	//

	List<RadiusProfile> getProfiles();

	RadiusProfile getProfile(String name);

	void createProfile(RadiusProfile profile);

	void updateProfile(RadiusProfile profile);

	void removeProfile(String name);

	//
	// Authenticator management
	//

	List<RadiusAuthenticatorFactory> getAuthenticatorFactories();

	List<RadiusAuthenticator> getAuthenticators();

	RadiusAuthenticator createAuthenticator(String instanceName, String factoryName, Map<String, Object> configs);

	void removeAuthenticator(String instanceName);

	//
	// User database management
	//

	List<RadiusUserDatabase> getUserDatabases();

	RadiusUserDatabase getUserDatabase(String name);

	RadiusUserDatabase createUserDatabase(String instanceName, String factoryName, Map<String, Object> configs);

	void removeUserDatabase(String instanceName);

	//
	// Event callbacks
	//

	void addEventListener(RadiusServerEventListener listener);

	void removeEventListener(RadiusServerEventListener listener);
}
