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
import java.util.Arrays;

import org.krakenapps.radius.client.MalformedResponseException;
import org.krakenapps.radius.client.RadiusClient;
import org.krakenapps.radius.protocol.AccessRequest;
import org.krakenapps.radius.protocol.NasIpAddressAttribute;
import org.krakenapps.radius.protocol.NasPortAttribute;
import org.krakenapps.radius.protocol.RadiusPacket;
import org.krakenapps.radius.protocol.RadiusResponse;
import org.krakenapps.radius.protocol.UserNameAttribute;
import org.krakenapps.radius.protocol.UserPasswordAttribute;

public class PapAuthenticator implements Authenticator {
	private RadiusClient client;
	private String userName;
	private String password;

	public PapAuthenticator(RadiusClient client, String userName, String password) {
		this.client = client;
		this.userName = userName;
		this.password = password;
	}

	@Override
	public RadiusResponse authenticate() throws IOException {
		String sharedSecret = client.getSharedSecret();

		AccessRequest req = new AccessRequest();
		req.setIdentifier(client.getNextId());
		req.setUserName(new UserNameAttribute(userName));
		req.setUserPassword(new UserPasswordAttribute(req.getAuthenticator(), sharedSecret, password));
		req.setNasIpAddress(new NasIpAddressAttribute(InetAddress.getByName("127.0.0.1")));
		req.setNasPort(new NasPortAttribute(0));
		req.finalize();

		DatagramSocket socket = new DatagramSocket();
		try {
			socket.connect(client.getIpAddress(), client.getPort());

			byte[] payload = req.getBytes();
			DatagramPacket packet = new DatagramPacket(payload, payload.length);
			socket.setSoTimeout(5000);
			socket.send(packet);

			byte[] buf = new byte[65535];
			DatagramPacket response = new DatagramPacket(buf, buf.length);
			socket.receive(response);

			RadiusResponse resp = (RadiusResponse) RadiusPacket.parse(sharedSecret, buf);

			byte[] expectedAuthenticator = RadiusResponse.calculateResponseAuthenticator(resp, sharedSecret,
					req.getAuthenticator());
			if (!Arrays.equals(expectedAuthenticator, resp.getAuthenticator()))
				throw new MalformedResponseException(req, resp);

			return resp;
		} finally {
			socket.close();
		}
	}
}
