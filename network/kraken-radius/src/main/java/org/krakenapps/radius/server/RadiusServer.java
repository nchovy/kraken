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

	RadiusVirtualServer createVirtualServer(String name, RadiusPortType portType, String profileName);

	RadiusVirtualServer createVirtualServer(String name, RadiusPortType portType, String profileName,
			InetSocketAddress bindAddress);
	
	void removeVirtualServer(String name);

	//
	// Profile management
	//

	List<RadiusProfile> getProfiles();

	RadiusProfile getProfile(String name);

	void createProfile(RadiusProfile profile);

	void updateProfile(RadiusProfile profile);

	void removeProfile(String name);

	List<RadiusModule> getModules();

	RadiusModule getModule(RadiusModuleType type);

	RadiusInstance getModuleInstance(RadiusModuleType type, String instanceName);

	RadiusInstance createModuleInstance(RadiusModuleType type, String instanceName, String factoryName,
			Map<String, Object> configs);

	void removeModuleInstance(RadiusModuleType type, String instanceName);

	//
	// Event callbacks
	//

	void addEventListener(RadiusServerEventListener listener);

	void removeEventListener(RadiusServerEventListener listener);
}
