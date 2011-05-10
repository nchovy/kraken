package org.krakenapps.radius.client.auth;

import java.io.IOException;
import java.net.InetAddress;

import org.krakenapps.radius.protocol.RadiusPacket;

public interface Authenticator {
	RadiusPacket authenticate(InetAddress addr, int port, String sharedSecret) throws IOException;
}
