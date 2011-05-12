package org.krakenapps.radius.server;

import java.net.InetSocketAddress;
import java.util.List;

public interface RadiusVirtualServer {
	String getName();

	boolean isOpened();

	InetSocketAddress getBindAddress();

	RadiusPortType getPortType();

	RadiusProfile getProfile();

	void setProfile(String profileName);

	List<RadiusClientAddress> getOverriddenClients();

	RadiusProfile getClientProfile(RadiusClientAddress client);

	void addClientProfile(RadiusClientAddress client, String profileName);

	void removeClientProfile(RadiusClientAddress client);

	void addEventListener(RadiusVirtualServerEventListener listener);

	void removeEventListener(RadiusVirtualServerEventListener listener);
}
