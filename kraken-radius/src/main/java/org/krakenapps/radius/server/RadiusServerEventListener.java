package org.krakenapps.radius.server;

public abstract class RadiusServerEventListener {
	public void onCreateVirtualServer(RadiusVirtualServer virtualServer) {
	}

	public void onRemoveVirtualServer(RadiusVirtualServer virtualServer) {
	}

	public void onCreateAuthenticator(RadiusAuthenticator authenticator) {
	}
	
	public void onRemoveAuthenticator(RadiusAuthenticator authenticator) {
	}
	
	public void onCreateProfile(RadiusProfile profile) {
	}

	public void onUpdateProfile(RadiusProfile oldProfile, RadiusProfile newProfile) {
	}
	
	public void onRemoveProfile(RadiusProfile profile) {
	}
}
