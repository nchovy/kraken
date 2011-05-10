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
package org.krakenapps.radius.client;

import java.io.IOException;
import java.net.InetAddress;

import org.krakenapps.radius.client.auth.Authenticator;
import org.krakenapps.radius.protocol.RadiusPacket;

public class RadiusClient {
	private InetAddress addr;
	private int port;
	private String sharedSecret;

	public RadiusClient(InetAddress addr, String sharedSecret) {
		this(addr, 1812, sharedSecret);
	}

	public RadiusClient(InetAddress addr, int port, String sharedSecret) {
		this.addr = addr;
		this.port = port;
		this.sharedSecret = sharedSecret;
	}

	public RadiusPacket authenticate(Authenticator authenticator) throws IOException {
		return authenticator.authenticate(addr, port, sharedSecret);
	}
}
