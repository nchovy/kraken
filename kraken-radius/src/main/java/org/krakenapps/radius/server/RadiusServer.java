package org.krakenapps.radius.server;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

public interface RadiusServer {
	List<RadiusVirtualServer> getVirtualServers();

	void createVirtualServer(String name, InetSocketAddress bindAddress, RadiusPortType portType, String profileName);

	void removeVirtualServer(String name);

	List<RadiusProfile> getProfiles();

	RadiusProfile getProfile(String name);

	void createProfile(RadiusProfile profile);

	void updateProfile(RadiusProfile profile);

	void removeProfile(String name);

	List<RadiusAuthenticatorFactory> getAuthenticatorFactories();

	List<RadiusAuthenticator> getAuthenticators();

	void createAuthenticator(String instanceName, String factoryName, Map<String, Object> configs);

	void removeAuthenticator(String instanceName);
	
	void addEventListener(RadiusServerEventListener listener);
	
	void removeEventListener(RadiusServerEventListener listener);
}
