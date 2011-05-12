package org.krakenapps.radius.server;

public abstract class RadiusVirtualServerEventListener {
	public void onClose(RadiusVirtualServer virtualServer) {
	}

	public void onSetProfile(RadiusVirtualServer virtualServer, String profileName) {
	}

	public void onAddClientProfile(RadiusVirtualServer virtualServer, RadiusClientAddress client, String profileName) {
	}

	public void onRemoveClientProfile(RadiusVirtualServer virtualServer, RadiusClientAddress client) {
	}
}
