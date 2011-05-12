package org.krakenapps.radius.server;

import java.util.List;

import org.krakenapps.radius.protocol.AccessRequest;
import org.krakenapps.radius.protocol.RadiusPacket;

public interface RadiusAuthenticator {
	String getName();
	
	RadiusAuthenticatorFactory getFactory();
	
	RadiusPacket authenticate(AccessRequest req);

	List<String> getConfigNames();
	
	void setConfig(String name, String value);

	String getConfig(String name);
}
