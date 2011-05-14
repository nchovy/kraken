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
package org.krakenapps.radius.client.auth;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.krakenapps.radius.protocol.AccessRequest;
import org.krakenapps.radius.protocol.NasIpAddressAttribute;
import org.krakenapps.radius.protocol.NasPortAttribute;
import org.krakenapps.radius.protocol.RadiusPacket;
import org.krakenapps.radius.protocol.UserNameAttribute;
import org.krakenapps.radius.protocol.UserPasswordAttribute;

public class PapAuthenticator implements Authenticator {

	private String userName;
	private String password;

	public PapAuthenticator(String userName, String password) {
		this.userName = userName;
		this.password = password;
	}

	@Override
	public RadiusPacket authenticate(InetAddress addr, int port, String sharedSecret) throws IOException {
		AccessRequest req = new AccessRequest();
		req.setUserName(new UserNameAttribute(userName));
		req.setUserPassword(new UserPasswordAttribute(req.getAuthenticator(), sharedSecret, password));
		req.setNasIpAddress(new NasIpAddressAttribute(InetAddress.getByName("127.0.0.1")));
		req.setNasPort(new NasPortAttribute(0));

		DatagramSocket socket = new DatagramSocket();
		socket.connect(addr, port);
		
		byte[] payload = req.getBytes();
		DatagramPacket packet = new DatagramPacket(payload, payload.length);
		socket.setSoTimeout(5000);
		socket.send(packet);

		byte[] buf = new byte[65535];
		DatagramPacket response = new DatagramPacket(buf, buf.length);
		socket.receive(response);

		return RadiusPacket.parse(sharedSecret, buf);
	}
}
